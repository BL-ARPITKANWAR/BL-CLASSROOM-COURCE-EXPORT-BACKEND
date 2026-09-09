Google Classroom Grade & Submission Exporter
Automated Student Engagement & Submission Reporting System

A Spring Boot application that integrates with Google Classroom, Google Sheets, and Google Drive to automatically collect student, coursework, and submission information and generate structured reports.

1. Overview

The Google Classroom Grade & Submission Exporter automates the process of collecting student engagement and assignment submission information from Google Classroom.

Instead of manually checking multiple courses, students, assignments, and submissions, the application automatically:

Google Classroom
       ↓
    Courses
       ↓
    Students
       ↓
   Coursework
       ↓
  Submissions
       ↓
 Data Processing
       ↓
 ┌──────────────┬───────────────┐
 │              │               │
CSV Reports   Google Sheets   Google Drive

The generated reports can be used by educators, training teams, administrators, and management to monitor student activity and submission status.

2. Key Features
Feature	Description
Google OAuth2	Secure authentication using Google
Course Management	Fetch courses from Google Classroom
Student Data	Retrieve enrolled students
Coursework	Fetch assignments and coursework
Submission Tracking	Track student submission status
Batch Organization	Organize courses into batches such as B1, B2, B3 and B4
Google Sheets	Generate structured spreadsheets
Google Drive	Store generated reports
CSV Export	Generate local CSV backup files
Error Handling	Handle API and application errors
WebClient	Communicate with Google APIs
Modular Architecture	Separate controller, service, model and utility layers
3. Architecture
                         ┌─────────────────┐
                         │      USER       │
                         │     Browser     │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ Spring Security │
                         │    OAuth2       │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │   Controller    │
                         │ Classroom       │
                         │ Controller      │
                         └────────┬────────┘
                                  │
                                  ▼
                    ┌───────────────────────────┐
                    │         SERVICES          │
                    │                           │
                    │ GoogleClassroomService    │
                    │ GoogleSheetsDriveService  │
                    │ CourseService              │
                    │ StudentService             │
                    │ CourseworkService          │
                    │ SubmissionService          │
                    └─────────────┬─────────────┘
                                  │
                 ┌────────────────┼────────────────┐
                 │                │                │
                 ▼                ▼                ▼
          ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
          │   Google    │  │   Google    │  │   Google    │
          │  Classroom  │  │   Sheets    │  │    Drive    │
          │     API     │  │     API     │  │     API     │
          └─────────────┘  └─────────────┘  └─────────────┘
                 │                │                │
                 └────────────────┼────────────────┘
                                  ▼
                         ┌─────────────────┐
                         │ Generated       │
                         │ Reports         │
                         │                 │
                         │ Google Sheets   │
                         │ CSV Files       │
                         └─────────────────┘
4. Application Workflow
START
  │
  ▼
Open Application
  │
  ▼
Google Login
  │
  ▼
OAuth2 Authorization
  │
  ▼
Access Token
  │
  ▼
Fetch Courses
  │
  ▼
Fetch Students
  │
  ▼
Fetch Coursework
  │
  ▼
Fetch Submissions
  │
  ▼
Process & Organize Data
  │
  ├───────────────┐
  ▼               ▼
CSV Report    Google Sheets
                  │
                  ▼
             Google Drive
                  │
                  ▼
             Export Complete
5. Google Classroom Integration

The application communicates with Google Classroom to retrieve:

Courses
Students
Coursework
Student submissions

The application uses the authenticated user's Google access token to make authorized API requests.

6. Batch Organization

Courses are organized based on their course names.

Supported batches:

B1
B2
B3
B4
Other_Batches

Example:

Courses
   │
   ├── B1
   │    └── Courses
   │
   ├── B2
   │    └── Courses
   │
   ├── B3
   │    └── Courses
   │
   ├── B4
   │    └── Courses
   │
   └── Other_Batches

This allows reports to be organized according to training batches.

7. Report Structure

A generated report contains student information and coursework submission statuses.

Example:

Student Name	Email	Assignment 1	Assignment 2	Assignment 3
Student 1	student1@example.com	Submitted	Missing	Submitted
Student 2	student2@example.com	Submitted	Submitted	Assigned
Student 3	student3@example.com	Missing	Submitted	Submitted
Submission Statuses
Status	Meaning
Submitted	Student submitted the coursework
Assigned	Coursework assigned but not submitted
Missing	Submission is missing
Returned	Submission has been returned
8. Technology Stack
Java 17+
     │
     ▼
