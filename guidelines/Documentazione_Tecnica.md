# Documentazione Tecnica

## 5.1 Analisi Statica (Implementazione della Sicurezza)

### 3.1 Gestione Sicura dei Cookie

La gestione sicura dei cookie è fondamentale per proteggere gli utenti da attacchi come Cross-Site Scripting (XSS) e Cross-Site Request Forgery (CSRF). L'utilizzo di flag come `HttpOnly`, `Secure` e `SameSite` contribuisce a prevenire l'accesso non autorizzato ai cookie e a garantire che vengano trasmessi solo su connessioni sicure e in contesti appropriati.

Nel progetto, la configurazione di base per i cookie di sessione è definita nel file `web.xml`, dove sono impostati i flag `HttpOnly` e `Secure` a `true` per tutti i cookie di sessione. Questo assicura che i cookie non siano accessibili via JavaScript (riducendo il rischio XSS) e siano inviati solo su connessioni HTTPS.
Inoltre, il `CookieSecurityFilter` intercetta l'header `Set-Cookie` e aggiunge il flag `SameSite=Strict` al cookie `JSESSIONID` se non già presente. Questa misura rafforza la protezione contro gli attacchi CSRF, impedendo al browser di inviare il cookie in richieste cross-site.

> **[SCREENSHOT RICHIESTO]**: Configurazione cookie - web.xml e CookieSecurityFilter.java - Dettaglio: Configurazione `<cookie-config>` nel web.xml e l'aggiunta di `SameSite=Strict` nel `CookieSecurityFilter`.

### 3.2 Gestione Sessione HTTP

Una corretta gestione delle sessioni è cruciale per mantenere lo stato dell'autenticazione e prevenire attacchi di Session Fixation.

Nel `LoginServlet.java`, dopo un'autenticazione riuscita, viene implementata una robusta mitigazione della Session Fixation. Qualsiasi sessione esistente viene invalidata (`oldSession.invalidate();`), e successivamente viene creata una nuova sessione per l'utente. L'ID della sessione viene esplicitamente cambiato (`req.changeSessionId();`), garantendo che un attaccante non possa sfruttare un ID di sessione pre-esistente e potenzialmente compromesso. Il timeout della sessione viene impostato a 30 minuti (`session.setMaxInactiveInterval(1800);`), limitando la finestra temporale in cui una sessione rubata potrebbe essere utilizzata.
Nel `LogoutServlet.java`, la sessione viene invalidata (`session.invalidate();`) e i cookie di sessione vengono rimossi impostando la loro scadenza immediata (`cookie.setMaxAge(0);`). Vengono anche impostati header `Cache-Control` per prevenire la memorizzazione nella cache di pagine contenenti dati sensibili dopo il logout.

> **[SCREENSHOT RICHIESTO]**: Codice di gestione sessione e session.invalidate() nel logout - LoginServlet.java e LogoutServlet.java - Dettaglio: `req.changeSessionId()` e `session.invalidate()` nel `LoginServlet` e `LogoutServlet`.

### 3.3 Caricamento Sicuro dei File (Upload)

La funzionalità di upload dei file rappresenta un punto di ingresso critico per potenziali attacchi. È essenziale implementare controlli rigorosi per prevenire l'upload di file malevoli o l'esecuzione di codice non autorizzato.

Il `FileUploadServlet.java` implementa diverse misure di sicurezza. Inizialmente, il nome del file originale viene sanitizzato (`originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");`) per prevenire attacchi di Path Traversal e Stored XSS. Viene eseguito un controllo sull'estensione del file, accettando solo `.txt`.
Dopo aver copiato il file in una directory temporanea, viene utilizzato Apache Tika (`tika.detect(tempFilePath.toFile());`) per determinare il MIME-type reale del contenuto del file. Questo previene gli attacchi di tipo "Estensione Fake" dove un file malevolo viene rinominato con un'estensione lecita. Se il MIME-type rilevato non è `text/plain`, il file viene rifiutato.
Infine, il file viene rinominato con un UUID univoco (`UUID.randomUUID().toString() + ".txt";`) e spostato in una posizione di archiviazione sicura (`TARGET_STORAGE_PATH`) esterna alla root del web server, impedendo l'esecuzione diretta del file caricato come codice lato server.

