# Phase 4: Functional Gaps & Critical Vulnerabilities

## Goal
Colmare le lacune funzionali che rappresentano vulnerabilità critiche (secondo la traccia d'esame) ignorando CAPTCHA e verifica email. Il progetto ha ora una solida base di sicurezza (BCrypt, Schema SQL pulito, build passante). CAPTCHA e verifica email non devono essere reintrodotti.

## Dependencies
Phase 3 (implicitly, as it builds upon the current state of the application after previous refactorings)

## Tasks

### [x] Task 1: Validazione Unicità Registrazione (RF1) - COMPLETED
**Description**: Attualmente il sistema non controlla se l'email o lo username esistono già. Modificare il `RegistrationService` per effettuare una query preventiva sul DB. In caso di duplicato, restituire un messaggio di errore generico secondo la policy RF8 (es. "Credenziali non valide o già in uso").
**Implementation Details**:
*   Modificare `RegistrationService.java`.
*   Prima di salvare un nuovo utente, utilizzare i metodi `userRepository.findByUsername(username)` e `userRepository.findByEmail(email)` per verificare l'esistenza.
*   Se uno dei due campi è duplicato, il metodo `register` di `RegistrationService` deve restituire `false`.
*   Il `RegistrationServlet` gestirà il messaggio di errore generico.
*   **Decision (RF8):** Il messaggio di errore generico per la registrazione duplicata sarà: "Impossibile completare la registrazione. I dati inseriti non sono validi o sono già associati a un account".
**Acceptance Criteria**:
*   Non è possibile registrare due utenti con lo stesso username.
*   Non è possibile registrare due utenti con la stessa email.
*   Il messaggio di errore mostrato all'utente in caso di duplicato è generico e non rivela se è lo username o l'email ad essere già in uso.

### [x] Task 2: Protezione Accesso Home Page (RF3 / TA6) - COMPLETED
**Description**: Attualmente la Home e la lista dei file sono accessibili senza login. Implementare un controllo (Filter o logica nella Servlet) che verifichi l'esistenza di una sessione valida. Se l'utente non è autenticato, deve essere reindirizzato a `login.jsp`.
**Implementation Details**:
*   Creare un nuovo `javax.servlet.Filter` chiamato `AuthenticationFilter`.
*   **Decision (Filter Configuration):** Configurare `AuthenticationFilter` in `web.xml` per proteggere *tutto* tranne `/login`, `/register` e le risorse statiche in `/css`.
*   All'interno del filtro, verificare `session.getAttribute("userId")` e `session.getAttribute("email")`.
*   Se l'attributo di sessione non esiste o non è valido, reindirizzare la richiesta a `login.jsp`.
*   **Decision (Session Fixation Mitigation):** Dopo un'autenticazione riuscita, nel `LoginServlet`, utilizzare `request.changeSessionId()` (se su Servlet 3.1+) o invalidare la vecchia sessione e crearne una nuova subito dopo l'autenticazione.
**Acceptance Criteria**:
*   La Home page è accessibile solo agli utenti autenticati.
*   La visualizzazione dei file è accessibile solo agli utenti autenticati.
*   Gli utenti non autenticati che tentano di accedere a risorse protette vengono reindirizzati a `login.jsp`.
*   La Session Fixation è mitigata tramite la rigenerazione dell'ID di sessione dopo il login.

### Task 3: Visualizzazione File Utente (RF6)
**Description**: Creare/Aggiornare la Home Page in modo che mostri solo la lista dei file caricati dall'utente attualmente loggato (identificato tramite l'ID utente in sessione). Assicurarsi che i nomi dei file siano visualizzati usando `<c:out>` per prevenire XSS.
**Implementation Details**:
*   Modificare `home.jsp` per visualizzare i file in una tabella.
*   Modificare o creare una servlet (es. `DisplayContentServlet.java` o una nuova `UserContentServlet.java`) che recuperi l'ID dell'utente dalla sessione (memorizzato come `userId`).
*   **Decision (ContentRepository):** Il `ContentRepository` deve implementare `findByUserId(Long userId)` per filtrare i contenuti lato database.
*   Passare la lista dei file alla JSP.
*   Nella JSP, iterare sulla lista e visualizzare ogni nome di file utilizzando `<c:out value="${fileName}" />` per garantire la prevenzione da XSS.
**Acceptance Criteria**:
*   La Home page mostra solo i file caricati dall'utente loggato.
*   I nomi dei file visualizzati sono correttamente sanitizzati per prevenire XSS.
*   Se l'utente non ha caricato file, viene mostrato un messaggio appropriato.

### Task 4: Terminazione Sessione (RF7)
**Description**: Implementare la `LogoutServlet` che provveda a invalidare la sessione (`session.invalidate()`) e a cancellare i cookie di sessione.
**Implementation Details**:
*   Creare una nuova servlet `LogoutServlet.java` mappata a `/logout`.
*   All'interno del metodo `doGet` o `doPost` (o entrambi), richiamare `session.invalidate()` per terminare la sessione.
*   Implementare la logica per rimuovere i cookie di sessione (es. `JSESSIONID`) impostando la loro età a 0 e riaggiungendoli alla risposta.
*   **Decision (Cookie Flags RF4):** Assicurarsi che nel `web.xml` (o via codice, se applicabile) i cookie di sessione siano configurati con i flag `HttpOnly` (per mitigare XSS) e `Secure` (se si usa HTTPS), come richiesto esplicitamente nel PDF Cookie_Security_Java.pdf.
*   **Decision (No-Cache Headers):** Al logout, impostare gli header `Cache-Control: no-cache, no-store, must-revalidate`, `Pragma: no-cache`, `Expires: 0` sulla risposta per prevenire la memorizzazione nella cache della pagina di logout.
*   Reindirizzare l'utente a `login.jsp` dopo il logout.
**Acceptance Criteria**:
*   Quando un utente clicca sul link/pulsante di logout, la sessione viene invalidata.
*   I cookie di sessione vengono rimossi dal browser dell'utente.
*   L'utente viene reindirizzato alla pagina di login.
*   I cookie di sessione sono configurati con i flag `HttpOnly` e `Secure`.
*   Gli header no-cache sono impostati sulla risposta dopo il logout.

## Rigid Constraints Adhered To:
*   Maintain the current SQL schema for the user.
*   Do not add unnecessary external libraries.
*   Do not reintroduce CAPTCHA.

## Next Steps
This plan will be used by the planning agent to create detailed execution steps.