Spring Boot
     │
     ├── Spring Security OAuth2
     │
     ├── Spring WebClient
     │
     └── Application Services
             │
             ▼
       Google APIs
       ├── Classroom API
       ├── Sheets API
       └── Drive API
             │
             ▼
       OpenCSV
Technologies
Java 17+
Spring Boot
Spring Security OAuth2
Spring WebClient
Google Classroom API
Google Sheets API
Google Drive API
OpenCSV
Maven
9. Project Structure
google-classroom-report-generation/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── bridgelabz/
│       │           │
│       │           ├── Success2Application.java
│       │           │
│       │           ├── controller/
│       │           │   └── ClassroomController.java
│       │           │
│       │           ├── service/
│       │           │   ├── GoogleClassroomService.java
│       │           │   ├── GoogleSheetsDriveService.java
│       │           │   ├── CourseService.java
│       │           │   ├── StudentService.java
│       │           │   ├── CourseworkService.java
│       │           │   └── SubmissionService.java
│       │           │
│       │           ├── model/
│       │           │   └── StudentInfo.java
│       │           │
│       │           ├── csvUtiles/
│       │           │   └── CSVWriterUtil.java
│       │           │
│       │           ├── security/
│       │           │   └── SecurityConfig.java
│       │           │
│       │           ├── error/
│       │           │   └── ErrorController.java
│       │           │
│       │           └── handler/
│       │               └── OAuth2ErrorController.java
│       │
│       └── resources/
│           └── application.properties
│
├── .gitignore
├── pom.xml
└── README.md
10. Important Components
ClassroomController

Responsible for receiving application requests and triggering the Classroom report generation process.

GoogleClassroomService

The core service responsible for interacting with Google Classroom.

Responsibilities:

Fetch courses
Fetch students
Fetch coursework
Fetch submissions
Organize Classroom data
Prepare data for reporting
GoogleSheetsDriveService

Responsible for Google Sheets and Google Drive operations.

Responsibilities:

Create spreadsheets
Create worksheets
Write report data
Manage Drive folders
Store generated reports
CSVWriterUtil

Responsible for generating local CSV files.

SecurityConfig

Configures Spring Security and Google OAuth2 authentication.

StudentInfo

Represents student information used during report generation.

11. Google Cloud Configuration
Step 1 — Create Google Cloud Project

Open Google Cloud Console.

Navigate to:

APIs & Services
       ↓
Library

Create or select the required Google Cloud project.

Step 2 — Enable APIs

Enable:

Google Classroom API
Google Sheets API
Google Drive API
Step 3 — Create OAuth2 Credentials

Navigate to:

APIs & Services
       ↓
Credentials
       ↓
Create Credentials
       ↓
OAuth Client ID

Select:

Web Application

Configure the redirect URI:

http://localhost:8084/login/oauth2/code/google

Google provides:

Client ID
Client Secret
12. OAuth2 Scopes

The application requires access to Google Classroom, Sheets and Drive resources.

Example scopes:

openid
profile
email

Google Classroom
├── classroom.courses.readonly
├── classroom.rosters.readonly
├── classroom.coursework.students.readonly
├── classroom.student-submissions.students.readonly
└── classroom.profile.emails

Google Sheets
└── spreadsheets

Google Drive
└── drive.file

Security: Never commit the actual Google Client ID or Client Secret to GitHub.

Use environment variables instead.

13. Application Configuration

File:

src/main/resources/application.properties

Example:

spring.application.name=Success2

server.port=8084

spring.codec.max-in-memory-size=50MB

spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}

spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8084/login/oauth2/code/google

spring.security.oauth2.client.registration.google.client-name=Google

spring.security.oauth2.client.registration.google.authorization-grant-type=authorization_code

spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/auth

spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token

spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v3/userinfo
14. Environment Variables
Windows CMD
set GOOGLE_CLIENT_ID=your-client-id
set GOOGLE_CLIENT_SECRET=your-client-secret
Linux / macOS
export GOOGLE_CLIENT_ID=your-client-id
export GOOGLE_CLIENT_SECRET=your-client-secret
15. Running the Application
Clone Repository
git clone <repository-url>
cd google-classroom-report-generation
Build
mvn clean install
Start
mvn spring-boot:run

Application:

