# Phase 5 Context: Home Page Implementation and File Persistence

## Goal
Implement the Home Page functionality and file persistence according to the provided technical specifications, ensuring secure display and access control.

## Key Implementation Decisions

### Contents Table Schema (Task 1)
*   **Decision**: The `contents` table will have the following columns, addressing audit and security requirements (TA4, TA8):
    *   `id` (UUID): Primary key, unique and unguessable.
    *   `user_id` (UUID): Foreign Key referencing the `users` table's `id`.
    *   `original_name` (VARCHAR): The name of the file as uploaded by the user.
    *   `internal_name` (VARCHAR): The unique name of the file on the filesystem (e.g., UUID).
    *   `mime_type` (VARCHAR): Stores the result of Apache Tika analysis, crucial for `Content-Type` header and preventing MIME sniffing.
    *   `file_size` (BIGINT): Stores file size for display, quota checks, and DoS prevention.
    *   `file_path` (VARCHAR): Absolute path on the filesystem outside the webroot, providing flexibility for storage location.
    *   `created_at` (TIMESTAMP): For ordering files (most recent first) in the Home Page and for auditing purposes.

### `Content` Model Definition
*   **Decision**: The `Content` Java POJO will faithfully mirror the database schema, including:
    ```java
    public class Content {
        private UUID id;
        private UUID userId; // Owner
        private String originalName;
        private String internalName; // Name on disk (UUID)
        private String mimeType;
        private long size; // Renamed from file_size to size
        private String filePath;
        private LocalDateTime createdAt;
        // Getter and Setter methods
    }
    ```
    (Note: `size` and `createdAt` will be handled as `long` and `LocalDateTime` in Java, mapping to `BIGINT` and `TIMESTAMP` in DB).

### `ContentRepository` Interface and Implementation
*   **Decision**: The `ContentRepository` interface will define the following methods to manage the content lifecycle:
    *   `void save(Content content)`: To persist metadata after upload.
    *   `List<Content> findByUserId(UUID userId)`: To populate the logged-in user's Home Page (RF6).
    *   `Optional<Content> findById(UUID id)`: For secure download/viewing, retrieving the physical path and owner ID (for IDOR prevention).
    *   `void delete(UUID id)`: For atomic (DB + disk) secure removal.

### `home.jsp` UI/UX for the Tabella (Task 3)
*   **Decision (File Type Display)**: The "Tipo" column will display an icon or a simplified string based on the file extension (e.g., "TEXT", "PDF") for UX. The full MIME type can be included in a `title` attribute (tooltip) for technical precision.
*   **Decision (Actions Column)**: The "Azioni" column will include the following links:
    *   "Visualizza" (View): Opens the file in the browser (if supported, e.g., `.txt`).
    *   "Scarica" (Download): Forces file download (`Content-Disposition: attachment` header).
    *   "Elimina" (Delete): A red button that requires confirmation.
*   **Security**: All data from the DB (especially `originalName`) must be rendered with `<c:out>` to neutralize Stored XSS (TA5).

### File Upload Integration (Implicitly related to Task 1 & 2)
*   **Decision (FileUploadServlet Flow)**: The `FileUploadServlet` (or a dedicated `ContentService`) will follow a sequential and atomic flow:
    1.  **Ricezione (Reception)**: Reads the file stream.
    2.  **Validazione (Validation)**: Passes content to Apache Tika to confirm its legitimacy (e.g., `text/plain`).
    3.  **Salvataggio Fisico (Physical Storage)**: Writes the file outside the webroot, renaming it with a UUID (`internalName`).
    4.  **Persistenza Metadati (Metadata Persistence)**: Creates the `Content` object and calls `contentRepository.save()`.
    5.  **Feedback**: Returns a generic message (RF8) for errors in any of the preceding steps, logging internal details.

## Rigid Constraints (from user)
*   Maintain the current SQL schema for the user.
*   Do not add unnecessary external libraries.
*   Do not reintroduce CAPTCHA.
*   Do not reintroduce email verification.

## Unresolved Questions / Deferred Ideas
None at this point, as all identified gray areas have been clarified.
