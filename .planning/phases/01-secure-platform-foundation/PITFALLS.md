# Domain Pitfalls

**Domain:** Secure Java Web Application for Content Sharing
**Researched:** 2024-02-09

## Critical Pitfalls

Mistakes that cause rewrites or major issues.

### Pitfall 1: Weak Password Hashing Algorithm
**What goes wrong:** Using fast, cryptographic hash functions like SHA-256 directly for password hashing, even with salting.
**Why it happens:** SHA-256 is designed for speed, which makes it highly susceptible to brute-force attacks and rainbow table attacks on passwords, even with salting. Attackers can quickly test billions of password guesses per second.
**Consequences:** Compromised user accounts, loss of user trust, severe data breaches.
**Prevention:** Always use computationally expensive, memory-hard Key Derivation Functions (KDFs) specifically designed for password hashing, such as PBKDF2, BCrypt, or SCrypt. Ensure proper salt generation (`SecureRandom`) and storage per password.
**Detection:** Security audits, password crack testing, code reviews.

### Pitfall 2: SQL Injection (SQLi)
**What goes wrong:** Malicious SQL queries are injected into input fields, allowing attackers to read, modify, or delete database data, or even execute administrative operations.
**Why it happens:** Concatenating user-supplied input directly into SQL queries without proper sanitization or parameterization.
**Consequences:** Complete database compromise, data theft, data manipulation, unauthorized access, denial of service.
**Prevention:** Systematically use `PreparedStatement` with parameterized queries for all database interactions. Never concatenate user input directly into SQL strings.
**Detection:** Web application vulnerability scanners, code reviews, penetration testing.

### Pitfall 3: Cross-Site Scripting (XSS)
**What goes wrong:** Malicious scripts are injected into web pages and executed in the user's browser, allowing attackers to steal session cookies, deface websites, or redirect users to malicious sites.
**Why it happens:** Displaying user-supplied content on a web page without proper output encoding. The browser interprets the malicious script as legitimate page content.
**Consequences:** Session hijacking, unauthorized actions on behalf of the user, data theft, website defacement, spread of malware.
**Prevention:** Use robust output encoding libraries (e.g., OWASP Java Encoder) to escape all dynamic content before rendering it in HTML, JavaScript, URL attributes, etc.
**Detection:** Web application vulnerability scanners, Content Security Policy (CSP), code reviews.

### Pitfall 4: Insecure Session Management
**What goes wrong:** Attackers exploit vulnerabilities in session handling to hijack user sessions. This includes Session Fixation (forcing a user to use a known session ID) and insufficient cookie flags.
**Why it happens:** Session IDs are exposed (e.g., in URLs), cookies lack `HttpOnly`, `Secure`, or `SameSite` flags, or session timeouts are too long/short.
**Consequences:** Unauthorized access to user accounts, data theft, impersonation.
**Prevention:**
*   Always use `HttpOnly`, `Secure`, and `SameSite=Lax` (or `Strict` if applicable) flags for session cookies.
*   Generate new session IDs upon authentication (prevent Session Fixation).
*   Implement appropriate session timeouts and inactivity limits.
*   Avoid passing session IDs in URLs.
**Detection:** Security audits, penetration testing.

### Pitfall 5: Insecure File Uploads
**What goes wrong:** Attackers upload malicious files (e.g., executable scripts, web shells) that can then be executed on the server.
**Why it happens:** Relying solely on file extensions for validation, or failing to verify the actual MIME type of the uploaded file.
**Consequences:** Remote code execution, server compromise, data exfiltration, denial of service.
**Prevention:**
*   Strictly define allowed file types (e.g., only `.txt` as per project).
*   Always verify the actual MIME type using libraries like Apache Tika.
*   Store uploaded files outside the web root.
*   Scan uploaded files for malware.
**Detection:** File integrity monitoring, security scans.

