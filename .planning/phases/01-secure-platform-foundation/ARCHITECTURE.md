# Architecture Patterns

**Domain:** Secure Java Web Application for Content Sharing
**Researched:** 2024-02-09

## Recommended Architecture

The recommended architecture is a standard 3-tier web application, providing clear separation of concerns and a solid foundation for security and maintainability.

```
+----------------+       +-------------------+       +---------------------+       +--------------+
|                |       |                   |       |                     |       |              |
|   Web Browser  | ----> |  Apache Tomcat    | ----> | Java Servlets /     | ----> |    MySQL     |
|    (Client)    |       | (Web Server/      |       | Business Logic      |       | (Database)   |
|                |       | Servlet Container) |       | (Service Layer)     |       |              |
+----------------+       +-------------------+       +----------^----------+       +------^-------+
                                                            |           |                   |
                                                            |           |                   | SSL/TLS
                                                            |           v                   |
                                                            |   JSP Pages (View) <----------+
                                                            |  (Output Encoding)
                                                            |
                                                            +-------------------------+
                                                                                      |
                                                                                      | JDBC
                                                                                      v
                                                                             +---------------------+
                                                                             | Data Access Layer   |
                                                                             | (PreparedStatement) |
                                                                             +---------------------+
```

### Component Boundaries

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| **Web Browser (Client)** | User interaction, display UI, send/receive HTTP(S) requests. | Apache Tomcat (via HTTPS) |
| **Apache Tomcat** | Web server, Servlet container, manages HTTP(S) connections, routes requests. | Web Browser, Java Servlets |
| **Java Servlets** | Act as controllers, handle HTTP requests, validate input, orchestrate business logic, prepare data for views. | Apache Tomcat, Business Logic (Service Layer), Data Access Layer, JSP Pages |
| **Business Logic (Service Layer - Optional)** | Contains core business rules, application logic, transaction management. | Java Servlets, Data Access Layer |
| **JSP Pages** | Presentation layer, renders dynamic HTML content, responsible for output encoding. | Java Servlets |
| **Data Access Layer** | Manages persistence, interacts with the database (JDBC), handles SQL operations. | Java Servlets, Business Logic (Service Layer), MySQL Database |
| **MySQL Database** | Stores and retrieves application data securely. | Data Access Layer (via SSL/TLS) |

### Data Flow

1.  A user interacts with the **Web Browser (Client)**, initiating an HTTP(S) request (e.g., login, content upload).
2.  The request is sent over **HTTPS** to **Apache Tomcat**.
3.  **Apache Tomcat** receives the request and dispatches it to the appropriate **Java Servlet**.
4.  The **Java Servlet** performs:
    *   **Input Validation:** Using libraries like Apache Commons Validator.
    *   Invokes **Business Logic** (potentially within a Service Layer) to process the request.
    *   The **Business Logic** or **Servlet** interacts with the **Data Access Layer** for database operations.
5.  The **Data Access Layer** executes secure database queries using `PreparedStatement` to communicate with the **MySQL Database** over an **SSL/TLS** encrypted connection.
6.  The **MySQL Database** performs the requested operation and returns data to the **Data Access Layer**.
7.  The **Data Access Layer** returns results to the **Business Logic** / **Servlet**.
8.  The **Java Servlet** prepares the data and forwards the request to a **JSP Page** for rendering the response.
9.  The **JSP Page** generates HTML output, ensuring all dynamic content is properly encoded using **OWASP Java Encoder** to prevent XSS.
10. The rendered HTML response is sent back through **Apache Tomcat** to the **Web Browser (Client)** over **HTTPS**.

## Patterns to Follow

### Pattern 1: Model-View-Controller (MVC)
**What:** Separates an application into three main components: Model (data/business logic), View (user interface), and Controller (handles user input).
**When:** For managing complexity and improving maintainability in web applications.
**Example:** Servlets act as Controllers, JSPs as Views, and Java POJOs/Beans represent the Model or are used in the Service layer.

