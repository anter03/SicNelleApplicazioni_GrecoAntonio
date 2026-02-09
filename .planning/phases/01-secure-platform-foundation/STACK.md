# Technology Stack

**Project:** SicNelleApplicazioni
**Researched:** 2024-02-09

## Recommended Stack

### Core Framework
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Java Development Kit (JDK) | SE 25 | Core platform for Java applications | Latest Long-Term Support (LTS) version, providing stability and modern features. |
| Java Servlets/JSP | 4.0/2.3 (bundled with Tomcat 11) | Web application framework components | Specified by project requirements, forms the basis for the web application. |

### Application Server
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Apache Tomcat | 11.0.18 | Servlet container and web server | Current stable version, robust and widely used for Java web applications. |

### Database
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| MySQL Community Server | 8.4.8 LTS | Relational database management system | Current stable Long-Term Support (LTS) version, meeting project data storage needs. Project explicitly requires SSL/TLS for connection. |

### Supporting Libraries
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| OWASP Java Encoder | 1.4.0 | Output encoding for XSS prevention | Essential for sanitizing user-generated content before rendering to prevent Cross-Site Scripting (XSS) attacks. |
| Apache Commons Validator | 1.10.1 | Input validation | For robust server-side validation of user input, ensuring data integrity and preventing injection vulnerabilities. |
| Apache Tika | 3.1.0 | MIME type detection | To accurately detect the real MIME type of uploaded files (e.g., .txt) and prevent malicious file uploads that masquerade as safe types. |
| **Password Hashing Library (e.g., jBcrypt, Spring Security Crypto)** | Latest stable version | Secure password hashing | **CRITICAL:** To implement secure, computationally intensive password hashing (e.g., PBKDF2, BCrypt, SCrypt) with proper salting, as SHA-256 alone is insufficient for robust password security. (Project specified SHA-256 + Salt, but stronger algorithms are recommended). |
| Java EE Security API (JSR 375) | 1.0 (part of Jakarta EE 8+) | Authentication and Authorization | For container-managed authentication and authorization, leveraging Java EE's declarative security model via `web.xml` for roles and permissions. |

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| Password Hashing Algorithm | PBKDF2/BCrypt/SCrypt | SHA-256 | SHA-256 is a fast cryptographic hash, making it vulnerable to brute-force attacks on passwords even with salting. Modern algorithms like PBKDF2, BCrypt, or SCrypt are designed to be computationally expensive, significantly increasing the cost of brute-force attacks. |
| Web Framework (Templating) | JSP (project requirement) | Thymeleaf, JSF | While JSP is a project requirement, modern Java web development often prefers templating engines like Thymeleaf or JSF for better separation of concerns, improved maintainability, and enhanced security features out-of-the-box compared to traditional JSP scriptlets. Keep in mind for future architectural decisions. |
| Authentication/Authorization Framework | Java EE Security API / Custom | Spring Security | Spring Security is a comprehensive and robust security framework but introduces the entire Spring ecosystem. For a project explicitly focused on Servlets/JSP without other Spring dependencies, it might be an unnecessary dependency, preferring Java EE native security or a more lightweight custom implementation for authentication coupled with strong hashing. |

## Installation

```bash
# Core
# Add dependencies to your project's build file (e.g., Maven pom.xml or Gradle build.gradle)

# Example Maven dependencies:
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>4.0.4</version> <!-- Or newer, compatible with Tomcat 11's specification -->
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>jakarta.servlet.jsp</groupId>
    <artifactId>jakarta.servlet.jsp-api</artifactId>
    <version>2.3.7</version> <!-- Or newer, compatible with Tomcat 11's specification -->
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.owasp.encoder</groupId>
    <artifactId>encoder</artifactId>
    <version>1.4.0</version>
</dependency>
<dependency>
    <groupId>commons-validator</groupId>
    <artifactId>commons-validator</artifactId>
    <version>1.10.1</version>
</dependency>
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>3.1.0</version>
</dependency>
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>3.1.0</version>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.4.0</version> <!-- Or newer, compatible with MySQL 8.4 LTS -->
</dependency>
<!-- For password hashing (example using jBcrypt, consider also Spring Security Crypto for PBKDF2) -->
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
```

## Sources

- https://docs.oracle.com/en/java/javase/25/
- https://tomcat.apache.org/
- https://dev.mysql.com/doc/refman/8.4/en/
- https://github.com/OWASP/owasp-java-encoder
- https://commons.apache.org/proper/commons-validator/
- https://tika.apache.org/
- Web search results for "Java Servlet JSP security best practices 2024"
- Web search results for "current stable version Java JDK", "current stable version Apache Tomcat", "current stable version MySQL"
- Web search results for "Java secure password hashing SHA-256 salt library", "Java authentication authorization library Servlet JSP", "OWASP Java Encoder current version", "Apache Commons Validator current version", "Apache Tika current version"
