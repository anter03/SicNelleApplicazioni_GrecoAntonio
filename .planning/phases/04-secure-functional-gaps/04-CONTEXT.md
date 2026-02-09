# Phase 4 Context: Functional Gaps & Critical Vulnerabilities

## Goal
To address critical functional vulnerabilities as per the exam requirements, without reintroducing CAPTCHA or email verification. The project now has a solid security foundation (BCrypt, clean SQL Schema, passing build).

## Key Implementation Decisions

### Registration Uniqueness (Task 1 / RF1)
*   **Decision**: `RegistrationService` will perform `findByUsername(username)` and `findByEmail(email)` checks before saving a new user. If either exists, `register` will return `false`.
*   **Decision (RF8 Generic Error Message)**: The `RegistrationServlet` will display "Impossibile completare la registrazione. I dati inseriti non sono validi o sono già associati a un account" for duplicate registration attempts.

### Home Page Access Protection (Task 2 / RF3 / TA6)
*   **Decision (User Identifiers in Session)**: Upon successful login, the `userId` (for database queries) and `email` (for display purposes) will be stored in the HTTP session. `username` will also be stored.
*   **Decision (Authentication Filter Configuration)**: An `AuthenticationFilter` will be configured in `web.xml` to protect *all* URLs (`/*`) except:
    *   `/login` servlet
    *   `/register` servlet
    *   `/login.jsp`
    *   `/register.jsp`
    *   Static resources in `/css/`
*   **Decision (Session Fixation Mitigation)**: After successful authentication in `LoginServlet`, `request.changeSessionId()` will be used to mitigate Session Fixation attacks (requires Servlet 3.1+).

### User File Display (Task 3 / RF6)
*   **Decision (UI/UX)**: The Home Page will display the user's files in a **Table** format.
*   **Decision (ContentRepository Extension)**: The `ContentRepository` must implement a new method: `findByUserId(Long userId)` to retrieve content specific to the logged-in user. File names will be displayed using `<c:out>` for XSS prevention.

### Session Termination (Task 4 / RF7)
*   **Decision (Logout Process)**: The `LogoutServlet` will invalidate the HTTP session (`session.invalidate()`), remove session cookies (e.g., `JSESSIONID`) by setting their max age to 0 and re-adding them to the response.
*   **Decision (No-Cache Headers)**: After logout, the following HTTP headers will be set on the response to prevent caching: `Cache-Control: no-cache, no-store, must-revalidate`, `Pragma: no-cache`, `Expires: 0`.
*   **Decision (Cookie Flags RF4)**: Session cookies must be configured with `HttpOnly` and `Secure` flags, either in `web.xml` or programmatically. (Note: These flags are already configured in `web.xml`).

## Rigid Constraints (from user)
*   Maintain the current SQL schema for the user.
*   Do not add unnecessary external libraries.
*   Do not reintroduce CAPTCHA.
*   Do not reintroduce email verification.

## Unresolved Questions / Deferred Ideas
None at this point, as all identified gray areas have been clarified.
