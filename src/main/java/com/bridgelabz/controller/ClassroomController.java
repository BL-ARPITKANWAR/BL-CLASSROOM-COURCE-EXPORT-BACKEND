package com.bridgelabz.controller;
import com.bridgelabz.model.ResponseDTO;
import com.bridgelabz.service.CourseService;
import com.bridgelabz.service.CourseworkService;
import com.bridgelabz.service.GoogleClassroomService;
import com.bridgelabz.service.StudentService;
import com.bridgelabz.service.SubmissionService;
import com.bridgelabz.security.PccoeAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.*;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

@RestController
@RequestMapping("/classroom")
public class ClassroomController {

    @Autowired
    private GoogleClassroomService classroomService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseworkService courseworkService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    @Autowired
    private PccoeAccessService pccoeAccessService;
    Logger logger = Logger.getLogger("ClassroomController");

    private String getAccessToken(OAuth2AuthenticationToken authToken) {
        String registrationId = authToken.getAuthorizedClientRegistrationId();
        String principalName = authToken.getName();
        
        logger.info("Attempting to load token for registrationId: " + registrationId + ", principalName: " + principalName);
        
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(registrationId, principalName);

        if (client == null) {
            logger.severe("No authorized client found for registrationId: " + registrationId + ", principalName: " + principalName);
            throw new RuntimeException("No authorized client found. Please log in again.");
        }
        
        if (client.getAccessToken() == null) {
            logger.severe("Access token is null for registrationId: " + registrationId);
            throw new RuntimeException("Access token not available. Please log in again.");
        }
        
        logger.info("Successfully retrieved access token for: " + principalName);
        return client.getAccessToken().getTokenValue();
    }

    // ==================== EXISTING ENDPOINTS ====================