http://localhost:8084
16. Using the Application
Step 1 — Open
http://localhost:8084
Step 2 — Login

Sign in with a Google account that has access to the required Classroom courses.

Step 3 — Grant Permissions

Grant the required Classroom, Sheets and Drive permissions.

Step 4 — Generate Reports

The application retrieves:

Courses
   ↓
Students
   ↓
Coursework
   ↓
Submissions
Step 5 — View Reports

Reports are generated in:

Google Drive
   └── Google Sheets

Project Directory
   └── CSV Files
17. Main API Endpoint
GET /classroom/all-student-submissions
Processing Flow
GET /classroom/all-student-submissions
                │
                ▼
       Fetch Classroom Data
                │
                ▼
          Process Data
                │
         ┌──────┴──────┐
         ▼             ▼
       CSV         Google Sheets
                       │
                       ▼
                  Google Drive

Successful execution returns:

All Sheets exported successfully!
18. Error Handling

The application handles common authentication, API and application errors.

HTTP Status	Description
401	Unauthorized / Invalid OAuth2 token
404	Requested resource not found
500	Internal server / Google API error
503	Google API temporarily unavailable

For temporary Google API failures such as 503 Service Unavailable, retry logic with exponential backoff can be used.

Example:

Attempt 1
   ↓
503
   ↓
Wait
   ↓
Attempt 2
   ↓
503
   ↓
Wait
   ↓
Attempt 3
19. Troubleshooting
Invalid OAuth2 Credentials

Check:

Client ID
Client Secret
Redirect URI
Google Cloud OAuth configuration

The redirect URI must exactly match:

http://localhost:8084/login/oauth2/code/google
403 Insufficient Permission

If you receive:

ACCESS_TOKEN_SCOPE_INSUFFICIENT

Verify that the required Google API scopes are configured.

After changing scopes, authenticate again and grant the updated permissions.

Google Classroom API Error

Verify:

Google Classroom API is enabled
Authenticated user has access to the required courses
OAuth scopes are configured correctly
Google Sheets / Drive Error

Verify:

Google Sheets API is enabled
Google Drive API is enabled
Required OAuth scopes are present
Authenticated user has access to the destination Drive
Memory Issue

The application uses:

spring.codec.max-in-memory-size=50MB

This can be increased when processing very large API responses.

20. Security

Never commit the following to GitHub:

Client Secrets
Access Tokens
Refresh Tokens
Passwords
API Keys
Private Credentials

Use environment variables:

spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}

spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}

Recommended .gitignore entries:

target/
*.class
.env
application-local.properties
21. Future Enhancements

The system can be extended with:

Date-based course filtering
Submission status filtering
Student performance dashboard
Email notifications
Excel export
Scheduled report generation
Student engagement analytics
Admin dashboard
Report download functionality
Export history
Automated periodic synchronization
22. Use Cases
Educators

Track student assignments and submission status.

Training Organizations

Generate batch-wise student engagement reports.

Administrators

Monitor multiple Classroom courses from centralized reports.

Management

Analyze student participation and coursework completion.

23. Project Benefits
Google Classroom
       ↓
Automated System
       ↓
┌───────────────┬──────────────┬───────────────┐
│               │              │
Courses      Students      Submissions
│               │              │
└───────────────┴──────────────┘
                 ↓
           Data Processing
                 ↓
        ┌────────┴────────┐
        ▼                 ▼
 Google Sheets           CSV
        │                 │
        ▼                 ▼
 Google Drive        Local Backup
Key Advantages
Reduces manual reporting effort
Produces structured reports
Organizes data batch-wise
Automatically stores reports in Google Drive
Provides CSV backups
Uses OAuth2 authentication
Provides modular architecture
Easy to extend with additional reporting features
24. Conclusion

The Google Classroom Grade & Submission Exporter provides an automated solution for collecting and reporting student engagement and submission data.

The system integrates:

Java
  +
Spring Boot
  +
Spring Security OAuth2
  +
Google Classroom API
  +
Google Sheets API
  +
Google Drive API
  +
WebClient
  +
OpenCSV

The final workflow is:

Google Classroom
       ↓
     Fetch
       ↓
     Process
       ↓
     Report
       ↓
Google Sheets / CSV
       ↓
Google Drive

The modular architecture allows the application to be extended with dashboards, analytics, Excel exports, scheduled reports and additional Google Workspace integrations.

Built with Java & Spring Boot

Google Classroom → Process → Report → Google Drive
