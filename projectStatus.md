# Technical Baseline Document

### Stato Funzionale

*   **Autenticazione Utente**: Implementati i flussi di registrazione, login e logout con gestione della sessione tramite `HttpSession` e filtri (`AuthenticationFilter`, `SessionControlFilter`). Le password sono gestite con hashing (`PasswordUtil`).
*   **Gestione File**: È possibile l'upload di file `.txt` con validazione del MIME type (via Apache Tika) e salvataggio su un percorso locale. La home page mostra una tabella di tutti i file caricati da tutti gli utenti. È funzionante la visualizzazione del contenuto testuale di un file in una pagina dedicata e sicura (prevenzione XSS tramite `<c:out>`).
*   **Sicurezza**:
    *   **Cookie di Sessione**: Flag `HttpOnly`, `Secure` e `SameSite=Strict` sono configurati.
    *   **Protezioni Aggiuntive**: Sono attivi filtri per la prevenzione del clickjacking (`X-Frame-Options`) e per il rate limiting sull'endpoint di login.
    *   **Concurrency**: Aggiunti lock `synchronized` a livello di metodo nel repository JDBC e nel servlet di upload per prevenire race condition di base.

---

### Architettura dei File

*   **Controller (Servlet)**: `src/main/java/com/sicnelleapplicazioni/servlet/`
    *   Contiene la logica di gestione delle richieste HTTP per ogni feature (login, registrazione, upload, visualizzazione lista/contenuto).
*   **Data Access (Repository)**: `src/main/java/com/sicnelleapplicazioni/repository/`
    *   La persistenza è gestita tramite `JdbcContentRepository` e `JdbcUserRepository`, che usano JDBC puro con credenziali e URL hardcoded.
*   **Business Logic (Service)**: `src/main/java/com/sicnelleapplicazioni/service/`
    *   Logica di business per registrazione e login, separata dai servlet.
*   **Filtri (Cross-Cutting)**: `src/main/java/com/sicnelleapplicazioni/filter/`
    *   Gestiscono sicurezza, autenticazione e controllo sessione a livello di richiesta.
*   **Modello Dati (POJO)**: `src/main/java/com/sicnelleapplicazioni/model/`
    *   Classi `User` e `Content` che mappano le tabelle del database.
*   **Viste (View)**: `src/main/webapp/`
    *   Pagine JSP che renderizzano l'interfaccia utente.
*   **Configurazione**:
    *   `pom.xml`: Gestione dipendenze Maven.
    *   `web.xml`: Dichiarazione e mapping di servlet/filtri, configurazione della sessione.

---

### Dipendenze e Integrazioni

*   **Stack Principale**: Java 11, Servlet 4.0, JSP/JSTL.
*   **Database**: Microsoft SQL Server (connessione diretta via `mssql-jdbc`). Non è presente un connection pool.
*   **Librerie Chiave**:
    *   **Apache Tika**: Rilevamento e validazione del tipo di file.
    *   **Google Guava**: Utilizzato per l'implementazione del `RateLimitingFilter`.
    *   **commons-io**: Utility per la gestione di file e stream.
*   **Tooling**: Apache Maven per la gestione del build e delle dipendenze.
*   **Testing**: JUnit 5 e Mockito per l'unit testing.

---

### Task Residui

*   **Database**: Sostituire la connessione JDBC diretta con un connection pool (es. HikariCP) gestito tramite JNDI.
*   **Configurazione**: Rimuovere i percorsi hardcoded (es. path di upload in `FileUploadServlet`) e le credenziali del DB, esternalizzandoli in un file di properties.
*   **Funzionalità Mancanti**: Le azioni "Scarica" ed "Elimina" file non sono implementate.
*   **API Layer**: Manca un vero e proprio layer di API REST. La logica è strettamente accoppiata alle viste JSP.
*   **Logging**: Il logging si basa su `java.util.logging`. Manca una configurazione robusta con un framework moderno (es. Log4j2 o SLF4J).
*   **Frontend**: L'UI è funzionale ma minimale e richiede un refactoring significativo per usabilità e manutenibilità.
*   **No Logging Framework:** Logging is done via `java.util.logging`. A more robust framework like Log4j2 or SLF4J should be configured.
*   **Missing Features:** No user profile page, no password recovery, no admin panel.
*   **File Storage:** Files are stored directly on the local filesystem. For a scalable application, using a dedicated object store (like AWS S3) would be better.

---

### Input per GSD

1.  **`Initialize Secure DataSource`**: Configure a production-ready database connection pool (HikariCP) using JNDI in the web server and refactor repository classes to use it. Eliminate hardcoded credentials.
2.  **`Implement Externalized Configuration`**: Create a properties file (`app.properties`) to store all external configurations (database URLs, file storage paths, etc.) and implement a utility class to load them securely at startup.
3.  **`Build Core User & Content APIs`**: Develop a set of RESTful endpoints (e.g., using JAX-RS or Spring MVC) for core functionalities: user registration, login, file upload, file list, file view. This separates the backend logic from the JSP views.
4.  **`Develop Frontend with Modern UI Kit`**: Rebuild the UI as a Single Page Application (SPA) using a modern framework (like React or Vue.js) that consumes the new RESTful APIs. Use a component library like Bootstrap or Material-UI for a consistent look and feel.
5.  **`Establish Logging and Error Handling Framework`**: Integrate SLF4J with Logback for structured logging. Implement a global exception handler for the RESTful APIs to return standardized error responses.
6.  **`Containerize Application`**: Create a `Dockerfile` for the application and a `docker-compose.yml` file to run the application and its database (e.g., SQL Server) in a containerized environment for consistent development and deployment.
7.  **`Implement File Abstraction Layer`**: Create a `FileStorageService` interface with implementations for local filesystem storage and a cloud-based object store (like AWS S3 or MinIO). This makes the storage backend pluggable.