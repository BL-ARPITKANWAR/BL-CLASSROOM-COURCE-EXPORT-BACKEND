

package com.bridgelabz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class CourseService {

    private static final Logger logger = Logger.getLogger(CourseService.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();

    public List<JsonNode> getAllCourses(String token) {
        WebClient client = buildClient(token);
        List<JsonNode> list = new ArrayList<>();

        try {
            String pageToken = null;
            int pageCount = 0;
            do {
                pageCount++;
                String currentPageToken = pageToken;
                logger.info("Fetching courses page " + pageCount + (currentPageToken != null ? " (token: " + currentPageToken + ")" : ""));
                String coursesJson = client.get()
                        .uri(uriBuilder -> {
                            var builder = uriBuilder.path("/v1/courses")
                                    .queryParam("teacherId", "me")
                                    .queryParam("pageSize", 100);
                            if (currentPageToken != null && !currentPageToken.isBlank()) {
                                builder.queryParam("pageToken", currentPageToken);
                            }
                            return builder.build();
                        })
                        .retrieve()
                        .onStatus(
                                status -> status.is4xxClientError(),
                                response -> {
                                    logger.severe("Google Classroom API returned 4xx error while fetching courses: " + response.statusCode());
                                    return response.bodyToMono(String.class)
                                            .flatMap(body -> {
                                                logger.severe("Error response body: " + body);
                                                return response.createException();
                                            });
                                })
                        .onStatus(
                                status -> status.is5xxServerError(),
                                response -> {
                                    logger.severe("Google Classroom API returned 5xx error while fetching courses: " + response.statusCode());
                                    return response.createException();
                                })
                        .bodyToMono(String.class)
                        .block();

                logger.fine("Raw API response: " + coursesJson);
                JsonNode response = mapper.readTree(coursesJson);
                JsonNode courses = response.path("courses");
                int pageCourses = 0;
                if (courses.isArray()) {
                    courses.forEach(list::add);
                    pageCourses = courses.size();
                }
                logger.info("Page " + pageCount + " returned " + pageCourses + " courses. Total so far: " + list.size());
                pageToken = response.path("nextPageToken").asText("");
            } while (!pageToken.isBlank());
            logger.info("Finished fetching all courses. Total: " + list.size());
        } catch (WebClientResponseException e) {
            logger.severe("WebClientResponseException fetching courses: " + e.getStatusCode() + " - " + e.getMessage());
            logger.severe("Response body: " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            logger.severe("Unexpected error while fetching courses: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Unable to fetch accessible classrooms from Google Classroom.", e);
        }

        return list;
    }

    /**
     * Fetches courses from Google Classroom API that start with the given prefix.
     * Case-sensitive: only matches exact prefix (e.g., "PCCOE" matches "PCCOE-2028-1" but not "pccoe-2028-1")
     * Trims whitespace from course name before comparison to handle extra spaces.
     * Handles null/undefined course names safely without throwing errors.
     * 
     * @param token Google OAuth2 access token
     * @param prefix The prefix to filter courses by (case-sensitive, e.g., "PCCOE")
     * @return List of courses whose names start with the given prefix
     */
    public List<JsonNode> getCoursesByPrefix(String token, String prefix) {
        WebClient client = buildClient(token);
        List<JsonNode> list = new ArrayList<>();

        try {
            String coursesJson = client.get()
                    .uri("/v1/courses?teacherId=me")
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError(),
                            response -> {
                                logger.warning("Google Classroom API returned 4xx error while fetching courses");
                                return response.createException();
                            })
                    .onStatus(
                            status -> status.is5xxServerError(),
                            response -> {
                                logger.severe("Google Classroom API returned 5xx error while fetching courses");
                                return response.createException();
                            })
                    .bodyToMono(String.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        logger.severe("Error fetching courses: " + ex.getMessage());
                        return java.util.Optional.of("{}").map(reactor.core.publisher.Mono::just).get();
                    })
                    .block();

            JsonNode root = mapper.readTree(coursesJson).path("courses");
            if (root.isArray()) {
                for (JsonNode course : root) {
                    String courseName = course.path("name").asText("");
                    // Trim whitespace and check if course name starts with the prefix (case-sensitive)
                    // Handles null, undefined, empty course names safely
                    if (courseName != null && courseName.trim().startsWith(prefix)) {
                        list.add(course);
                        logger.info("Found matching course: " + courseName.trim());
                    }
                }
            }
            logger.info("Total courses found with prefix '" + prefix + "': " + list.size());
        } catch (Exception e) {
            logger.severe("Unexpected error while fetching courses: " + e.getMessage());
        }

        return list;
    }

    private WebClient buildClient(String token) {
        return WebClient.builder()
                .baseUrl("https://classroom.googleapis.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                .build();
    }
}
