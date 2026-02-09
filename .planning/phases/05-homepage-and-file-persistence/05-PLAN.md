# Phase 5: Home Page Implementation and File Persistence

## Goal
Implement the Home Page functionality and file persistence according to the provided technical specifications, ensuring secure display and access control.

## Dependencies
Phase 4 (implicitly, as it builds upon the current state of the application)

## Tasks

### [x] Task 1: Database - Create `contents` table - COMPLETED
**Description**: Crea la tabella `contents` utilizzando lo schema SQL fornito.
**Implementation Details**:
*   Generare lo script SQL per la tabella `contents`.
*   ID per la tabella `contents` deve essere di tipo UUID.
*   `user_id` sarà una Foreign Key che punta alla tabella `users` (di tipo INT).
*   Deve distinguere tra `original_name` (nome originale del file caricato) e `internal_name` (nome con cui il file viene salvato internamente, es. UUID).
**Acceptance Criteria**:
*   La tabella `contents` è creata nel database con lo schema specificato.
*   L'ID è di tipo UUID.
*   Esiste una relazione di chiave esterna (`user_id`) di tipo INT con la tabella `users`.

### [x] Task 2: `HomeServlet` Implementation - COMPLETED
**Description**: La `HomeServlet` deve recuperare l'ID utente dalla sessione e interrogare il database tramite `ContentRepository.findByUserId(userId)`. Non devono essere mostrati file di altri utenti.
**Implementation Details**:
*   Assicurarsi che la servlet mappata a `/home` (attualmente `DisplayContentServlet.java`) recuperi correttamente il `userId` dalla sessione.
*   Utilizzare `ContentRepository.findByUserId(userId)` per recuperare solo i contenuti associati a quell'ID utente.
*   Passare la lista filtrata di `Content` alla `home.jsp`.
**Acceptance Criteria**:
*   La `HomeServlet` recupera correttamente i file solo per l'utente loggato.
*   Nessun file di altri utenti viene mai esposto tramite questa servlet.

### Task 3: `home.jsp` Implementation
**Description**: Visualizza una tabella con: Nome File, Tipo, Data Caricamento e Azioni.
**Implementation Details**:
*   Modificare `home.jsp` per creare una tabella HTML.
*   Le colonne della tabella saranno: "Nome File", "Tipo" (MIME type o estensione, se Type non è disponibile nel Content model), "Data Caricamento", e una colonna "Azioni" (per futuri link di download/visualizzazione, per ora può essere vuota o con un placeholder).
*   **IMPORTANTE**: Usa `<c:out>` per visualizzare il nome del file (`original_name`) per prevenire Stored XSS (TA5).
*   Aggiungere un link per il Logout che punti alla `LogoutServlet`.
**Acceptance Criteria**:
*   `home.jsp` mostra una tabella ben formattata dei file dell'utente.
*   I nomi dei file visualizzati sono visualizzati in modo sicuro utilizzando `<c:out>`.
*   È presente un link di logout funzionante.

### Task 4: Authentication Filter Verification
**Description**: Assicurati che il filtro creato nei task precedenti blocchi l'accesso a `home.jsp` e alla relativa Servlet se la sessione non contiene un `userId` valido.
**Implementation Details**:
*   Verificare la configurazione di `AuthenticationFilter` in `web.xml` per assicurarsi che `home.jsp` e la servlet mappata a `/home` siano protetti.
*   Confermare che la logica dell'`AuthenticationFilter` reindirizzi correttamente l'utente non autenticato a `login.jsp`.
**Acceptance Criteria**:
*   L'accesso diretto a `home.jsp` o alla `HomeServlet` senza autenticazione valida viene bloccato.
*   Gli utenti non autenticati vengono reindirizzati alla pagina di login.

## Rigid Constraints Adhered To:
*   Maintain the current SQL schema for the user.
*   Do not add unnecessary external libraries.
*   Do not reintroduce CAPTCHA.
*   Do not reintroduce email verification.

## Next Steps
This plan will be used by the planning agent to create detailed execution steps.