> **[SCREENSHOT RICHIESTO]**: Blocco di codice con validazione Tika e salvataggio file - FileUploadServlet.java - Dettaglio: La sezione dove viene usato `tika.detect` e dove il file viene rinominato e spostato (`Files.move`).

### 3.4 Protezione contro Injection (SQLi & XSS)

Gli attacchi di Injection, come SQL Injection (SQLi) e Cross-Site Scripting (XSS), sono tra le vulnerabilità più comuni e pericolose. Una difesa robusta è essenziale per proteggere l'integrità dei dati e la sicurezza degli utenti.

**SQL Injection (SQLi):**
Per prevenire le SQL Injection, tutte le interazioni con il database avvengono tramite l'uso di `PreparedStatement` in classi come `JdbcUserRepository.java` e `JdbcContentRepository.java`. I `PreparedStatement` parametrizzano le query SQL, separando i dati di input dal codice SQL, impedendo così che input malevoli vengano interpretati come parte della logica della query.

> **[SCREENSHOT RICHIESTO]**: Query SQL con ? nel DAO - JdbcUserRepository.java (es. metodo `save` o `findByEmail`) - Dettaglio: Una query SQL con segnaposto `?` e l'uso di `pstmt.setString()` o `pstmt.setLong()`.

**Cross-Site Scripting (XSS):**
Per la protezione da XSS, è fondamentale che tutti i dati generati dall'utente o provenienti da sorgenti esterne siano correttamente sanificati o codificati prima di essere visualizzati nelle pagine web. Nel contesto attuale, non è stata identificata una classe di utilità centralizzata per l'HTML escaping. Si assume che la protezione XSS debba essere applicata a livello di presentazione (nelle JSP) codificando l'output (`<%= someUserGeneratedContent %>` dovrebbe essere `fn:escapeXml(someUserGeneratedContent)` o equivalente).

> **[SCREENSHOT RICHIESTO]**: Metodo di sanitizzazione input XSS - [NomeFile.java o NomeFile.jsp] - Dettaglio: Esempio di una funzione `escapeHtml` (se presente) o un'istruzione JSTL come `<c:out value="${userContent}" escapeXml="true"/>` in una JSP.

### 3.5 Gestione sicura delle chiavi e crittografia

La gestione sicura delle chiavi crittografiche è vitale per proteggere informazioni sensibili, come le credenziali del database.

La classe `DbManager.java` implementa un approccio robusto per la gestione delle credenziali del database. Le password del database non sono memorizzate in chiaro; sono invece criptate nel file `application.properties`. `DbManager` legge il percorso di un `keystore.p12`, la sua password, l'alias della chiave e la password della chiave specifica dal `ConfigManager`. Utilizza quindi un `KeyStore` (formato PKCS12) per caricare una `SecretKey` (masterKey) che, insieme a un `IvParameterSpec` (Initialisation Vector) anch'esso letto dalle proprietà, viene usata per decriptare la password del database utilizzando il `EncryptionUtil.java`. Questo meccanismo garantisce che la password del database sia decriptata solo a runtime e che le chiavi necessarie per tale operazione siano conservate in un keystore protetto.

> **[SCREENSHOT RICHIESTO]**: Gestione chiavi e crittografia - DbManager.java e EncryptionUtil.java - Dettaglio: Sezione di `DbManager` dove vengono caricati il keystore e la master key, e la funzione `decrypt` in `EncryptionUtil`.

### 3.6 Gestione Credenziali (Password)

La gestione sicura delle credenziali utente, in particolare delle password, è un pilastro fondamentale della sicurezza delle applicazioni.

La classe `PasswordUtil.java` è dedicata alla gestione sicura delle password. Utilizza l'algoritmo di hashing `PBKDF2WithHmacSHA256` con un numero elevato di iterazioni (`600000`) e una lunghezza della chiave di 256 bit. Ogni password viene salata individualmente con un sale (`salt`) generato casualmente utilizzando `SecureRandom`, prevenendo così attacchi basati su tabelle arcobaleno (rainbow tables). La password viene passata come `char[]` per evitare che rimanga in stringhe immutabili in memoria più a lungo del necessario, e la pratica di cancellare l'array di caratteri dopo l'uso (implementata ad esempio in `LoginServlet`) riduce ulteriormente il rischio di esposizione.