    @GetMapping("/all-student-submissions")
    public ResponseEntity<?> exportAllBatches(OAuth2AuthenticationToken authToken) {
        try {
            long startTime = System.currentTimeMillis();
            String token = getAccessToken(authToken);
            List<String> urls = classroomService.exportAllCoursesData(token);
            long endTime = System.currentTimeMillis();
            calculatingTime(startTime, endTime);
            return ResponseEntity.ok(ResponseDTO.success(
                    "Sheets exported successfully!",
                    Map.of("urls", urls, "duration", (endTime - startTime) / 1000)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/pccoe-courses")
    public ResponseEntity<?> getPccoeCourses(OAuth2AuthenticationToken authToken) {
        if (!canAccessPccoe(authToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseDTO.error("You do not have access to PCCOE reports."));
        }
        try {
            String token = getAccessToken(authToken);
            List<JsonNode> courses = courseService.getCoursesByPrefix(token, "PCCOE");
            return ResponseEntity.ok(ResponseDTO.success("PCCOE courses fetched", courses));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to export PCCOE courses to a separate Google Spreadsheet.
     * Creates one tab per PCCOE course with student data and submission status.
     * Only courses whose name starts with "PCCOE" (case-sensitive, trimmed) are included.
     * Output is uploaded directly to Google Drive - no local files are created.
     * 
     * @param authToken OAuth2 authentication token from Google login
     * @return ResponseEntity with spreadsheet URL or error message
     */
    @GetMapping("/pccoe-cource")
    public ResponseEntity<?> exportPccoeCourses(OAuth2AuthenticationToken authToken) {
        if (!canAccessPccoe(authToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseDTO.error("You do not have access to PCCOE reports."));
        }
        try {
            long startTime = System.currentTimeMillis();
            String token = getAccessToken(authToken);
            String sheetUrl = classroomService.exportPccoeCoursesData(token);
            long endTime = System.currentTimeMillis();
            calculatingTime(startTime, endTime);
            return ResponseEntity.ok(ResponseDTO.success(
                    "PCCOE sheets exported successfully!",
                    Map.of("url", sheetUrl, "duration", (endTime - startTime) / 1000)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Error: " + e.getMessage()));
        }

    }

    private boolean canAccessPccoe(OAuth2AuthenticationToken authToken) {
        return authToken != null
                && pccoeAccessService.canAccess(authToken.getPrincipal().getAttribute("email"));
    }

    // ==================== NEW JSON ENDPOINTS FOR FRONTEND ====================

    /**
     * Returns all courses the authenticated user teaches (JSON).
     */
    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses(OAuth2AuthenticationToken authToken) {
        logger.info("Fetching courses for user: " + authToken.getName());
        try {
            String token = getAccessToken(authToken);
            List<JsonNode> courses = courseService.getAllCourses(token);
            logger.info("Successfully fetched " + courses.size() + " courses for user: " + authToken.getName());
            return ResponseEntity.ok(ResponseDTO.success("Courses fetched", courses));
        } catch (RuntimeException e) {
            logger.severe("Authentication error fetching courses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseDTO.error("Authentication error: " + e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error fetching courses: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Error fetching courses: " + e.getMessage()));
        }
    }

    /**
     * Debug endpoint to verify token retrieval and scopes
     */
    @GetMapping("/debug-token")
    public ResponseEntity<?> debugToken(OAuth2AuthenticationToken authToken) {
        try {
            String registrationId = authToken.getAuthorizedClientRegistrationId();
            String principalName = authToken.getName();
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(registrationId, principalName);
            
            Map<String, Object> debugInfo = new LinkedHashMap<>();
            debugInfo.put("principalName", principalName);
            debugInfo.put("registrationId", registrationId);
            debugInfo.put("clientFound", client != null);
            debugInfo.put("tokenFound", client != null && client.getAccessToken() != null);
            debugInfo.put("tokenExpired", client != null && client.getAccessToken() != null && client.getAccessToken().getExpiresAt().isBefore(Instant.now()));
            
            if (client != null && client.getAccessToken() != null) {
                debugInfo.put("tokenType", client.getAccessToken().getTokenType().getValue());
                debugInfo.put("issuedAt", client.getAccessToken().getIssuedAt());
                debugInfo.put("expiresAt", client.getAccessToken().getExpiresAt());
                debugInfo.put("scopes", client.getAccessToken().getScopes());
            }
            
            return ResponseEntity.ok(ResponseDTO.success("Debug info", debugInfo));
        } catch (Exception e) {
            logger.severe("Debug token error: " + e.getMessage());
            return ResponseEntity.status(500).body(ResponseDTO.error("Debug error: " + e.getMessage()));
        }
    }

    /**
     * Returns enrolled students for a specific course (JSON).
     */
    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<?> getCourseStudents(
            @PathVariable String courseId,
            OAuth2AuthenticationToken authToken) {
        try {
            String token = getAccessToken(authToken);
            Map<String, Map<String, String>> students = studentService.getStudents(courseId, token);

            List<Map<String, String>> studentList = new ArrayList<>();
            for (Map.Entry<String, Map<String, String>> entry : students.entrySet()) {
                Map<String, String> studentData = new LinkedHashMap<>();
                studentData.put("userId", entry.getKey());
                studentData.put("name", entry.getValue().getOrDefault("name", "Unknown"));
                studentData.put("email", entry.getValue().getOrDefault("email", "Unknown"));
                studentList.add(studentData);
            }

            return ResponseEntity.ok(ResponseDTO.success("Students fetched", studentList));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Error fetching students: " + e.getMessage()));
        }
    }

    /**
     * Returns coursework (assignments) for a specific course (JSON).
     */
    @GetMapping("/courses/{courseId}/coursework")
    public ResponseEntity<?> getCourseCoursework(
            @PathVariable String courseId,
            OAuth2AuthenticationToken authToken) {
        try {
            String token = getAccessToken(authToken);
            Map<String, String> coursework = courseworkService.getCourseworkTitles(courseId, token);

            List<Map<String, String>> courseworkList = new ArrayList<>();
            for (Map.Entry<String, String> entry : coursework.entrySet()) {
                Map<String, String> cwData = new LinkedHashMap<>();
                cwData.put("id", entry.getKey());
                cwData.put("title", entry.getValue());
                courseworkList.add(cwData);
            }

            return ResponseEntity.ok(ResponseDTO.success("Coursework fetched", courseworkList));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Error fetching coursework: " + e.getMessage()));
        }
    }

    /**
     * Returns full submission matrix for a course — students × assignments with statuses (JSON).
     */
    @GetMapping("/courses/{courseId}/submissions")
    public ResponseEntity<?> getCourseSubmissions(
            @PathVariable String courseId,
            OAuth2AuthenticationToken authToken) {
        try {
            String token = getAccessToken(authToken);

            Map<String, Map<String, String>> students = studentService.getStudents(courseId, token);
            Map<String, String> coursework = courseworkService.getCourseworkTitles(courseId, token);
            List<String> courseworkTitles = new ArrayList<>(coursework.values());

            // Populate submissions into the students map
            submissionService.getSubmissions(courseId, token, coursework, students);

            // Fill missing submissions
            for (Map<String, String> student : students.values()) {
                for (String title : courseworkTitles) {
                    student.putIfAbsent(title, "Missing");
                }
            }

            List<Map<String, Object>> submissionRows = new ArrayList<>();
            for (Map.Entry<String, Map<String, String>> entry : students.entrySet()) {
                Map<String, Object> row = new LinkedHashMap<>();
                Map<String, String> studentData = entry.getValue();
                row.put("userId", entry.getKey());
                row.put("name", studentData.getOrDefault("name", "Unknown"));
                row.put("email", studentData.getOrDefault("email", "Unknown"));

                Map<String, String> statuses = new LinkedHashMap<>();
                int submitted = 0;
                for (String title : courseworkTitles) {
                    String status = studentData.getOrDefault(title, "Missing");
                    statuses.put(title, status);
                    if ("Submitted".equalsIgnoreCase(status)) submitted++;
                }
                row.put("submissions", statuses);
                row.put("submittedCount", submitted);
                row.put("totalAssignments", courseworkTitles.size());
                row.put("percentage", courseworkTitles.size() > 0
                        ? String.format("%.1f", (submitted * 100.0 / courseworkTitles.size()))
                        : "0.0");

                submissionRows.add(row);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("courseworkTitles", courseworkTitles);
            result.put("students", submissionRows);
            result.put("totalStudents", submissionRows.size());
            result.put("totalCoursework", courseworkTitles.size());

            return ResponseEntity.ok(ResponseDTO.success("Submissions fetched", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Error fetching submissions: " + e.getMessage()));
        }
    }

    /**
     * Returns the authenticated user's profile info (JSON).
     */
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(OAuth2AuthenticationToken authToken) {
        try {
            OAuth2User user = authToken.getPrincipal();
            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("name", user.getAttribute("name"));
            userInfo.put("email", user.getAttribute("email"));
            userInfo.put("picture", user.getAttribute("picture"));
            userInfo.put("locale", user.getAttribute("locale"));

            return ResponseEntity.ok(ResponseDTO.success("User info fetched", userInfo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Error fetching user info: " + e.getMessage()));
        }
    }

    // ==================== HELPERS ====================

    private ResponseEntity<?> exportBatch(OAuth2AuthenticationToken authToken, String batch) {
        try {
            String token = getAccessToken(authToken);
            List<String> urls = classroomService.exportAllCoursesData(token);
            if (urls.isEmpty()) {
                return ResponseEntity.status(404).body(ResponseDTO.error("No data found for batch: " + batch));
            }
            return ResponseEntity.ok(ResponseDTO.success("Sheets exported for " + batch, Map.of("urls", urls)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Error: " + e.getMessage()));
        }
    }

    public void calculatingTime(long startTime, long endTime)
    {
        long durationSeconds = (endTime - startTime) / 1000;
        // Format as "X minutes Y seconds"
        long minutes = durationSeconds / 60;
        long seconds = durationSeconds % 60;
        String timeFormatted = (minutes > 0 ? minutes + (minutes == 1 ? " minute " : " minutes ") : "") +
                (seconds > 0 ? seconds + (seconds == 1 ? " second" : " seconds") : "") +
                (minutes == 0 && seconds == 0 ? "0 seconds" : "");
        logger.info("Total time taken by the application: " + timeFormatted);
    }
}
