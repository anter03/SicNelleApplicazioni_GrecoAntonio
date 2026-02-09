# Plan for Phase 2: Secure User Authentication & Session Management

**Goal**: Users can securely register, log in, and maintain a secure session with the application.

---
## Tasks

<task>
<name>Implement PBKDF2 Password Hashing (AUTH-01)</name>
<action>
Implement password hashing using PBKDF2 with HmacSHA256. Generate a 16-byte salt using `java.security.SecureRandom` for each user and store it in the database. Use 600,000 iterations for PBKDF2.
</action>
<files>
- `src/main/java/.../security/PasswordUtil.java` (for hashing/verification logic)
- `src/main/java/.../repository/UserRepository.java` (for storing hashed password and salt)
- Database schema migration script (to add salt column to users table)
</files>
<verify>
- Ensure new user registrations store hashed passwords with unique salts.
- Verify successful login using correct passwords.
- Verify failed login using incorrect passwords.
- Conduct a code review to confirm adherence to specified PBKDF2 parameters.
</verify>
<done>
PBKDF2 password hashing with secure salt management is implemented and integrated into user registration and login.
</done>
</task>

<task>
<name>Secure In-Memory Credential Management (AUTH-02)</name>
<action>
Refactor Java code to handle all sensitive password data as `char[]` instead of `String`. Implement explicit clearing of `char[]` buffers using `java.util.Arrays.fill(charArray, '\0')` immediately after use (e.g., after hashing, after comparison). Ensure methods for authentication accept `char[]` and use `Arrays.equals()` for comparison.
</action>
<files>
- `src/main/java/.../service/LoginService.java`
- `src/main/java/.../security/PasswordUtil.java`
- Any Servlet or utility handling password input (e.g., `LoginServlet`)
</files>
<verify>
- Conduct a thorough code review to ensure `String` objects are never used for sensitive password data, and `char[]` are correctly cleared.
- If feasible in a test environment, attempt memory dumps to confirm password data is not lingering.
</verify>
<done>
Secure in-memory management of credentials using `char[]` and explicit clearing is implemented across the application.
</done>
</task>

<task>
<name>Implement Secure Session Cookie Configuration (AUTH-03)</name>
<action>
Configure `webapp/WEB-INF/web.xml` to set `HttpOnly=true` and `Secure=true` for all `JSESSIONID` cookies. Set the session timeout to 15 minutes. Configure the `SameSite=Strict` attribute for session cookies in Tomcat's `context.xml` (or `web.xml` if supported by the Servlet container version).
</action>
<files>
- `webapp/WEB-INF/web.xml`
- `apache-tomcat/conf/context.xml`
</files>
<verify>
- Use browser developer tools to inspect `JSESSIONID` cookies after login, verifying `HttpOnly`, `Secure`, and `SameSite=Strict` attributes are present.
- Test session timeout by leaving the application inactive for more than 15 minutes and verifying re-authentication is required.
</verify>
<done>
Secure session cookie attributes (`HttpOnly`, `Secure`, `SameSite=Strict`) and a 15-minute session timeout are configured.
</done>
</task>

<task>
<name>Implement Session ID Regeneration (AUTH-04)</name>
<action>
Integrate `request.changeSessionId()` into the login process. This method must be called immediately after successful user authentication to generate a new session ID and prevent session fixation attacks.
</action>
<files>
- `src/main/java/.../servlet/LoginServlet.java` (or authentication handler)
</files>
<verify>
- Attempt a session fixation attack: acquire a session ID before authentication, then authenticate and try to use the original session ID. The original ID should be invalidated, and a new one generated.
- Verify that session attributes (e.g., user ID) are preserved across `changeSessionId()`.
</verify>
<done>
Session ID regeneration using `request.changeSessionId()` is implemented post-authentication.
</done>
</task>

<task>
<name>Implement User Enumeration Prevention (RF1, RF8)</name>
<action>
Modify the `LoginServlet` and `RegistrationServlet` to return generic error messages for all authentication and registration failures, preventing user enumeration. Implement an artificial `sleep` delay before returning feedback to mitigate timing attacks.
</action>
<files>
- `src/main/java/.../servlet/LoginServlet.java`
- `src/main/java/.../servlet/RegistrationServlet.java`
- `src/main/webapp/login.jsp`, `src/main/webapp/register.jsp` (for displaying generic messages)
</files>
<verify>
- Test login with non-existent usernames and incorrect passwords; verify the same generic error message is displayed for both.
- Test registration with an already-registered email; verify a neutral feedback or an obscured flow that does not confirm email existence.
- Observe consistent response times for different login/registration failure scenarios.
</verify>
<done>
User enumeration prevention via generic error messages and timing attack mitigation is implemented for login and registration.
</done>
</task>

