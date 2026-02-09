# Roadmap: SicNelleApplicazioni

## Overview

This roadmap outlines the phased development of "SicNelleApplicazioni," a secure Java web application for sharing textual content. It prioritizes establishing a robust and secure foundation, followed by secure user authentication and session management, and finally, secure handling of user-generated content to mitigate common web vulnerabilities from the outset.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Secure Platform Foundation** - Establish the fundamental secure infrastructure for the application.
- [ ] **Phase 2: Secure User Authentication & Session Management** - Enable secure user registration, login, and session handling.
- [ ] **Phase 3: Secure Content & File Handling** - Implement secure upload and management of textual content, protected against injection and XSS.
- [x] **Phase 4: Colmare le lacune funzionali che rappresentano vulnerabilità critiche** - Risolvere le vulnerabilità critiche nell'autenticazione, nell'autorizzazione e nella gestione delle sessioni.
- [ ] **Phase 5: Home Page Implementation and File Persistence** - Implementa la funzionalità della Home Page e la persistenza dei file secondo le specifiche tecniche fornite.

## Phase Details

### Phase 1: Secure Platform Foundation
**Goal**: The core application environment is configured with end-to-end secure communication channels.
**Depends on**: Nothing (first phase)
**Requirements**: INFRA-01, INFRA-02
**Success Criteria** (what must be TRUE):
  1. The Apache Tomcat server is accessible only via HTTPS, and all HTTP requests are redirected.
  2. The application successfully connects to the MySQL database using SSL/TLS.
  3. Attempts to connect to Tomcat via HTTP are rejected or redirected to HTTPS.
  4. Attempts to connect to the database without SSL/TLS are rejected.
**Plans**: TBD

### Phase 2: Secure User Authentication & Session Management
**Goal**: Users can securely register, log in, and maintain a secure session with the application.
**Depends on**: Phase 1
**Requirements**: AUTH-01, AUTH-02, AUTH-03, AUTH-04
**Success Criteria** (what must be TRUE):
  1. A new user can successfully register with a password, and the password hash is stored securely using a robust KDF (PBKDF2/BCrypt/SCrypt).
  2. A registered user can successfully log in using their credentials and access authenticated areas of the application.
  3. Upon successful login, session cookies are set with `HttpOnly`, `Secure`, and `SameSite` flags.
  4. The application actively prevents session fixation attacks (e.g., by regenerating session ID on login).
  5. User credentials are handled in memory using `char[]` and securely cleared after use (`Arrays.fill()`).
**Plans**: TBD

### Phase 3: Secure Content & File Handling
**Goal**: Authenticated users can securely upload textual content, and the application protects against SQL Injection and XSS when handling and displaying this content.
**Depends on**: Phase 2
**Requirements**: VALID-01, VALID-02, INJ-01, INJ-02
**Success Criteria** (what must be TRUE):
  1. An authenticated user can successfully upload a file with a `.txt` extension.
  2. The application verifies the uploaded file's real MIME type using Apache Tika and rejects non-text files, even if they have a `.txt` extension.
  3. Textual content (e.g., uploaded text, user input) stored in the database is saved via `PreparedStatement` to prevent SQL Injection.
  4. Any user-generated content displayed on web pages is sanitized to prevent Cross-Site Scripting (XSS).
  5. Attempts to upload files with non-`.txt` extensions or incorrect MIME types are rejected with an appropriate error message.
**Plans**: TBD

### Phase 4: Colmare le lacune funzionali che rappresentano vulnerabilità critiche
**Goal**: Risolvere le vulnerabilità critiche nell'autenticazione, nell'autorizzazione e nella gestione delle sessioni, basandosi sulle lacune funzionali identificate.
**Depends on**: Phase 3
**Requirements**: RF1, RF3, TA6, RF6, RF7 (from user prompt)
**Success Criteria** (what must be TRUE):
  1. Il sistema controlla l'unicità di email e username durante la registrazione, restituendo un errore generico in caso di duplicati.
  2. L'accesso alle risorse protette (es. Home, lista file) richiede autenticazione.
  3. La Home page mostra solo i file dell'utente autenticato, con prevenzione XSS.
  4. La funzionalità di logout invalida la sessione e cancella i cookie.
**Plans**: 04-PLAN.md (generated)

### Phase 5: Home Page Implementation and File Persistence
**Goal**: Implement the Home Page functionality and file persistence according to the provided technical specifications, ensuring secure display and access control.
**Depends on**: Phase 4
**Requirements**: (from user prompt)
**Success Criteria** (what must be TRUE):
  1. La tabella 'contents' è creata con lo schema specificato (UUID ID, FK su user_id, original_name, internal_name).
  2. HomeServlet recupera e mostra solo i file dell'utente loggato.
  3. home.jsp visualizza i file in una tabella con Nome File, Tipo, Data Caricamento e Azioni, usando <c:out> per XSS.
  4. home.jsp ha un link di logout funzionante.
  5. AuthenticationFilter blocca l'accesso non autenticato a home.jsp e alla servlet /home.
**Plans**: 05-PLAN.md (generated)

## Progress

| Phase | Status |
|-------|--------|
| 1     | Completed |
| 2     | Completed |
| 3     | Completed |
| 4     | Completed |
| 5     | Plans |



