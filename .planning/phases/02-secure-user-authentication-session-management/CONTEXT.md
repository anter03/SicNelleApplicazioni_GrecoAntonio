# Context for Phase 2: Secure User Authentication & Session Management

This document captures key decisions and clarifications for Phase 2 of the "SicNelleApplicazioni" project. These decisions will guide subsequent research and planning activities, ensuring that implementation aligns with the user's vision for security and functionality in user authentication and session management.

## 1. Robust Password Handling

### KDF Algorithm
-   **Decision:** Use PBKDF2 (Password-Based Key Derivation Function 2) with HmacSHA256 as the core.
-   **Rationale:** Native Java support (`javax.crypto.SecretKeyFactory`), avoids heavy external libraries, and provides brute-force resistance through key stretching.

### Salt Generation and Storage
-   **Decision (Length):** 16 bytes (128 bits).
-   **Decision (Generation):** Use `java.security.SecureRandom` for cryptographically secure output.
-   **Decision (Storage):** Store salt directly in the database, in a dedicated unique column for each user, to prevent Rainbow Table attacks.

### Iterations and Work Factor
-   **Decision (PBKDF2 Iterations):** 600,000 iterations (following current OWASP recommendations). This value will be configurable via a properties file for future adjustments.
-   **Rationale:** Aims for a calculation time of approximately 200-500ms per login attempt to slow down offline attacks without significantly impacting user experience.

### Password Changes and Resets
-   **Decision (New Passwords):** Generate a new, cryptographically secure salt for every password change or reset. Never reuse old salts.
-   **Decision (Security in Process):** Manage old and new passwords as `char[]` and immediately clear them with `Arrays.fill()` after the operation.
-   **Decision (Error Feedback - RF8):** For password verification failures, return a generic error message ("Credenziali non valide. Riprova."). Log failed attempts internally with anonymized user ID and timestamp for monitoring (TA1, TA2).

## 2. In-Memory Credential Management

### Critical Lifecycles for Clearing
-   **Decision:** Explicitly overwrite `char[]` immediately after use.
-   **Specific Points:**
    *   Immediately after authentication/comparison with the database or login service.
    *   Immediately after password hashing, clear the original `char[]` buffer.
    *   Immediately after processing sensitive data read from external sources (e.g., files, Vault).

### Immediate Input Conversion
-   **Decision:** Convert sensitive input from `request.getParameter()` to `char[]` immediately using `.toCharArray()`.
-   **Decision:** Never assign passwords or other secrets to `String` variables.
-   **Decision:** When reading from streams (files, network), prefer methods that write directly to pre-allocated `char[]` or `byte[]` to avoid intermediate string creation.

### Centralized Management and Clearing
-   **Decision:** Implement utility methods (e.g., `clearPassword()`, `clearData()`) within sensitive data handling classes.
-   **Method:** Centralize clearing logic using `java.util.Arrays.fill(buffer, '\0')`.

### Preventing Implicit Conversion
-   **Decision (Method Signatures):** Credential management methods must accept `char[]` parameters (e.g., `verify(String username, char[] password)`).
-   **Decision (Secure Comparisons):** Use `java.util.Arrays.equals(passwordA, passwordB)` for comparing password `char[]`.
-   **Decision (API Restrictions):** Avoid `new String(charArray)` unless strictly necessary, and if used, follow with immediate clearing of the `String` object.
-   **Decision (Logging/Debug):** Prohibit passing sensitive `char[]` to logging or printing methods to prevent implicit `toString()` calls and exposure in logs.

## 3. Secure Session Cookie Configuration

### Cookie Attributes and Enforcement
-   **Decision:** Use declarative configuration in `web.xml` for the `JSESSIONID` cookie.
-   **Attributes:**
    *   `HttpOnly`: `true` (mitigates XSS).
    *   `Secure`: `true` (forces transmission over HTTPS only).
    *   `SameSite`: `Strict` (maximizes CSRF protection; configured at Tomcat level in `context.xml` via `CookieProcessor`).