> **[SCREENSHOT RICHIESTO]**: Metodo che esegue l'hashing della password - PasswordUtil.java - Dettaglio: Il metodo `hashPassword` e la definizione di `ITERATIONS`, `KEY_LENGTH` e `ALGORITHM`.

### 3.7 Programmazione Difensiva

La programmazione difensiva è una pratica che mira a rendere il software più robusto e sicuro anticipando errori e comportamenti imprevisti. Una parte cruciale di ciò è la gestione delle eccezioni che non rivela dettagli interni sensibili all'utente.

Nel progetto, un esempio di programmazione difensiva si trova nella gestione degli errori a livello di applicazione tramite la configurazione di `error-page` nel `web.xml`. Questo assicura che in caso di errori interni del server (es. 500 Internal Server Error) o risorse non trovate (es. 404 Not Found), l'utente venga reindirizzato a pagine di errore generiche (`error500.jsp`, `error404.jsp`) invece di visualizzare stack trace che potrebbero fornire informazioni utili a un attaccante. Inoltre, servlets come `FileUploadServlet` includono blocchi `try-catch` robusti che catturano eccezioni, le registrano internamente (`LOGGER.log(...)`) e presentano messaggi di errore generici all'utente, evitando di esporre dettagli tecnici sul funzionamento interno.

> **[SCREENSHOT RICHIESTO]**: Un blocco try-catch significativo nel Controller - FileUploadServlet.java (metodo `doPost` o `handleError`) - Dettaglio: Un blocco `try-catch` che cattura un'eccezione, la logga e mostra un messaggio generico all'utente.

### 3.8 Gestione della Concorrenza

La gestione della concorrenza è fondamentale nelle applicazioni multi-thread per garantire l'integrità dei dati e prevenire race condition quando più utenti o processi accedono contemporaneamente a risorse condivise.

Nel progetto, la gestione della concorrenza è evidente nell'implementazione dei repository e nelle operazioni sui file. La classe `JdbcContentRepository.java` dichiara i suoi metodi principali (`save`, `findById`, `findByInternalName`, `findByUserId`, `findAll`, `delete`) come `synchronized`. Questo assicura che solo un thread alla volta possa eseguire uno di questi metodi su una data istanza del repository, prevenendo potenziali race condition durante l'accesso e la modifica dei dati nel database. Per quanto riguarda l'upload dei file, `FileUploadServlet` utilizza file temporanei unici per ogni upload e il rilocamento atomico (`Files.move`) del file finale, riducendo le opportunità di conflitto su risorse di file system condivise.

> **[SCREENSHOT RICHIESTO]**: Il blocco di codice sincronizzato - JdbcContentRepository.java - Dettaglio: La dichiarazione `synchronized` su uno dei metodi del repository, ad esempio `save` o `findById`.

## 5.2 Analisi Dinamica (Testing)

### A. Test d'Uso (TU) - Funzionalità Corrette

Questa sezione documenta i test d'uso, che verificano il corretto funzionamento delle funzionalità dell'applicazione da una prospettiva utente.

