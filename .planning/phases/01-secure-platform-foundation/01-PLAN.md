# Plan for Phase 1: Secure Platform Foundation

**Goal**: The core application environment is configured with end-to-end secure communication channels.

---
## Tasks

<task>
<name>Configure Tomcat TLS/HTTPS</name>
<action>
Configure Apache Tomcat to use TLS 1.2/1.3, with AES 256-bit symmetric encryption and RSA/ECDHE for key exchange. This includes setting `sslProtocol` and `ciphers` in `server.xml`.
</action>
<files>
- `apache-tomcat/conf/server.xml`
</files>
<verify>
Verify Tomcat is only accessible via HTTPS. Attempt to access via HTTP (port 8080 or configured HTTP port) and confirm redirection to HTTPS (port 8443 or configured HTTPS port). Use `curl -v http://localhost:8080/` to check redirection headers.
</verify>
<done>
Tomcat is configured for secure HTTPS communication using TLS 1.2/1.3 and robust cipher suites.
</done>
</task>

<task>
<name>Generate and Configure Tomcat SSL Certificate</name>
<action>
Generate a self-signed SSL certificate for Tomcat using `keytool` for development/testing purposes. Store it in a `keystore.jks` file and configure the Tomcat connector in `server.xml` to use this keystore.
</action>
<files>
- `keystore.jks` (generated file)
- `apache-tomcat/conf/server.xml`
</files>
<verify>
Ensure Tomcat starts successfully with the new certificate. Access the application via HTTPS and verify the browser shows a valid (though self-signed) certificate.
</verify>
<done>
SSL certificate generated and configured for Tomcat.
</done>
</task>

<task>
<name>Implement Global HTTPS Enforcement in web.xml</name>
<action>
Implement global HTTPS enforcement in the application's `web.xml` using a `<security-constraint>` with `<transport-guarantee>CONFIDENTIAL</transport-guarantee>`. This will ensure all HTTP requests are redirected to HTTPS at the application level.
</action>
<files>
- `webapp/WEB-INF/web.xml`
</files>
<verify>
Deploy the application and attempt to access any resource via HTTP. Verify that the request is automatically redirected to the HTTPS version of the URL. Test with both GET and POST requests if possible.
</verify>
<done>
HTTPS enforcement is configured via `web.xml`, redirecting all HTTP traffic to HTTPS.
</done>
</task>

<task>
<name>Configure HSTS and Secure Session Cookies</name>
<action>
Configure Tomcat or the application to send the `Strict-Transport-Security` (HSTS) header in all HTTPS responses. Additionally, ensure that session cookies (`JSESSIONID`) are always marked with `Secure` and `HttpOnly` flags.
</action>
<files>
- `apache-tomcat/conf/server.xml` (for HSTS filter)
- `webapp/WEB-INF/web.xml` (for session cookie configuration)
</files>
<verify>
Access the application via HTTPS. Use browser developer tools or `curl -v https://localhost:8443/` to inspect response headers for `Strict-Transport-Security`. Inspect session cookies to confirm `Secure` and `HttpOnly` flags are set.
</verify>
<done>
HSTS header is sent with HTTPS responses, and session cookies are secured with `Secure` and `HttpOnly` flags.
</done>
</task>

<task>
<name>Configure MySQL JDBC SSL/TLS Connection</name>
<action>
Modify the application's JDBC connection string to MySQL to explicitly enforce SSL/TLS, using parameters like `useSSL=true` and `requireSSL=true`. Ensure modern TLS versions (1.2/1.3) and strong cipher suites are implicitly or explicitly used by the JVM and MySQL connector.
</action>
<files>
- `src/main/java/.../YourDatabaseConnectionClass.java` (or relevant configuration file)
</files>
<verify>
Attempt to establish a database connection without SSL/TLS enabled on the client or server (if possible in a test environment); the connection should fail. Verify that the application successfully connects to the database when SSL/TLS is enforced.
</verify>
<done>
MySQL JDBC connection is securely configured for SSL/TLS, enforcing encryption.
</done>
</task>

<task>
<name>Implement Secure Database Credential Handling</name>
<action>
Refactor application code to handle database passwords using `char[]` instead of `String`. Implement clearing of the `char[]` with `Arrays.fill(password, '\0')` immediately after use to minimize memory exposure.
</action>
<files>
- `src/main/java/.../YourDatabaseConnectionClass.java`
- `src/main/java/.../LoginService.java` (or any class handling credentials)
</files>
<verify>
Conduct a code review to confirm that `String` objects are not used for sensitive password data and that `char[]` are properly cleared. Attempt memory dumps in a test environment to confirm credentials are not persistent.
</verify>
<done>
Database credentials are handled securely in application code using `char[]` and memory clearing.
</done>
</task>

<task>
<name>Configure Secure Error Logging and Reporting</name>
<action>
Integrate a logging framework (e.g., Log4j2 or SLF4J + Logback). Configure it to write logs to a secure, server-side directory outside the web application's root (e.g., `/var/log/sic-app/`). Implement log rotation, prohibit logging of PII/secrets, and ensure anonymization of user IDs if logged. Use JSON format for structured logging.
</action>
<files>
- `src/main/resources/log4j2.xml` or `logback.xml`
- `src/main/java/.../GlobalExceptionHandler.java` (or similar)
</files>
<verify>
Trigger various error conditions (e.g., invalid input, database errors) and inspect log files. Verify that logs are written to the correct secure location, do not contain sensitive information, and are rotated. Confirm logs are in JSON format.
</verify>
<done>
Secure error logging and reporting are configured, protecting sensitive data and ensuring proper log management.
</done>
</task>

---
*Created: Monday, February 9, 2026*
