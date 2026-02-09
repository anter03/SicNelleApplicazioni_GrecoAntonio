# Plan for Phase 3: Secure Content & File Handling

**Goal**: Authenticated users can securely upload textual content, and the application protects against SQL Injection and XSS when handling and displaying this content.

---
## Tasks

<task>
<name>Implement File Upload Servlet (Initial)</name>
<action>
Implement a File Upload Servlet that handles requests to upload files. This servlet should initially accept any file type.
</action>
<files>
- `src/main/java/.../servlet/FileUploadServlet.java`
- `src/main/webapp/upload.jsp`
</files>
<verify>
- Verify that a file can be successfully uploaded to the temporary directory.
- Verify that the upload form is displayed correctly in `upload.jsp`.
</verify>
<done>
A File Upload Servlet and a basic upload JSP are implemented to handle file uploads.
</done>
</task>

<task>
<name>Restrict Uploads to .txt File Extensions</name>
<action>
Modify the File Upload Servlet to restrict uploads to only `.txt` file extensions.
</action>
<files>
- `src/main/java/.../servlet/FileUploadServlet.java`
</files>
<verify>
- Test uploading files with `.txt` extension and verify they are accepted.
- Test uploading files with other extensions (e.g., `.jpg`, `.pdf`) and verify they are rejected with an appropriate error message.
</verify>
<done>
The File Upload Servlet is modified to restrict uploads to only `.txt` file extensions based on the filename.
</done>
</task>

<task>
<name>Integrate Apache Tika for MIME Type Validation</name>
<action>
Integrate Apache Tika into the File Upload Servlet to verify the real MIME type of uploaded files. Reject files that are not `text/plain`, even if they have a `.txt` extension.
</action>
<files>
- `pom.xml` (for Apache Tika dependency)
- `src/main/java/.../servlet/FileUploadServlet.java`
</files>
<verify>
- Test uploading a genuine `.txt` file and verify it's accepted.
- Test uploading a file disguised as `.txt` (e.g., an image renamed to `.txt`) and verify it's rejected due to incorrect MIME type.
- Test uploading a file with a non-`.txt` extension and verify it's rejected by the extension check.
</verify>
<done>
Apache Tika is integrated into the File Upload Servlet for MIME type detection, rejecting non-`text/plain` files.
</done>
</task>

<task>
<name>Implement Secure Storage for Uploaded Content</name>
<action>
Implement secure storage for the uploaded textual content. If storing in a database, ensure `PreparedStatement` is used to prevent SQL Injection. This involves creating `Content` model, `ContentRepository` interface, and `JdbcContentRepository` implementation.
</action>
<files>
- `src/main/java/.../model/Content.java`
- `src/main/java/.../repository/ContentRepository.java`
- `src/main/java/.../repository/JdbcContentRepository.java`
- `src/main/java/.../servlet/FileUploadServlet.java`
</files>
<verify>
- Verify that uploaded content is successfully saved to the database (or other persistent storage).
- Conduct a code review of `JdbcContentRepository.java` to ensure `PreparedStatement` is used for all database interactions.
- Ensure the temporary file created during upload is deleted after processing.
</verify>
<done>
Secure storage for uploaded textual content is implemented using `Content` model, `ContentRepository`, and `JdbcContentRepository` with `PreparedStatement` for SQL Injection prevention.
</done>
</task>

<task>
<name>Create JSP for Content Display with XSS Protection</name>
<action>
Create a JSP page to display the uploaded textual content. Ensure all content displayed is sanitized using JSTL `<c:out>` or similar mechanisms to prevent XSS. This involves creating `DisplayContentServlet` and `displayContent.jsp`.
</action>
<files>
- `src/main/java/.../servlet/DisplayContentServlet.java`
- `src/main/webapp/displayContent.jsp`
</files>
<verify>
- Verify that uploaded content is correctly displayed on `displayContent.jsp`.
- Test displaying content containing XSS payloads (e.g., `<script>alert('XSS')</script>`) and verify that the content is encoded and the script does not execute.
</verify>
<done>
A JSP page for content display is created, ensuring XSS protection through JSTL `<c:out>` for all displayed content.
</done>
</task>

<task>
<name>Develop Comprehensive Unit and Integration Tests</name>
<action>
Develop comprehensive unit and integration tests for the file upload functionality, MIME type validation, secure content storage (including SQL Injection prevention), and XSS protection in content display.
</action>
<files>
- `src/test/java/.../servlet/FileUploadServletTest.java`
- `src/test/java/.../repository/JdbcContentRepositoryTest.java`
</files>
<verify>
- All unit tests pass successfully.
- Conduct a code review of the test files to ensure adequate coverage of the implemented security measures.
</verify>
<done>
Comprehensive unit tests are developed for file upload, MIME type validation, and secure content storage. XSS protection in content display is verified through correct JSP tag usage.
</done>
</task>

---
*Created: Monday, February 9, 2026*
