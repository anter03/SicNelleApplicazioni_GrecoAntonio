# CONTEXT for Phase 3: Secure Content & File Handling

## Overview
This document captures key implementation decisions for Phase 3, ensuring that subsequent research and planning phases are guided by clear, security-conscious choices. The goal of Phase 3 is to enable authenticated users to securely upload textual content, with robust protection against SQL Injection and XSS during content handling and display.

---

## 1. File Upload User Experience

### 1.1 Visual Indicator for Upload Progress
**Decision:** Yes, implement a progress bar or "Loading..." indicator.
**Rationale:** Improves UX and prevents "Double Submit" attacks by giving immediate feedback and discouraging multiple submission clicks, which could otherwise overload the server or create race conditions with temporary file handling.

### 1.2 Success Message
**Decision:** A concise and neutral message: "File caricato correttamente" (File uploaded successfully).
**Rationale:** Avoids information leakage. Never expose physical server paths (e.g., C:\uploads\file.jpg), renamed file names (e.g., 550e8400-e29b.pdf), or direct links unless strictly necessary. Minimizing external information reduces an attacker's ability to map the filesystem structure (TA8).

### 1.3 Client-Side Handling of Invalid Files
**Decision:** Use the `accept` attribute in HTML (e.g., `accept=".pdf,.jpg"`) and immediate JavaScript validation before submission.
**Approach:** If an invalid file is selected, disable the "Upload" button and display a warning: "Formato file non supportato" (Unsupported file format).
**Critical Note:** This is solely for UX. The agent must understand that the primary security barrier is server-side validation with Apache Tika, as client-side extension checks are easily manipulated.

### 1.4 Detail of Server-Side Error Messages
**Decision:** Error messages for the user must be generic, while detailed logs are maintained internally (RF8).
**Example for User:** "Errore: il file non soddisfa i requisiti di sicurezza o dimensionali" (Error: the file does not meet security or size requirements).
**Rationale:** Revealing specific failure reasons (e.g., "Error: MIME type 'application/x-php' not allowed") provides attackers with valuable hints on blocked content and circumvention strategies. Prevents information leakage during abuse testing failures (TA8).
**Attention Point:** Every server-side error, beyond the generic message, must trigger a JSON-formatted log entry recording the IP address and violation type, as established in Phase 1.

---

## 2. Content Display Behavior

### 2.1 Long Text Handling
**Decision:** Use buffering or pagination to load and display text in chunks, preventing Denial of Service (DoS) attacks by saturating server/client memory with excessively long content.
**Implementation:** In Java, use `ByteBuffer` with direct allocation, ensuring `clear()` is called after use to limit the lifetime of sensitive data in memory.
**Limits:** Enforce a maximum visible file size (`maxFileSize`) on the server-side.

### 2.2 Formatting and Syntax Highlighting
**Decision:** Before applying any formatting, mandatory escaping of special characters (sanitization) is required to prevent Stored XSS attacks (RF6). For example, convert `<` to `&lt;` and `>` to `&gt;`.
**Highlighting:** If syntax highlighting is implemented, ensure the library operates on already sanitized text and does not reintroduce vulnerabilities via unprotected `innerHTML` usage.

### 2.3 Searchability
**Decision:** The search function must strictly adhere to access control.
**Server-side:** All search operations must occur server-side, where the system can verify user permissions for specific files (RF3).
**Results:** Do not display text snippets in search results if the user lacks authorization for that file, to prevent accidental exposure of sensitive data.

### 2.4 Access Denied Behavior
**Decision:** Consistent with RF8 (Feedback message management) and defensive programming.
**Non-informative Messages:** In cases of insufficient permissions or expired sessions, the application must provide generic error messages that do not reveal details about file structure or the existence of specific resources to potential attackers.
**Fail-safe:** Access must be denied by default if the session is absent or invalid (RF3).
**Status Code:** Use appropriate HTTP status codes (e.g., 403 Forbidden or 404 Not Found) to prevent resource enumeration.

---

## 3. Content Management (User Permissions)

### 3.1 Access Levels
**Decision:** Follow the "Default Deny" principle (denial by default).
**Private (Default):** All content uploaded via RF5 is initially associated exclusively with the owner's account.
**Shared:** Access is extended to other authenticated users as per RF6 requirements.
**Public:** Avoid public access in this context to minimize the attack surface, unless specific requirements dictate otherwise. Confidentiality is maintained by requiring a valid session for resource access.

### 3.2 Sharing Mechanisms
**Decision:** Prioritize targeted sharing (e.g., via user ID) over public links.
**Secure Identification:** Never use the original filename in the URL or sharing parameters to prevent Path Traversal or enumeration attacks. Files must be identified using multiple pieces of information (e.g., internal ID + ownership check within the session).
**Integrity:** Ensure the `SameSite` attribute on cookies is set (RF4) to mitigate CSRF attacks that could force unintended sharing.

### 3.3 Editing/Deletion Implications
**Decision:** Only the owner should be able to modify or delete their content.
**Consistency and Concurrency:** Deletion must be atomic: removal of the physical file from the filesystem and its reference from the database. Use synchronization mechanisms (e.g., explicit locks or `@ThreadSafe` annotations) to manage simultaneous access to shared resources and prevent race conditions.
**Secure Deletion:** Explicitly clear memory references (heap) associated with removed files to reduce the lifetime of sensitive data.

### 3.4 Admin and Moderation
**Decision:** A moderator role is essential for managing abuse tests (TA3-TA5), such as uploaded scripts for Stored XSS.
**Defensive Programming:** Administrators must be able to invalidate suspicious sessions or remove malicious content that has bypassed initial filters (e.g., files with legitimate extensions but dangerous content).

---

## 4. Error Handling & User Feedback

### 4.1 Specific Upload Error Conditions
**Decision:** Distinguish between technical errors and security violations to meet security requirements and abuse tests (TA3, TA4, TA5).
**Policy Violation (Security Error):** Forbidden extension (e.g., `.exe`), MIME-type mismatch (detected via Apache Tika), or presence of malicious scripts (`<script>`) identified during input sanitization.
**Resource Limits:** Files exceeding `maxFileSize` or exhausted server disk space.
**Integrity Errors:** File corruption during transfer (HMAC/Checksum failure).

### 4.2 Appropriate Generic User Messages (RF8)
**Decision:** The user must never receive technical details that could aid an attacker (Information Leakage).
**Response:** "The system must return uniform messages. If an upload fails because the file is malware (TA4) or the server is offline, the message for the user will be the same: 'Impossibile completare l'operazione di caricamento. Riprova più tardi o contatta l'assistenza.'" (Unable to complete the upload operation. Please try again later or contact support.)
**Rule:** Never expose Stack Traces or file paths (Path Traversal) in the UI.

### 4.3 Detailed Internal Logging
**Decision:** While the user receives generic feedback, the system must log all details internally for auditing.
**Necessary Details:** User ID, Timestamp, Type of violation (e.g., "MIME Mismatch: expected image/png, got application/x-msdownload"), IP Address.
**Log Security:** Ensure sensitive data (cleartext passwords or session IDs) are never included in logs, adhering to data lifetime guidelines (PDF Lifetime.pdf).

### 4.4 Automatic Recovery for Transient Errors
**Decision:** Implement a Retry mechanism with Exponential Backoff only for network or database transient errors.
**Security Limit:** If the error persists after `n` attempts (e.g., `n=3`), the operation must definitively fail to prevent self-inflicted DoS attacks or thread saturation.