-   **`Max-Age`:** Not set (defaults to "Session" so cookie is removed on browser close).
-   **`web.xml` configuration snippet:**
    ```xml
    <session-config>
        <session-timeout>15</session-timeout>
        <cookie-config>
            <http-only>true</http-only>
            <secure>true</secure>
            <tracking-mode>COOKIE</tracking-mode>
        </cookie-config>
    </session-config>
    ```

### Session ID Generation and Fixation Prevention
-   **Decision (Entropy):** Tomcat's default session ID generation using `SecureRandom` (128-bit) is deemed sufficient.
-   **Decision (Session Fixation):** Implement immediate regeneration of the Session ID after successful authentication.
    *   **Preferred Method (Servlet 3.1+):** `request.changeSessionId()`.
    *   **Alternative:** `session.invalidate()` followed by `request.getSession(true)`.

### Timeout and Invalidation Policy
-   **Decision (Timeout):** Set inactivity timeout to 15 minutes (`<session-timeout>15</session-timeout>`) to reduce exposure window.
-   **Decision (Explicit Invalidation):** `LogoutServlet` must explicitly invoke `session.invalidate()`.
-   **Decision (Cleanup):** Ensure no sensitive data remains in session attributes after logout; Tomcat handles expired session cleanup automatically.

### SameSite Attribute Considerations
-   **Decision:** Use `SameSite=Strict` to maximize CSRF protection, accepting that external links will require re-login if no active same-site session.

## 4. User Authentication Flow Security

### User Enumeration Prevention (RF1, RF8)
-   **Decision (Login):** Return a generic error message ("Credenziali non valide. Riprova.") for both non-existent users and incorrect passwords.
-   **Decision (Registration RF1):** Provide neutral feedback for existing emails; avoid confirming existence (e.g., "Se l'e-mail è valida, riceverai istruzioni...").
-   **Decision (Timing Attack):** Implement an artificial `sleep` delay to ensure constant response times, regardless of user existence.

### Account Lockout Policy
-   **Decision (Attempts):** Maximum 5 consecutive failed login attempts per account.
-   **Decision (Duration):** Temporary lockout of 15-30 minutes.
-   **Decision (Unlock):** Automatic unlock after timeout. Manual unlock via "reset password" email link (also invalidates sessions).
-   **Decision (Logging RF8):** Log lockout events internally (e.g., "Account [ID_ANONIMIZZATO] locked for 30m") without exposing details to the end-user.

### Rate Limiting (Brute-Force Protection)
-   **Decision (Implementation):** Apply rate limiting per IP address and per Username.
-   **Thresholds:**
    *   Per IP: Max 10 login attempts per minute.
    *   Per Username: Max 3 login attempts per minute.
-   **Mechanism:** Implement a Java Servlet Filter to monitor requests to `LoginServlet`. Respond with HTTP 429 (Too Many Requests) if thresholds are exceeded.

### Additional Registration Measures (RF1)
-   **Decision (CAPTCHA):** Mandatory in the registration form to prevent automated account creation.
-   **Decision (Email Verification RF1):** Implement Double Opt-In (send a unique token via email) for account activation.
-   **Decision (Password Policy RF1):** Enforce minimum length (e.g., 12 characters) and complexity (uppercase, numbers, symbols).
-   **Decision (Input Sanitization):** Treat all registration form fields as "untrusted input" and process via `PreparedStatement` to prevent SQL Injection during account creation.

## Application-Wide Security Measures

-   **Zero Trust Input:** Server-side Regex validation for every field (RF1).
-   **XSS Protection:** Encoding every output in JSPs using JSTL (TA5).
-   **Clickjacking Defense:** `X-Frame-Options: SAMEORIGIN` header.
-   **Scope Reduction:** Minimum visibility for variables and methods (private).
-   **Centralized Auth:** Global Servlet Filter for session control (TA6).