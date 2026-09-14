package com.bridgelabz.controller;
import com.bridgelabz.service.CourseService; // Added for PCCOE course filtering
import com.bridgelabz.service.GoogleClassroomService;
import com.fasterxml.jackson.databind.JsonNode; // Added for PCCOE course filtering
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/classroom")
public class ClassroomController {

    @Autowired
    private GoogleClassroomService classroomService;

    @Autowired
    private CourseService courseService; // Added for PCCOE course filtering

    @Autowired
    private OAuth2AuthorizedClientService clientService;
    Logger logger = Logger.getLogger("ClassroomController");

    private String getAccessToken(OAuth2AuthenticationToken authToken) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName());

        if (client == null || client.getAccessToken() == null) {
            throw new RuntimeException("No valid access token found.");
        }
        return client.getAccessToken().getTokenValue();
    }

    @GetMapping("/all-student-submissions")
    public ResponseEntity<?> exportAllBatches(OAuth2AuthenticationToken authToken) {
        try {
            long startTime = System.currentTimeMillis();
            String token = getAccessToken(authToken);
            List<String> urls = classroomService.exportAllCoursesData(token);
            long endTime = System.currentTimeMillis();
            calculatingTime(startTime, endTime);
            return ResponseEntity.ok("Sheets exported successfully! URLs: " + urls);
        } catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // Endpoint to get all PCCOE courses - Added for PCCOE course filtering
    @GetMapping("/pccoe-courses")
    public ResponseEntity<?> getPccoeCourses(OAuth2AuthenticationToken authToken) {
        try {
            String token = getAccessToken(authToken);
            List<JsonNode> courses = courseService.getCoursesByPrefix(token, "PCCOE");
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
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
        try {
            long startTime = System.currentTimeMillis();
            String token = getAccessToken(authToken);
            // Call service method to export only PCCOE courses to spreadsheet
            String sheetUrl = classroomService.exportPccoeCoursesData(token);
            long endTime = System.currentTimeMillis();
            calculatingTime(startTime, endTime);
            return ResponseEntity.ok("PCCOE sheets exported successfully! URL: " + sheetUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    private ResponseEntity<?> exportBatch(OAuth2AuthenticationToken authToken, String batch) {
        try {
            String token = getAccessToken(authToken);
            List<String> urls = classroomService.exportAllCoursesData(token);
            if (urls.isEmpty()) {
                return ResponseEntity.status(404).body("No data found for batch: " + batch);
            }
            return ResponseEntity.ok("Sheets exported successfully for " + batch + "! URLs: " + urls);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
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
