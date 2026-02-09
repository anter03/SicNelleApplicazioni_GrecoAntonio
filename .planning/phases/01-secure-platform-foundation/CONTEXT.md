# Context for Phase 1: Secure Platform Foundation

This document captures key decisions and clarifications for Phase 1 of the "SicNelleApplicazioni" project. These decisions will guide subsequent research and planning activities, ensuring that implementation aligns with the user's vision for security and functionality.

## 1. TLS/HTTPS Configuration

### TLS Versions and Cipher Suites
- **Decision:** Exclusively use TLS 1.2 and TLS 1.3. Earlier protocols (SSLv2, SSLv3, TLS 1.0, 1.1) are deprecated and insecure.
- **Decision:** For Cipher Suites, prefer AES 256-bit symmetric encryption algorithms and asymmetric key exchange algorithms like RSA or ECDHE.

### Client Compatibility
- **Decision:** Enforce TLS 1.2/1.3 only. Security takes precedence over compatibility with older browsers, as older versions introduce vulnerabilities.
- **Implementation Note:** This is achieved by configuring `sslProtocol="TLS"` (or specifically `protocols="TLSv1.2,TLSv1.3"`) in Tomcat's connector.

### Certificate Management
- **Decision (Development/Test):** Acceptable and recommended to use a self-signed certificate generated via Java's `keytool` command.
- **Command:** `keytool -genkey -alias tomcat -keyalg RSA -keystore keystore.jks -keysize 2048`.
- **Decision (Production):** A certificate signed by a trusted Certificate Authority (CA) is required.
- **Decision (Renewal):** Set a reasonable validity period (e.g., 90-365 days) during generation with `keytool` for the project.

### Additional Protection Mechanisms (HSTS and Headers)
- **Decision (HTTPS Enforcement):** Use the `web.xml` file with `<transport-guarantee>CONFIDENTIAL</transport-guarantee>` to force automatic HTTP to HTTPS redirection via Tomcat.
- **Decision (HSTS):** The `web.xml` redirection acts as a fundamental countermeasure against downgrade attacks.
- **Decision (Cookie Security):** Ensure session cookies always have the `Secure` (HTTPS only) and `HttpOnly` (inaccessible to scripts) flags.

## 2. Database Credential & Connection Security

### Credential Storage
- **Decision (Production):** Prefer a centralized Secret Manager like HashiCorp Vault.
- **Decision (Development/Test):** Avoid plain-text secrets in config files. Environment variables are better, but Vault is preferred for production.

### Java Access and Exposure Minimization
- **Decision (Password Handling):** Never use `String` objects for passwords. Use `char[]` instead.
- **Mitigation:** After establishing a database connection, overwrite the `char[]` with null characters using `Arrays.fill(password, '\0')`.
- **Decision (Logging):** Never log secrets or stack traces that could reveal credentials. Sanitize exceptions before logging.

### MySQL SSL/TLS Enforcement
- **Decision (Enforcement):** JDBC connection must force encryption using `useSSL=true` and `requireSSL=true` in the connection string.
- **Decision (Certificate Validation):** `requireSSL=true` ensures connection failure if the server doesn't support SSL. Full security (MITM prevention) requires server certificate validation. Mutual authentication (mTLS) is advanced; server validation is the minimum.

### TLS Versions and Cipher Suites for Database
- **Decision (TLS Versions):** Use modern protocols like TLS 1.2 or TLS 1.3 for database connections, consistent with Tomcat configuration.
- **Decision (Ciphers):** Utilize the `TLS_AES_256_GCM_SHA384` suite (typical for TLS 1.3) to ensure AES 256-bit symmetric encryption algorithms.

## 3. HTTPS Enforcement & Redirection

### Primary Enforcement Method
- **Decision:** Application-level enforcement via `web.xml` using `<security-constraint>` with `<transport-guarantee>CONFIDENTIAL</transport-guarantee>`. This instructs Tomcat to block HTTP access and automatically redirect to the secure port.

### Redirecting POST Requests and Data Preservation
- **Decision:** Never allow sensitive operations (POST) over HTTP. Data transmitted over HTTP is already compromised.
- **Strategy:** Redirect the user to the HTTPS version of the login page (via GET) before form submission.
- **Status Code:** The entire site must be "HTTPS-only" from the first access for maximum security.

### Internal Links and HSTS
- **Decision (Internal Links):** Always use relative paths in JSP files (e.g., `<a href="dashboard.jsp">`) instead of absolute URLs.
- **Decision (HSTS):** Implement sending the `Strict-Transport-Security` header in server responses.
- **Example Header:** `Strict-Transport-Security: max-age=31536000; includeSubDomains`.

### Verification and Resilience
- **Verification (Manual Test):** Access `http://localhost:8080/app/` and verify redirection to `https://localhost:8443/app/`.
- **Verification (Header Inspection):** Check that the `JSESSIONID` cookie is marked as `Secure`.
- **Verification (CLI Tools):** Use `curl -I http://localhost:8080/app/` and verify the 302/301 status code with the `Location` header pointing to the HTTPS version.

## 4. Secure Error Reporting & Logging

### Detail Level for End-Users
- **Decision (Allowed Messages):** Only generic and non-descriptive messages (e.g., "Si è verificato un errore tecnico", "Credenziali errate", "Risorsa non disponibile").
- **Decision (Never Expose):** Stack Traces, SQL Queries, File system paths, Authentication specifics (to prevent User Enumeration).

### Detailed Log Storage and Security
- **Decision (Location):** Logs must be written exclusively server-side, outside Tomcat's webapps directory (e.g., `/var/log/sic-app/`), protected by system permissions.
- **Decision (Access Control):** Limited access via operating system privileges.
- **Decision (Rotation):** Mandatory log rotation (daily or by size).
- **Decision (Integrity - Critical Contexts):** In critical contexts, logs should be sent in real-time to a centralized logging server (e.g., Syslog, ELK).

### Logging Frameworks and Practices (Anonymization)
- **Decision (Framework):** Use Log4j2 or SLF4J + Logback.
- **Decision (No PII/Secrets):** Prohibited from logging passwords, session tokens, or clear-text Personal Identifiable Information (PII).
- **Decision (Anonymization):** Mask or encrypt user IDs if logging is necessary for debugging.
- **Decision (Structured Logging):** Use JSON format for automated analysis.

### Security Events and Alerting
- **Decision (Identification):** Log failed authentication attempts (TA1, TA2) and access to protected resources (TA6) at WARN or ERROR level.
- **Decision (Log Metadata):** Each security event log must include: Timestamp, Source IP, Session ID (if present), and action outcome.
- **Decision (Alerting):** Critical thresholds (e.g., >5 failed logins/min from same IP) should trigger temporary lockout (Account Lockout) or admin notification. Utilize tools like Fail2Ban.