| ID Test | Descrizione | Input | Esito Atteso | Screenshot |
| :------ | :---------- | :---- | :----------- | :--------- |
| TU1 | Registrazione nuovo utente | Username: `testuser`, Email: `test@example.com`, Password: `SecureP@ss1` | Registrazione riuscita, reindirizzamento alla pagina di login o home. | > [SCREEN: Browser con esito] |
| TU2 | Login con credenziali corrette | Email: `test@example.com`, Password: `SecureP@ss1` | Login riuscito, reindirizzamento alla home page. | > [SCREEN: Browser con esito] |
| TU3 | Login con password errata | Email: `test@example.com`, Password: `WrongP@ss` | Login fallito, messaggio di errore generico "Credenziali non valide. Riprova." | > [SCREEN: Browser con esito] |
| TU4 | Upload file TXT valido | File: `documento.txt` con contenuto "Testo di prova." | Upload riuscito, messaggio di successo, file visibile nella lista. | > [SCREEN: Browser con esito] |
| TU5 | Upload file JPG valido | *Non applicabile con l'attuale filtro .txt* | *Non applicabile con l'attuale filtro .txt* | *Non applicabile con l'attuale filtro .txt* |
| TU6 | Visualizzazione lista file caricati | Utente loggato | Lista dei file caricati dall'utente corrente e dagli altri utenti visualizzata correttamente. | > [SCREEN: Browser con esito] |
| TU7 | Download del file caricato | Click su link di download per `documento.txt` | Download del file `documento.txt` riuscito. | > [SCREEN: Browser con esito] |
| TU8 | Modifica profilo/password (se presente) o altra operazione utente | *Assenza funzionalità di modifica profilo/password diretta nel progetto analizzato* | *Non applicabile con l'attuale struttura* | *Non applicabile con l'attuale struttura* |
| TU9 | Logout | Click su link di logout | Reindirizzamento alla pagina di login, sessione invalidata. | > [SCREEN: Browser con esito] |
| TU10 | Tentativo accesso pagina protetta dopo Logout | Tentativo di accedere a `/home` dopo il logout | Reindirizzamento alla pagina di login. | > [SCREEN: Browser con esito] |

### B. Test di Abuso (TA) - Attacchi Simulati

Questa sezione descrive i test di abuso, che simulano attacchi per verificare la robustezza dell'applicazione contro vulnerabilità comuni.

| ID | Input Fornito | Comportamento Atteso | Comportamento Osservato (Codice) | Contromisura Applicata | Screenshot |
| :-- | :------------ | :------------------- | :------------------------------- | :--------------------- | :--------- |
| TA1 | SQL Injection: `' OR '1'='1` nel campo email di login. | Accesso negato, nessun errore SQL a video. | `JdbcUserRepository` impedisce l'iniezione. | PreparedStatement (Java JDBC) | > [SCREEN: Browser con esito] |
| TA2 | Bypass Auth: URL diretto a `/home` senza login. | Redirect al login. | `SessionControlFilter` o `LoginServlet` reindirizza. | Session Filter / Controllo `session.getAttribute("user")` | > [SCREEN: Browser con esito] |
| TA3 | Estensione Vietata: Upload di `virus.exe`. | Rifiuto caricamento. | `FileUploadServlet` rifiuta estensioni diverse da `.txt`. | Whitelist estensioni (`.txt`) | > [SCREEN: Browser con esito] |
| TA4 | Estensione Fake/MIME: File `malware.exe` rinominato in `doc.txt`. | Rifiuto basato sul contenuto (Magic Number). | `FileUploadServlet` analizza il MIME-type con Apache Tika. | Analisi contenuto con Apache Tika | > [SCREEN: Browser con esito] |
| TA5 | Stored XSS: Caricamento file con nome `<script>alert(1)</script>.txt` o descrizione malevola. | Il testo viene mostrato come stringa letterale, lo script non parte. | `FileUploadServlet` sanitizza il nome del file; XSS output encoding in JSP. | Sanitizzazione nome file; Output Encoding / HTML Escaping | > [SCREEN: Browser con esito] |
| TA6 | Accesso Risorse Protette: Tentativo di scaricare file di un altro utente (ID manipulation) o accesso a cartella `/WEB-INF`. | 403 Forbidden o 404 Not Found. | Controllo titolarità risorsa (es. in `DownloadServlet`); `/WEB-INF` è protetto. | Controllo titolarità risorsa; Struttura `/WEB-INF` | > [SCREEN: Browser con esito] |
| TA7 | Session Replay/Fixation: Login, cattura JSESSIONID, logout, tentativo riuso ID vecchio. | Richiesta nuova login. | `LoginServlet` invalida la vecchia sessione e cambia l'ID; `LogoutServlet` invalida la sessione. | `session.invalidate()` e `req.changeSessionId()` al login/logout | > [SCREEN: Browser con esito] |
| TA8 | Esecuzione File Caricati: Tentativo di navigare direttamente a `/uploads/shell.jsp` (se fosse possibile). | Il server non esegue il file o lo scarica come testo/blob. | File caricati fuori dalla web root; Server non esegue `.txt` come codice. | Upload fuori dalla root; Server configurato per non eseguire `.txt` | > [SCREEN: Browser con esito] |