<task>
<name>Implement Account Lockout Policy</name>
<action>
Implement an account lockout mechanism that locks a user account for 15-30 minutes after 5 consecutive failed login attempts. Log all lockout events internally with anonymized user IDs.
</action>
<files>
- `src/main/java/.../service/LoginService.java`
- `src/main/java/.../repository/UserRepository.java` (to store failed attempts/lockout status)
- Database schema migration script (to add lockout-related columns)
</files>
<verify>
- Test exceeding 5 failed login attempts for a user and verify the account is locked.
- Verify the account automatically unlocks after the specified duration.
- Check internal logs for lockout events (with anonymized IDs).
</verify>
<done>
Account lockout policy (5 attempts, 15-30 min duration) is implemented with internal logging.
</done>
</task>

<task>
<name>Implement Rate Limiting for Login Attempts</name>
<action>
Develop and integrate a Java `Servlet Filter` (`RateLimitingFilter.java`) to apply rate limiting to `LoginServlet` requests. Enforce thresholds of 10 attempts/min per IP and 3 attempts/min per username. Respond with HTTP 429 (Too Many Requests) if thresholds are exceeded.
</action>
<files>
- `src/main/java/.../filter/RateLimitingFilter.java`
- `webapp/WEB-INF/web.xml` (for filter mapping)
</files>
<verify>
- Test exceeding IP-based rate limits for login attempts and verify HTTP 429 responses.
- Test exceeding username-based rate limits for login attempts and verify HTTP 429 responses.
</verify>
<done>
Rate limiting (per IP and per username) for login attempts is implemented via a Servlet Filter.
</done>
</task>

<task>
<name>Implement Secure Registration Measures (RF1)</name>
<action>
Integrate CAPTCHA into the registration form. Implement an email verification system (Double Opt-In) where a unique token is sent for account activation. Enforce a password policy (min. 12 chars, complexity: uppercase, numbers, symbols) and use `PreparedStatement` for all registration input.
</action>
<files>
- `src/main/java/.../servlet/RegistrationServlet.java`
- `src/main/webapp/register.jsp` (CAPTCHA integration, client-side validation hints)
- `src/main/java/.../service/EmailService.java` (for email verification)
- `src/main/java/.../security/PasswordPolicyValidator.java`
</files>
<verify>
- Test registration with correct/incorrect CAPTCHA.
- Test account activation via email verification token.
- Test passwords that fail/pass the complexity policy.
- Test SQL injection attempts in registration fields.
</verify>
<done>
CAPTCHA, Double Opt-In email verification, password policy, and `PreparedStatement` input sanitization are implemented for user registration.
</done>
</task>

<task>
<name>Implement Application-Wide Security Measures</name>
<action>
Implement server-side Regex validation for all input fields (Zero Trust Input). Ensure all JSP output is encoded via JSTL's `<c:out>` or similar mechanisms (XSS Protection). Add `X-Frame-Options: SAMEORIGIN` header in `web.xml` or via a Servlet Filter (Clickjacking Defense). Ensure all application code adheres to minimum visibility (private) for methods and variables (Scope Reduction). Implement a global `Servlet Filter` for centralized session control (TA6).
</action>
<files>
- `src/main/java/.../util/ValidationUtil.java` (for regex validation)
- All `.jsp` files (for JSTL encoding)
- `webapp/WEB-INF/web.xml` (for `X-Frame-Options` header filter, global session control filter)
- All Java classes (code review for visibility)
- `src/main/java/.../filter/SessionControlFilter.java`
</files>
<verify>
- Test input fields with invalid data against Regex patterns.
- Test XSS payloads in all user-facing output; verify encoding prevents execution.
- Check HTTP response headers for `X-Frame-Options: SAMEORIGIN`.
- Code review for adherence to visibility principles.
- Verify global session control filter correctly handles authenticated/unauthenticated requests.
</verify>
<done>
Application-wide security measures including input validation, XSS protection, Clickjacking defense, scope reduction, and centralized session control are implemented.
</done>
</task>

---
*Created: Monday, February 9, 2026*
