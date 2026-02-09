# Research Summary: SicNelleApplicazioni

**Domain:** Secure Java Web Application Development for Content Sharing
**Researched:** 2024-02-09
**Overall confidence:** HIGH

## Executive Summary

This research outlines the foundational ecosystem for "SicNelleApplicazioni," a Java Servlet/JSP web application focused on secure textual content sharing. The project emphasizes secure software development and defensive programming to counter common web vulnerabilities. Key findings highlight modern stable versions for Java JDK (SE 25), Apache Tomcat (11.0.18), and MySQL (8.4.8 LTS), along with essential security libraries like OWASP Java Encoder, Apache Commons Validator, and Apache Tika. A critical divergence from initial project requirements was identified in password hashing, recommending stronger KDFs (PBKDF2/BCrypt/SCrypt) over raw SHA-256 for enhanced security. The architecture proposes a standard 3-tier MVC pattern, with rigorous security practices integrated across all layers, from input validation to output encoding and secure session management. Several critical pitfalls, particularly around authentication, injection, and secure file handling, are identified with clear prevention strategies.

## Key Findings

**Stack:** Recommended stack includes Java JDK SE 25, Apache Tomcat 11.0.18, MySQL 8.4.8 LTS, and security libraries like OWASP Java Encoder (1.4.0), Apache Commons Validator (1.10.1), and Apache Tika (3.1.0). **Critical recommendation to use PBKDF2/BCrypt/SCrypt for password hashing instead of SHA-256.**
**Architecture:** A standard 3-tier MVC architecture (Servlets as controllers, JSPs as views, service/data access layers) with a strong emphasis on secure data flow (HTTPS, SSL/TLS for DB) and integrated security patterns (server-side validation, output encoding, least privilege).
**Critical pitfall:** The most critical pitfall is the explicit mention of SHA-256 for password hashing in the project brief; modern security mandates computationally expensive KDFs like PBKDF2, BCrypt, or SCrypt to prevent brute-force attacks.

## Implications for Roadmap

Based on research, suggested phase structure prioritizes foundational security and core application features immediately due to the project's security-centric nature.

1.  **Phase 1: Secure Foundation & Basic User Management**
    -   **Rationale:** Establish the secure environment and core user authentication/authorization mechanisms from day one. This addresses the most critical security pitfalls upfront.
    -   **Addresses:** User Registration & Login, Secure Password Handling (using recommended KDFs), Session Management, HTTPS/TLS Communication, Secure Database Connectivity.
    -   **Avoids:** Insecure password hashing, unsecure session handling.

2.  **Phase 2: Content Sharing & Core Security Controls**
    -   **Rationale:** Implement the primary content sharing functionality, integrating robust input/output security and file handling.
    -   **Addresses:** Content Upload (Textual) & Viewing, Rigorous File Type Validation (Apache Tika), Comprehensive Injection Protection (PreparedStatement, OWASP Java Encoder).
    -   **Avoids:** SQL Injection, XSS, Insecure File Uploads.

3.  **Phase 3: Refinement & Advanced Security Hardening**
    -   **Rationale:** Refine the application, address moderate pitfalls, and introduce further hardening.
    -   **Addresses:** Robust error handling, comprehensive dependency management, adhering strictly to MVC/defensive programming.
    -   **Avoids:** Information leakage, outdated dependencies, business logic in JSPs.

**Phase ordering rationale:**
Starting with foundational security (authentication, communication, database) is paramount for a "secure application." Building on this, core content sharing features are integrated with comprehensive security controls. Subsequent phases focus on refinement and advanced hardening. This ensures that security is baked in from the ground up, rather than being an afterthought.

**Research flags for phases:**
-   Phase 1: **Password Hashing Implementation**: Deep dive into the chosen KDF (PBKDF2/BCrypt/SCrypt) to ensure correct salt generation, iteration count, and secure storage. This is a critical implementation detail.
-   Phase 1: **Session Management Configuration**: Verify all cookie flags (`HttpOnly`, `Secure`, `SameSite`) and session timeout settings are correctly configured for Apache Tomcat.
-   Phase 2: **Apache Tika Integration**: Detailed research into Apache Tika API for accurate MIME type detection, especially for text files, to prevent bypasses.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Specific versions and libraries identified based on current recommendations and project requirements. Key security enhancement (password hashing) flagged. |
| Features | HIGH | Clear distinction between table stakes, differentiators, and anti-features, directly derived from project description and security best practices. |
| Architecture | HIGH | Standard 3-tier MVC, components, data flow, and patterns align with secure Java web development. |
| Pitfalls | HIGH | Comprehensive list of critical, moderate, and minor pitfalls, with specific relevance to Java Servlet/JSP security. |

## Gaps to Address

-   **Specific KDF Implementation Details:** While PBKDF2/BCrypt/SCrypt are recommended, the exact library and implementation specifics (e.g., iteration counts for PBKDF2, work factors for BCrypt) will require focused research during the implementation phase.
-   **Exact JDBC SSL/TLS Configuration for MySQL:** While recommended, the precise setup steps and connection string parameters for SSL/TLS between Java JDBC and MySQL will need to be detailed during the development phase.
-   **Jakarta EE vs Java EE:** Tomcat 11 supports Jakarta EE 10. Given the project description mentions Java EE, a quick verification during setup would be prudent to ensure compatibility if any older Java EE APIs are implicitly assumed. (Note: Servlet 4.0 is part of Java EE 8, which largely corresponds to Jakarta EE 8).