### Pitfall 6: Information Leakage through Error Handling
**What goes wrong:** Detailed error messages, stack traces, or internal system configurations are exposed to end-users.
**Why it happens:** Default error pages are not customized, or exceptions are not caught and handled gracefully.
**Consequences:** Attackers gain insights into the application's internal structure, technologies used, potential vulnerabilities, and sensitive data paths.
**Prevention:** Implement custom, generic error pages for all error conditions (e.g., 404, 500). Log detailed error information securely on the server-side, never expose it to clients.
**Detection:** Manual testing, security audits.

### Pitfall 7: Hardcoded Credentials and Sensitive Configuration
**What goes wrong:** Database passwords, API keys, encryption keys, or other secrets are embedded directly in the source code or easily accessible configuration files.
**Why it happens:** Developer convenience, lack of secure configuration management practices.
**Consequences:** Compromise of secrets upon code access, easy exposure during deployments, increased risk of lateral movement for attackers.
**Prevention:** Utilize secure configuration mechanisms such as environment variables, Java KeyStore, secure vaults, or encrypted configuration files. Never commit sensitive data to version control.
**Detection:** Code reviews, static application security testing (SAST), secrets scanning tools.

## Moderate Pitfalls

### Pitfall 1: Business Logic in JSPs
**What goes wrong:** Mixing presentation logic with complex business rules and database calls within JSP scriptlets.
**Prevention:** Adhere strictly to the MVC pattern. Use Servlets for business logic and control flow, and JSPs purely for presentation using JSTL and EL.

### Pitfall 2: Insufficient Input Validation
**What goes wrong:** Incomplete or missing server-side validation of all user inputs.
**Prevention:** Implement comprehensive server-side input validation for all data received from the client, even if client-side validation is present. Use libraries like Apache Commons Validator.

### Pitfall 3: Untrusted/Outdated Dependencies
**What goes wrong:** Using third-party libraries with known security vulnerabilities or deprecated versions.
**Prevention:** Regularly audit and update all project dependencies. Use dependency scanning tools (e.g., OWASP Dependency-Check) and subscribe to security advisories.

## Minor Pitfalls

### Pitfall 1: Excessive Logging of Sensitive Data
**What goes wrong:** Logging passwords, full credit card numbers, PII, or other sensitive information in plain text.
**Prevention:** Implement strict logging policies. Sanitize or redact sensitive data before logging. Be mindful of what information is truly necessary for debugging and auditing.

### Pitfall 2: Poor Exception Handling Granularity
**What goes wrong:** Catching generic `Exception` types without specific handling, leading to swallowed errors or unintended behavior.
**Prevention:** Catch specific exceptions and handle them appropriately. Log relevant details for debugging.

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| **Initial Setup (v1.0 progectInit)** | **Using SHA-256 for password hashing.** Project specified SHA-256 which is weak for passwords. | **CRITICAL: Recommend migrating to PBKDF2, BCrypt, or SCrypt for password hashing immediately.** Update project requirements accordingly. |
| **User Authentication** | Insecure implementation of login/registration forms (e.g., lack of CSRF tokens, weak session handling). | Implement CSRF tokens, secure session management (HttpOnly, Secure, SameSite cookies), and robust input validation. |
| **Content Upload** | Bypassing file type checks, storing files insecurely. | Implement Apache Tika for MIME type validation, store uploaded content outside web root, implement strict access controls. |
| **Database Integration** | SQL Injection vulnerabilities. | Consistent use of `PreparedStatement` for all database queries. |
| **UI Development (JSP)** | XSS vulnerabilities due to unencoded output, scriptlets for business logic. | Enforce output encoding with OWASP Java Encoder, disallow scriptlets for business logic, use JSTL/EL. |

## Sources

- `.planning/PROJECT.md`
- `.planning/research/STACK.md`
- `.planning/research/FEATURES.md`
- OWASP Top 10 Web Application Security Risks (Latest version)
- General secure coding best practices for Java web applications.
