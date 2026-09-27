# BridgeLabz Classroom Reports - Documentation

## Overview

The BridgeLabz Classroom Reports application is a full-stack web application that integrates with Google Classroom to fetch course data, visualize student submission statuses, and export reports in multiple formats. It provides educators with a comprehensive view of student progress across courses (referred to as "classrooms" in the UI) and assignment submissions.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Features](#features)
3. [How to Use](#how-to-use)
4. [Main Components](#main-components)
5. [Backend Workflow](#backend-workflow)
6. [Frontend Workflow](#frontend-workflow)
7. [API Endpoints](#api-endpoints)
8. [Code Changes](#code-changes)
9. [File Structure](#file-structure)
10. [Running the Application](#running-the-application)
11. [Environment Configuration](#environment-configuration)

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (Next.js)                      │
│  ── Port 3000                                               │
│  ── React 19 Components                                      │
│  ── API Proxy → /api/*                                      │
└───────────────┬──────────────────────────┬──────────────────┘
                │                          │
                ▼                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Backend (Spring Boot)                          │
│  ── Port 8084                                                  │
│  ── Google Classroom API Integration                            │
│  ── Google Drive API Integration                                │
│  ── OAuth2 Authentication (Google)                            │
└─────────────────────────────────────────────────────────────────┘
                │                          │
                ▼                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Google APIs                                    │
│  ── Google Classroom API (course data, students, coursework)    │
│  ── Google Drive API (spreadsheet creation)                     │
│  ── Google Sheets API (spreadsheet manipulation)               │
└─────────────────────────────────────────────────────────────────┘
```

---

## Features

| Feature | Description |
|---------|-------------|
| **OAuth2 Login** | Google-based authentication restricted to `@bridgelabz.com` emails |
| **Course Dashboard** | Responsive dashboard showing all accessible classrooms with active/archived state filtering |
| **Submission Matrix** | Table view showing student submission status (Submitted/Missing/Assigned) per assignment |
| **Course Selection** | Multi-select checkboxes to select individual or all classrooms |
| **Export - Google Sheets** | Backend exports all accessible courses (batched by B1/B2/B3/B4) or PCCOE courses to Google Sheets |
| **Export - CSV** | Download submission matrix as CSV (individual files or merged sections) |
| **Export - Excel** | Download submission matrix as .xlsx (individual files or merged workbook with tabs per course) |
| **Export - Word** | Download submission matrix as .docx (individual or merged) |
| **Toast Notifications** | Success/error/warning notifications for all user actions |
| **PCCOE Access Control** | Role-based access using email allowlist for PCCOE-specific reports |
| **Responsive Design** | Mobile-friendly layout with sticky sidebar and horizontal table scrolling |

---

## How to Use

### 1. Login

1. Navigate to `http://localhost:3000`
2. Click "Continue with Google"
3. Sign in with your BridgeLabz Google account
4. After authentication, you'll be redirected to the dashboard

### 2. Browsing Courses

1. The sidebar shows all classrooms you teach
2. Use the search box to filter by course name
3. Use the state filter dropdown to show All, Active, or Archived courses
4. Click "Show more" to expand the list beyond the first 5 courses
5. Click "Select all accessible classrooms" to select/unselect all courses

### 3. Viewing Submissions

1. Click on any course in the sidebar to view its submission matrix
2. The main panel shows a table with:
   - Student Name
   - Email
   - Overall Completion percentage
   - Individual assignment submission statuses (color-coded badges)

### 4. Exporting Data

#### Google Sheets Export
1. Click the "Export" button in the submission matrix header
2. Choose between "All Courses" or "PCCOE Only" tabs (PCCOE tab only visible if you have PCCOE access)
3. A Google Sheet will be created in your Google Drive with one tab per batch (B1/B2/B3/B4)
4. You'll receive a link to open the sheet

#### File Exports (CSV, Excel, Word)
1. Select classrooms using the checkboxes in the sidebar
2. Click the "Export" button in the submission matrix header
3. Choose export scope: "Individually" (separate files) or "Merge into one" (single combined file)
4. Select file format: Excel (.xlsx), Word (.docx), or CSV (.csv)
5. The file downloads immediately to your computer:
   - **Excel**: Merged exports include one worksheet tab per classroom
   - **CSV**: Merged exports use classroom section labels
   - **Word**: Merged exports create a single table combining all classrooms

---

## Main Components

### Backend Components

| Component | Package | Description |
|-----------|---------|-------------|
| `ClassroomController` | `com.bridgelabz.controller` | REST API endpoints for courses, students, coursework, submissions, and exports |
| `AuthController` | `com.bridgelabz.controller` | Authentication status endpoint returning user info and capabilities |
| `GoogleClassroomService` | `com.bridgelabz.service` | Core business logic for fetching and processing classroom data |
| `CourseService` | `com.bridgelabz.service` | Fetches courses from Google Classroom API with pagination |
| `StudentService` | `com.bridgelabz.service` | Fetches enrolled students for a given course |
| `CourseworkService` | `com.bridgelabz.service` | Fetches assignments/coursework titles for a course |
| `SubmissionService` | `com.bridgelabz.service` | Fetches and evaluates student submissions |
| `GoogleSheetsDriveService` | `com.bridgelabz.service` | Handles Google Sheets/Drive operations (create, update, manage permissions) |
| `BridgeLabzOAuth2UserService` | `com.bridgelabz.security` | OAuth2 user details service enforcing email domain restrictions |
| `SecurityConfig` | `com.bridgelabz.security` | Spring Security configuration for OAuth2 login, CORS, and session management |
| `PccoeAccessService` | `com.bridgelabz.security` | Email-based access control for PCCOE features |

### Frontend Components

| Component | Path | Description |
|-----------|------|-------------|
| `Dashboard` | `frontend/src/app/dashboard/page.js` | Main page orchestrating data fetching and component rendering |
| `Navbar` | `frontend/src/components/Navbar.js` | Top navigation bar with user info |
| `CourseList` | `frontend/src/components/CourseList.js` | Sidebar with searchable course list and multi-selection |
| `SubmissionsTable` | `frontend/src/components/SubmissionsTable.js` | Submission matrix table with export functionality |
| `LoadingBar` | `frontend/src/components/SubmissionsTable.js` | Top loading progress bar |
| `BridgeLabzLogo` | `frontend/src/components/BridgeLabzLogo.js` | Scalable brand logo component |
| `Dialog` | `frontend/src/components/Dialog.js` | Modal dialog for displaying export results |
| `ToastProvider` | `frontend/src/components/ToastProvider.js` | Toast notification system |
| `AppProviders` | `frontend/src/components/AppProviders.js` | Context providers wrapper |
| `Home` | `frontend/src/app/page.js` | Login page |
| `Register` | `frontend/src/app/register/page.js` | Registration page for restricted access |

---

## Backend Workflow

### Authentication Flow

```
1. User clicks "Continue with Google" on the frontend
2. Frontend redirects to /api/oauth2/authorization/google
3. Next.js rewrite proxies to backend: /oauth2/authorization/google
4. Backend redirects to Google OAuth consent screen
5. Google redirects back to backend: /login/oauth2/code/google
6. BridgeLabzOAuth2UserService loads user info
7. Check: user email must end with configured domain (default @bridgelabz.com)
8. If rejected → redirect to /register?error=access-denied
9. If approved → redirect to frontend /dashboard
10. OAuth2AuthorizedClient stores the token for API calls
```

### Course Fetching Workflow

```
1. Dashboard calls /api/classroom/courses (proxied to /classroom/courses)
2. ClassroomController.getAllCourses() gets OAuth token
3. CourseService.getAllCourses():
   a. Calls Google Classroom API: GET /v1/courses?teacherId=me&pageSize=100
   b. Paginates through all results using nextPageToken
   c. Returns all courses where user is a teacher
4. Returns JSON list of courses to frontend
```

### Submission Data Fetching Workflow

```
1. User selects a course in sidebar
2. SubmissionsTable.fetchSubmissions(courseId) calls /classroom/courses/{courseId}/submissions
3. ClassroomController.getCourseSubmissions():
   a. Fetch students: StudentService.getStudents(courseId, token)
      - GET /v1/courses/{courseId}/students
      - Maps userId → {name, email}
   b. Fetch coursework: CourseworkService.getCourseworkTitles(courseId, token)
      - GET /v1/courses/{courseId}/courseWork
      - Maps courseworkId → title
   c. Fetch submissions: SubmissionService.getSubmissions(courseId, token, coursework, students)
      - For each coursework, GET /v1/courses/{courseId}/courseWork/{cwId}/studentSubmissions
      - Maps submission state → status:
        - TURNED_IN, RETURNED → "Submitted"
        - CREATED, NEW → "Assigned"
        - Other → "Missing"
   d. Build response object:
      - courseworkTitles: list of all assignment titles
      - students: list of {userId, name, email, submissions: {title: status}, submittedCount, percentage}
      - totalStudents, totalCoursework
```

### Google Sheets Export Workflow (All Courses)

```
1. User clicks "Export" → "Google Sheets" in submissions table
2. handleExport() calls /classroom/all-student-submissions
3. ClassroomController.exportAllBatches():
   a. Gets OAuth token
   b. Calls GoogleClassroomService.exportAllCoursesData(token)
4. ExportAllCoursesData workflow:
   a. Fetch all courses (same as above)
   b. Filter to courses where user is teacher (courseState=ACTIVE && has teacherFolder)
   c. For each course, determine batch key (B1, B2, B3, B4, or Other_Batches)
      based on course name pattern (e.g., "B3P01" → "B3")
   d. Concurrent processing (4 threads):
      - Fetch students, coursework, submissions
      - Calculate submitted count and percentage
   e. Group by batch key
   f. For each batch, create Google Sheet:
      - One worksheet tab per course
      - Columns: Student Name, Email, Status per assignment, Completion %, Submitted Count
   g. Append summary sheet with batch statistics
5. Returns list of spreadsheet URLs
6. Frontend shows dialog with link to Google Sheet
```

### PCCOE Export Workflow

```
1. User with PCCOE access clicks "Export" → "PCCOE Only"
2. handleExport() calls /classroom/pccoe-cource
3. ClassroomController.exportPccoeCourses():
   a. Check canAccessPccoe(authToken) → PccoeAccessService.canAccess(email)
   b. If not allowed → 403 Forbidden
   c. If allowed → call GoogleClassroomService.exportPccoeCoursesData(token)
4. ExportPccoeCoursesData workflow:
   a. Fetch only PCCOE courses: CourseService.getCoursesByPrefix(token, "PCCOE")
      (case-sensitive, starts with "PCCOE")
   b. Concurrent processing (4 threads):
      - For each PCCOE course, fetch students, coursework, submissions
      - Build student data with email + comma-separated statuses
   c. Create single Google Sheet with one tab per PCCOE course
5. Returns single spreadsheet URL
6. Frontend shows dialog with link to Google Sheet
```

### File Export (CSV/Excel/DOCX) Workflow

```
1. User selects courses in sidebar (checkboxes)
2. User clicks "Export" → selects file format (CSV/Excel/Word)
3. runFileExport(format) is called:
   a. Check selectedIds.length > 0
   b. Call fetchSelectedReports():
      - For each selected course ID, call GET /classroom/courses/{id}/submissions
      - Use Promise.all to fetch all in parallel
      - Map course.id → course using coursesById
      - Return reports with courseName attached
   c. Based on exportScope:
      - "individual": Export each report as separate file
      - "merged" + "excel": Create workbook with one tab per course
      - "merged" + "csv": Create single CSV with classroom sections
      - "merged" + "docx": Create single DOCX with merged table
4. File is created client-side using:
   - XLSX library for Excel (xlsx)
   - docx library for Word (docx)
   - Manual CSV string building
5. File is downloaded using URL.createObjectURL() + anchor click
```

---

## Frontend Workflow

### Initial Load

```
1. App loads, checks auth status via /auth/status
2. If authenticated:
   a. Fetch courses from /classroom/courses
   b. Set first course as active (if exists)
   c. Set selectedCourseIds to [firstCourseId]
3. If not authenticated → redirect to /
```

### Course Selection

```
1. User clicks course in sidebar → setSelectedCourseId(courseId)
2. SubmissionsTable receives new courseId prop
3. useEffect triggers fetchSubmissions(courseId)
4. API call fetches full submission matrix for that course
5. Data is rendered in table
```

### Multi-Selection for Export

```
1. User clicks checkbox for course → toggleCourseSelection(courseId)
2. Selected IDs are sent to Dashboard via onSelectionChange
3. Dashboard updates selectedCourseIds state
4. Both CourseList and SubmissionsTable receive updated selectedCourseIds
5. Export button enables when selectedIds.length > 0
```

### Export Process

```
1. User opens export dropdown
2. Selects export scope (individual/merged)
3. Selects file format (Excel/DOCX/CSV)
4. runFileExport(format) is called:
   a. Sets isExporting = true (shows spinner on button)
   b. Closes dropdown
   c. Fetches fresh data for all selected courses
   d. Generates file based on format and scope
   e. Triggers download
   f. Shows success/error toast
   g. Resets isExporting = false
```

### PCCOE Toggle

```
1. If user has PCCOE access (user.capabilities.pccoeExport === true):
   a. Toggle button visible: "All Courses" / "PCCOE Only"
2. exportMode state controls which backend endpoint is used
3. Only affects Google Sheets export, not file exports
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/auth/status` | Check if user is authenticated, returns capabilities | Public |
| GET | `/oauth2/authorization/google` | Start Google OAuth flow | Public |
| GET | `/login/oauth2/code/google` | OAuth callback | Public |
| POST | `/logout` | Logout and invalidate session | Authenticated |

### Classroom Data

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/classroom/courses` | Get all courses user teaches (paginated) | Authenticated |
| GET | `/classroom/courses/{courseId}/students` | Get enrolled students for a course | Authenticated |
| GET | `/classroom/courses/{courseId}/coursework` | Get assignments for a course | Authenticated |
| GET | `/classroom/courses/{courseId}/submissions` | Get full submission matrix | Authenticated |
| GET | `/classroom/user-info` | Get current user info | Authenticated |

### Google Sheets Export

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/classroom/all-student-submissions` | Export ALL courses to Google Sheets (batched by B1/B2/B3/B4) | Authenticated |
| GET | `/classroom/pccoe-cource` | Export PCCOE courses to Google Sheets | Authenticated + PCCOE access |

### Response Format

All API responses follow this format:

```json
{
  "success": true,
  "message": "Description of what succeeded",
  "data": { ... }
}
```

Or on error:

```json
{
  "success": false,
  "message": "Error description"
}
```

---

## Code Changes

### Key Changes Made

#### 1. Duplicate Variable Fix (SubmissionsTable.js)
**Issue**: `selectedIds` and `coursesById` were defined twice in the same component scope, causing a build error.
**Fix**: Removed duplicate definitions at lines 62-63.

```javascript
// BEFORE (lines 22-23 and 62-63 - duplicate):
const selectedIds = selectedCourseIds.length > 0 ? selectedCourseIds : (courseId ? [courseId] : []);
const coursesById = new Map(courses.map((course) => [course.id, course]));

// (later in the component, duplicates removed)
```

#### 2. Robust Export Data Fetching
**Issue**: `fetchSelectedReports` used `Promise.all` which would fail completely if any single course fetch failed.
**Fix**: Rewrote to use sequential processing with try/catch per course, collecting errors but still returning successfully fetched reports.

```javascript
const fetchSelectedReports = async () => {
  const reports = [];
  const errors = [];
  
  for (const id of selectedIds) {
    try {
      const response = await api.get(`/classroom/courses/${id}/submissions`);
      if (!response.data.success) {
        errors.push({ id, error: response.data.message || "Failed to load classroom report." });
        continue;
      }
      const course = coursesById.get(id);
      reports.push({ ...response.data.data, courseName: course?.name || `Classroom ${id}`, courseId: id });
    } catch (err) {
      errors.push({ id, error: err.response?.data?.message || err.message || "An error occurred" });
    }
  }
  
  if (reports.length === 0) {
    throw new Error(errors.map(e => `${e.id}: ${e.error}`).join("; ") || "No reports could be fetched.");
  }
  
  if (errors.length > 0) {
    console.warn("Some reports failed to load:", errors);
  }
  
  return reports;
};
```

#### 3. Improved Data Validation
**Issue**: Export functions didn't check for empty or missing data.
**Fix**: Added validation checks in all export functions:
- Check `report?.students?.length` before processing
- Check `reports?.length` before merging
- Check `workbook.SheetNames.length` before writing Excel

#### 4. Merged DOCX Export Support
**Issue**: Merged DOCX export was not properly handled - it fell through to individual export.
**Fix**: Added explicit merged DOCX export path:

```javascript
if (exportScope === "merged" && format === "docx") {
  const mergedReport = mergeReports(reports);
  await exportDocxReport(mergedReport, "bridgeLabz-merged-submission-report.docx");
  notify("Merged DOCX report downloaded.", "success");
  return;
}
```

#### 5. Merged Export Data Consistency
**Issue**: `mergeReports` didn't include `courseId` in student objects.
**Fix**: Added `courseId: report.courseId` to student mapping:

```javascript
const students = reports.flatMap((report) => (report.students || []).map((student) => ({
  ...student,
  courseName: report.courseName,
  courseId: report.courseId,
})));
```

#### 6. Loading States and Button States
**Issue**: Export buttons weren't disabled during export, and `isExporting` wasn't checked in dropdown items.
**Fix**: Added `disabled={selectedIds.length === 0 || isExporting}` to all file export buttons.

#### 7. UI Clarity Improvements
**Issue**: Users didn't know Google Sheets export exports ALL courses, not just selected ones.
**Fix**: 
- Updated Google Sheets dropdown label with tooltip explaining behavior
- Updated success message to show what was exported
- Added clarifying small text in export scope section
- Changed "Merge into one" to "Merge into one (merged tabs)" for Excel

#### 8. CSV Encoding Fix
**Issue**: CSV files didn't specify charset, causing encoding issues.
**Fix**: Changed MIME type from `"text/csv"` to `"text/csv;charset=utf-8"`.

#### 9. Excel Empty Workbook Fix
**Issue**: If all reports in a merged export had no students, an empty workbook would be created.
**Fix**: Added check for `workbook.SheetNames.length === 0`.

#### 10. Lint Error Fixes
**Issue**: LoadingBar defined inside component (react-hooks/static-components), setState in effect warning.
**Fix**: Moved LoadingBar outside component, added eslint-disable for false positive.

---

## File Structure

```
google-classroom-report-generation/
├── src/
│   └── main/
│       └── java/com/bridgelabz/
│           ├── BridgeLabzClassroomReportsApplication.java
│           ├── controller/
│           │   ├── ClassroomController.java
│           │   ├── AuthController.java
│           │   └── RootController.java
│           ├── service/
│           │   ├── CourseService.java
│           │   ├── StudentService.java
│           │   ├── CourseworkService.java
│           │   ├── SubmissionService.java
│           │   ├── GoogleClassroomService.java
│           │   └── GoogleSheetsDriveService.java
│           ├── security/
│           │   ├── BridgeLabzOAuth2UserService.java
│           │   ├── SecurityConfig.java
│           │   └── PccoeAccessService.java
│           ├── model/
│           │   └── ResponseDTO.java
│           └── resources/
│               └── application.properties
├── frontend/
│   └── src/
│       ├── app/
│       │   ├── layout.js
│       │   ├── page.js
│       │   ├── globals.css
│       │   ├── dashboard/
│       │   │   └── page.js
│       │   └── register/
│       │       └── page.js
│       └── components/
│           ├── SubmissionsTable.js
│           ├── CourseList.js
│           ├── Navbar.js
│           ├── Dialog.js
│           ├── ToastProvider.js
│           ├── BridgeLabzLogo.js
│           │   └── public/bridgelabz-logo.png
│           └── AppProviders.js
│       └── lib/api.js
│       ├── next.config.js
│       ├── package.json
│       └── eslint.config.mjs
└── pom.xml (Maven build)
```

---

## Running the Application

### Prerequisites

- Java 17+
- Node.js 18+
- Maven
- Google Cloud Platform project with:
  - OAuth 2.0 Client ID configured
  - Google Classroom API enabled
  - Google Drive API enabled
  - Google Sheets API enabled

### Steps

1. **Configure Environment**
   ```bash
   # Copy env example
   cp .env.example .env
   
   # Set Google OAuth credentials
   export GOOGLE_CLIENT_ID="your-client-id"
   export GOOGLE_CLIENT_SECRET="your-client-secret"
   export GOOGLE_REDIRECT_URI="http://localhost:8084/login/oauth2/code/google"
   
   # Set PCCOE access (optional)
   export PCCOE_ALLOWED_EMAILS="owner@bridgelabz.com,lead@bridgelabz.com"
   ```

2. **Start Backend**
   ```bash
   cd google-classroom-report-generation
   ./mvnw spring-boot:run
   ```

3. **Start Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **Access Application**
   - Open `http://localhost:3000`
   - Click "Continue with Google"
   - Authenticate with your BridgeLabz account

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8084` | Backend server port |
| `BRIDGELABZ_EMAIL_DOMAIN` | `bridgelabz.com` | Email domain for OAuth access |
| `PCCOE_ALLOWED_EMAILS` | *(empty)* | Comma-separated emails for PCCOE access |
| `spring.security.user.name` | `admin` | Default username for basic auth |
| `spring.security.user.password` | *(none)* | Default password (not used with OAuth) |
| `NEXT_PUBLIC_API_BASE_URL` | `/api` | Frontend API proxy base URL |

---

## Environment Configuration

### Backend (`application.properties`)

```properties
# Server
server.port=8084

# OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.redirect-uri=${GOOGLE_REDIRECT_URI}

# Security logging
logging.level.org.springframework.security=INFO

# Session
spring.session.timeout=30m
```

### Frontend (`next.config.js`)

```javascript
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.BACKEND_URL || 'http://localhost:8084'}/:path*`,
      },
    ];
  },
};
```

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GOOGLE_CLIENT_ID` | Yes | OAuth 2.0 client ID |
| `GOOGLE_CLIENT_SECRET` | Yes | OAuth 2.0 client secret |
| `GOOGLE_REDIRECT_URI` | Yes | OAuth redirect URI |
| `PCCOE_ALLOWED_EMAILS` | No | Comma-separated emails with PCCOE access |
| `BRIDGELABZ_EMAIL_DOMAIN` | No | Organization domain (default: bridgelabz.com) |
| `BACKEND_URL` | No | Backend URL for Next.js proxy |
| `NEXT_PUBLIC_API_BASE_URL` | No | Frontend API base URL |

---

## Troubleshooting

### Build Errors

**Issue**: `the name 'coursesById' is defined multiple times`
**Cause**: Variable declared twice in same scope
**Fix**: Remove duplicate declarations

**Issue**: `Calling setState synchronously within an effect`
**Cause**: ESLint linter detects async setState calls in useEffect
**Fix**: Add `// eslint-disable-next-line react-hooks/set-state-in-effect` comment

### Export Issues

**Issue**: Exported files show "Missing" for all assignments
**Cause**: User doesn't have teacher access to the course
**Fix**: Only courses where `courseState=ACTIVE && has teacherFolder` are processed

**Issue**: Google Sheets export returns 403
**Fix**: Check PCCOE access is configured; user must be in `PCCOE_ALLOWED_EMAILS`

**Issue**: CSV file shows encoding issues
**Fix**: Ensure UTF-8 charset is set (now fixed)

### Authentication Issues

**Issue**: Redirect loop after login
**Fix**: Verify `NEXT_PUBLIC_API_BASE_URL` matches backend CORS configuration

**Issue**: "Session expired" error
**Fix**: OAuth token expired; reload page to re-authenticate

---

## Security Considerations

1. OAuth client secrets must never be committed to version control
2. PCCOE access is controlled by explicit email allowlist
3. OAuth email domain is validated server-side
4. CORS is restricted to `http://localhost:3000` in development
5. Sessions timeout after 30 minutes
6. Logout properly invalidates OAuth2 client and session

---

## Performance Optimizations

1. **Concurrent Processing**: Backend uses 4-thread pool for parallel course processing
2. **Pagination**: Course fetching handles Google Classroom API pagination
3. **Throttling**: API calls include throttle delays to respect rate limits
4. **Retry Logic**: Failed API calls are retried up to 4 times
5. **Client Caching**: CoursesById Map built only when courses change (useMemo)
6. **Conditional Renders**: Early returns for loading/error states to prevent unnecessary renders<tool_call>write<arg_key>content</arg_key><arg_value># BridgeLabz Classroom Reports - Documentation

Comprehensive documentation for the BridgeLabz Classroom Reports application.

For the full detailed documentation, see [DOCUMENTATION.md](./DOCUMENTATION.md).
