# RESEARCH for Phase 3: Secure Content & File Handling

## Objective
This research aims to consolidate best practices and technical considerations for implementing secure file uploads, content storage, and display, aligning with the decisions made in `03-CONTEXT.md`.

## Key Research Areas & Findings

### 1. Secure File Uploads (Apache Tika & General Best Practices)

-   **File Extension vs. MIME Type Validation:**
    -   File extension checks (e.g., `.txt`) are easily bypassed by renaming files.
    -   **Apache Tika** is crucial for server-side MIME type validation. It analyzes the actual content (magic bytes) to determine the true file type, providing a strong defense against malicious file uploads disguised with legitimate extensions.
    -   **Decision Enforcement:** The upload process must enforce a strict whitelist, allowing only `text/plain` content types.

-   **File Renaming & Storage Location:**
    -   Upon successful validation, uploaded files should be **renamed** using a cryptographically secure random string (e.g., UUID) to prevent **Path Traversal** attacks and to obscure the original filename. The original filename can be stored securely in the database for display purposes.
    -   Files must be stored **outside the webroot** (e.g., in `/var/lib/app_uploads` or a designated secure storage service) to prevent direct execution by the web server. This is a critical security measure.
    -   Strict **file system permissions** must be applied to the upload directory, preventing execution bits and unauthorized read/write access.

-   **Size Limits & DoS Mitigation:**
    -   Enforce **strict file size limits** (`maxFileSize`, `maxRequestSize`) both at the servlet container level (`@MultipartConfig` annotation) and programmatically within the servlet. This is a primary defense against Denial of Service (DoS) attacks.
    -   Consider the total number of files a user can upload within a time frame (rate limiting on upload endpoint).

-   **Antivirus Scanning (Advanced consideration):**
    -   In production environments, integrate with an **antivirus scanner** immediately after upload to detect known malware signatures. (Beyond the immediate scope, but a recommended best practice).

### 2. Secure Content Storage (JDBC `PreparedStatement` for Text)

-   **SQL Injection Prevention:**
    -   **`PreparedStatement`** is the cornerstone defense against **SQL Injection**. When inserting user-provided content (such as `contentText`) into a relational database, `PreparedStatement` must be used. Parameters are sent separately from the SQL query string, ensuring they are treated as literal values, not executable code.
    -   **Decision Enforcement:** `JdbcContentRepository.save()` implementation must exclusively use `PreparedStatement` for all inserts and updates involving user-supplied data.

-   **Character Encoding & Data Integrity:**
    -   Ensure **consistent character encoding** (e.g., UTF-8) is used across the application, database, and JDBC connection. Inconsistent encoding can lead to data corruption or bypass some sanitization mechanisms.
    -   Be mindful of **database column size limits**. Implement logic to handle content exceeding these limits (e.g., truncation with warning, or rejection of upload) to prevent data integrity issues.

### 3. XSS Prevention for Displayed Content (JSTL `c:out`)

-   **Contextual Encoding:**
    -   **JSTL `<c:out>`** tag is the primary mechanism for **HTML escaping** in JSPs. By default, it converts characters like `<`, `>`, `&`, `'`, `"` into their corresponding HTML entities (e.g., `&lt;`, `&gt;`). This is essential for preventing **Reflected XSS** and **Stored XSS** when user-generated content is rendered within HTML contexts.
    -   **Decision Enforcement:** All dynamic, user-generated content displayed in JSPs must be wrapped within `<c:out value="${...}" />`.

-   **HTML Sanitization (Advanced consideration):**
    -   For scenarios where controlled HTML formatting (e.g., rich text, markdown) is allowed, a dedicated **HTML sanitization library** (e.g., OWASP Java HTML Sanitizer) is recommended. This library would parse the HTML, remove dangerous tags/attributes, and only allow a safe subset. (Beyond current project scope but important for future features).

-   **Content Security Policy (CSP) (Advanced consideration):**
    -   Implement a **Content Security Policy (CSP)** HTTP header to restrict resources (scripts, styles, etc.) a browser is allowed to load for a given page. This provides an additional layer of defense against XSS by limiting the impact of any script injection. (Beyond current project scope).

### 4. Content Management (Permissions, Sharing, Deletion, Moderation)

-   **Access Control & Ownership:**
    -   All operations on content (view, share, edit, delete) must undergo **server-side access control checks**. Client-side checks are for UX only and provide no security.
    -   Always verify that the logged-in user is the **owner** of the content (or has been explicitly granted permission, or holds an administrative role) before allowing any action.

-   **Sharing Mechanisms:**
    -   Sharing should be implemented via **targeted user selection** where possible, rather than public links, to maintain better control.
    -   When generating shareable links or identifiers, **never expose internal IDs or original filenames**. Use UUIDs or other opaque identifiers for content, coupled with explicit permission checks on the server.

-   **Content Deletion:**
    -   Implement **atomic deletion:** both the database record and the corresponding physical file (if stored on disk) should be removed in a single, transactional operation to maintain consistency.
    -   Consider **"soft deletes"** (marking as deleted, but retaining data for a period) for audit trails and recovery, before a **"hard delete"** (permanent removal).
    -   Securely clear any in-memory references to deleted content to reduce sensitive data lifetime.

-   **Moderation:**
    -   A **moderator role** is necessary for security incident response, allowing privileged users to review, quarantine, or delete content that violates policies (e.g., malicious content that bypassed initial filters).
    -   The moderation interface itself must be highly secured with strong authentication and authorization.

---
*Generated by Gemini CLI Planner on Monday, February 9, 2026.*
