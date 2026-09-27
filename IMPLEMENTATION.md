# BridgeLabz Classroom Reports implementation

## What is implemented

- A reusable inline BridgeLabz brand mark is rendered in the login screen and dashboard navigation. It is resolution-independent and does not depend on a remote image URL.
- The supplied BridgeLabz artwork is stored as `frontend/public/bridgelabz-logo.png` and rendered with `next/image` in the header using responsive contain sizing and priority loading.
- The application uses a single light theme with a professional BridgeLabz palette: navy typography, blue primary actions, orange/yellow accents, and green success states.
- The dashboard header is full-viewport and keeps the high-resolution logo responsive without stretching. Summary cards use explicit light surfaces and contrast-safe typography, while report table headers use a light-blue background with white text.
- The dashboard uses a sticky desktop sidebar with a five-item compact view and an accessible “Show more” expansion for additional classrooms.
- The Next.js application now uses a responsive shell, mobile dashboard stacking, horizontal report scrolling, accessible focusable controls, and constrained dialogs/toasts that work on narrow viewports.
- `frontend/src/components/ToastProvider.js` provides success, error, and informational notifications. Export results and API failures are surfaced through it instead of console-only feedback.
- `frontend/src/components/Dialog.js` provides a modal with backdrop dismissal, close control, keyboard/screen-reader semantics, responsive sizing, and an optional result link.
- CSV, Excel, and DOCX report downloads are available from the submission matrix. Google Sheets remains available through the secured backend export endpoints.
- Accessible classrooms can be selected individually or all at once from the sidebar. CSV and DOCX exports support separate files or a merged classroom-labelled report. Merged Excel exports are delivered as one professional workbook with one filtered, frozen-header tab per selected classroom. Google Sheets continues to use the backend all-accessible/PCCOE export flow.
- The backend no longer writes classroom reports to the project directory. Local CSV, Excel, and DOCX files are created only after an authenticated user explicitly chooses an export in the dashboard. CSV is a flat format, so merged CSV uses clearly labelled classroom sections; worksheet tabs are provided by merged Excel workbooks.
- API calls use `frontend/src/lib/api.js` and the Next rewrite in `frontend/next.config.js`. The browser only needs port **3000**; `/api/*` is proxied to the Spring Boot service (8084 by default, configurable with `BACKEND_URL`).
- Google OAuth is enforced server-side by `BridgeLabzOAuth2UserService`. Accounts outside `@bridgelabz.com` are rejected before a session is created. The domain is configurable with `BRIDGELABZ_EMAIL_DOMAIN`.
- OAuth email checks normalize case and configuration formatting, require the Google account email to match the configured domain exactly, and return the user to the access-denied screen with a clear reason when the account is not eligible.
- Classroom loading now returns every classroom taught by the authenticated user across all Google Classroom pages; it no longer hides classrooms based on their name.
- PCCOE access is controlled independently by an explicit `PCCOE_ALLOWED_EMAILS` comma-separated environment variable. The capability is returned for the signed-in user, the PCCOE filter is hidden for everyone else, and both PCCOE backend endpoints return `403 Forbidden` when called without permission.
- `PccoeAccessServiceTest` covers case-insensitive allowlist matching, missing/unknown users, and the secure empty-configuration default.
- `/register` is the registration/access-restricted experience. Registration is intentionally Google-based, so there are no local passwords or unverified duplicate user records.

## Changed files

### Frontend

- `frontend/src/components/BridgeLabzLogo.js`: high-quality scalable logo component.
- `frontend/src/components/AppProviders.js`, `ToastProvider.js`, `Dialog.js`: shared feedback and modal primitives.
- `frontend/src/lib/api.js`: credentialed API client with a same-origin `/api` base.
- `frontend/next.config.js`: port-3000 proxy to the Spring Boot backend.
- `frontend/src/app/layout.js`: product metadata and global providers.
- `frontend/src/app/page.js`: branded Google sign-in.
- `frontend/src/app/register/page.js`: registration and rejected-account UX.
- `frontend/src/app/dashboard/page.js`, `Navbar.js`, `SubmissionsTable.js`, `globals.css`: responsive dashboard, branded navigation, toasts, dialogs, and four local export formats.
- `frontend/src/components/CourseList.js`: searchable classroom list with active/archived state filtering.

### Backend

- `src/main/java/com/bridgelabz/security/BridgeLabzOAuth2UserService.java`: authoritative email-domain check.
- `src/main/java/com/bridgelabz/security/SecurityConfig.java`: OAuth user-service wiring and frontend success/failure redirects.
- `src/main/resources/application.properties`: configurable allowed domain and production-safe security logging level.
- `src/main/java/com/bridgelabz/security/SecurityConfig.java`: session invalidation, cookie deletion, and redirect-on-logout configuration.

## Running

1. Copy `.env.example` to a secret-managed environment (do not commit `.env`) and set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and `GOOGLE_REDIRECT_URI`. The OAuth callback defaults to `http://localhost:8084/login/oauth2/code/google` for local development.
2. Set PCCOE access, for example in PowerShell: `$env:PCCOE_ALLOWED_EMAILS="owner@bridgelabz.com,lead@bridgelabz.com"`.
3. Start Spring Boot on 8084: `./mvnw spring-boot:run` (or `mvnw.cmd spring-boot:run` on Windows).
4. Start the frontend on 3000: `cd frontend`, then `npm run dev`.
5. Open `http://localhost:3000`. The browser talks only to port 3000; the Next rewrite forwards API and OAuth requests.

Both processes must be running before clicking **Continue with Google**. If the backend is stopped, Next cannot proxy `/api/oauth2/authorization/google` and the browser will show an internal-server-error response. Verify the backend first with `http://localhost:8084/auth/status`; an unauthenticated backend should return JSON with `authenticated: false`.

For deployment, set `BACKEND_URL`, `NEXT_PUBLIC_API_BASE_URL` only when an external API origin is required, and `BRIDGELABZ_EMAIL_DOMAIN` when the organization domain changes. Do not commit OAuth client secrets. The previously committed OAuth secret must be revoked and rotated in Google Cloud Console because it has been exposed in repository history.