### Pattern 2: Server-Side Input Validation
**What:** All user input is rigorously validated on the server to ensure it conforms to expected formats and does not contain malicious content.
**When:** Absolutely all user-provided data, regardless of client-side validation.
**Example:** Using Apache Commons Validator in Servlets before processing data.

### Pattern 3: Output Encoding
**What:** All dynamic content rendered on a web page is properly encoded to prevent the browser from interpreting it as executable code.
**When:** Whenever user-generated or external data is displayed in JSPs.
**Example:** Using OWASP Java Encoder in JSPs: `<%= OWASP.encoder.encodeForHTML(userInput) %>`.

### Pattern 4: Principle of Least Privilege
**What:** Each component, user, and process in the system is granted only the minimum permissions necessary to perform its function.
**When:** During system design, database user creation, and application deployment.
**Example:** Database user only has `SELECT`, `INSERT`, `UPDATE`, `DELETE` permissions on specific tables, not `DROP` or `GRANT`.

## Anti-Patterns to Avoid

### Anti-Pattern 1: Business Logic in JSPs (Scriptlets)
**What:** Embedding Java code (scriptlets `<% %>`) directly into JSP pages to perform complex business operations or data manipulation.
**Why bad:** Violates separation of concerns, leads to tangled code, difficult to test, maintain, and secure (e.g., can expose security flaws if not carefully handled).
**Instead:** Use Servlets for controlling flow and business logic. JSPs should primarily focus on presentation using JSTL and Expression Language (EL).

### Anti-Pattern 2: Direct Database Access from JSPs
**What:** Embedding JDBC code or database connection details directly within JSP pages.
**Why bad:** Exposes sensitive database credentials, makes the application highly vulnerable to SQL Injection, and blurs architectural layers.
**Instead:** Database interactions should be encapsulated within a dedicated Data Access Layer, called by Servlets or Service Layer components.

### Anti-Pattern 3: Hardcoding Credentials or Sensitive Configuration
**What:** Embedding database passwords, API keys, or other sensitive configuration directly into source code.
**Why bad:** Security risk if code is compromised, difficult to manage across different environments, and makes auditing harder.
**Instead:** Use secure configuration management (e.g., environment variables, encrypted configuration files, Java KeyStore) for sensitive data.

### Anti-Pattern 4: Ignoring Comprehensive Error Handling
**What:** Allowing raw stack traces or overly detailed error messages to be displayed to end-users.
**Why bad:** Provides attackers with valuable information about the system's internals, potential vulnerabilities, and component versions.
**Instead:** Implement generic, user-friendly error pages for external users, and log detailed exceptions securely for internal debugging.

## Scalability Considerations

| Concern | At 100 users | At 10K users | At 1M users |
|---------|--------------|--------------|-------------|
| **Application Server** | Single Apache Tomcat instance. | Multiple Apache Tomcat instances behind a load balancer. | Geographically distributed clusters of Tomcat instances, potentially transitioning to microservices architecture. |
| **Database** | Single MySQL instance. | MySQL Master-Slave replication for read scaling; connection pooling. | Database sharding, read replicas, NoSQL for specific use cases (e.g., caching), connection pooling. |
| **Session Management** | In-memory sessions (Tomcat defaults). | Sticky sessions on load balancer; distributed session store (e.g., Redis, external database). | Distributed session store with high availability and fault tolerance. |
| **Static Content** | Served directly by Tomcat. | Served by a dedicated web server (e.g., Nginx) or Content Delivery Network (CDN). | CDN for global distribution, aggressive caching. |
| **Business Logic** | Monolithic application. | Modularized business logic, clear service boundaries. | Microservices-based architecture for independent scaling of components. |

## Sources

- `.planning/PROJECT.md`
- `.planning/research/STACK.md`
- Web search results for "Java Servlet JSP security best practices 2024"
- General software architecture patterns (e.g., MVC, Layered Architecture)
- OWASP guidelines for secure web application design.
