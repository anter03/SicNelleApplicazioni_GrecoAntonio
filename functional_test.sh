#!/bin/bash

# functional_test.sh
# Questo script esegue test funzionali e di sicurezza su un'applicazione Java (WAR) deployata su Apache Tomcat.
# Utilizza curl per interagire con l'applicazione e bash per la logica di test.

# Esporta le variabili d'ambiente di Tomcat per assicurarsi che gli script di Tomcat usino l'istanza corretta.
export CATALINA_HOME=/usr/share/tomcat9
export CATALINA_BASE=/var/lib/tomcat9

# --- Configurazione ---
BASE_URL="http://localhost:8080"
APP_NAME="sicnelleapplicazioni" # Assumiamo che questo sia il context root. Potrebbe essere 'sicurezzaNelleApplicazioni' o 'sicnelleapplicazioni-1.0-SNAPSHOT' a seconda della configurazione di deployment.
APP_URL="${BASE_URL}/${APP_NAME}"
COOKIES_FILE="cookies.txt"
PRE_LOGIN_COOKIES_FILE="cookies_prelogin.txt"
TEST_USER="testuser"
TEST_PASSWORD="Sicurezza2025!"
UPLOAD_FILE="test.txt"
EXIT_CODE=0 # 0 per successo, 1 per fallimento

# --- Funzioni di Utility per il Logging ---
log_info() {
    echo "[INFO] $1"
}

log_success() {
    echo -e "\033[0;32m[SUCCESS] $1\033[0m"
}

log_error() {
    echo -e "\033[0;31m[ERROR] $1\033[0m"
    EXIT_CODE=1 # Imposta il codice di uscita a 1 se si verifica un errore
}

# --- Pre-requisiti: Pulizia degli artefatti dei test precedenti ---
log_info "Pulizia degli artefatti dei test precedenti..."
rm -f "${COOKIES_FILE}" "${PRE_LOGIN_COOKIES_FILE}" "${UPLOAD_FILE}"
echo "" # Nuova riga per leggibilità

# --- Task 1: Verifica Rilascio (Deployment) ---
log_info "Task 1: Verifica del deployment dell'applicazione su ${APP_URL}..."

# Verifica che la directory del WAR espanso esista
WAR_DEPLOYED_DIR="${CATALINA_BASE}/webapps/${APP_NAME}/"
if [ -d "$WAR_DEPLOYED_DIR" ]; then
    log_success "Verifica deployment: Directory WAR espansa trovata: ${WAR_DEPLOYED_DIR}"
else
    log_error "Verifica deployment fallita: Directory WAR espansa NON trovata in ${WAR_DEPLOYED_DIR}. Assicurati che il WAR sia deployato correttamente."
    exit 1 # Termina lo script se il deployment non è verificato
fi

# Controlla lo stato HTTP della root dell'applicazione
# Ci aspettiamo 200 per accesso diretto, 302 per un redirect a login/home
DEPLOYMENT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${APP_URL}/")
if [ "$DEPLOYMENT_STATUS" -eq 200 ] || [ "$DEPLOYMENT_STATUS" -eq 302 ]; then
    log_success "Verifica deployment passata: L'applicazione ha risposto con stato ${DEPLOYMENT_STATUS}."
else
    log_error "Verifica deployment fallita: L'applicazione ha risposto con stato ${DEPLOYMENT_STATUS}. Atteso 200 o 302."
    exit 1 # Termina lo script se il deployment non è verificato
fi
echo ""

# --- Task 2: Coding del Test Funzionale in Bash ---

# RF1 (Registrazione)
log_info "RF1: Registrazione dell'utente '${TEST_USER}'..."
REGISTER_RESPONSE=$(curl -s -c "${COOKIES_FILE}" -b "${COOKIES_FILE}" -X POST 
    -d "username=${TEST_USER}&password=${TEST_PASSWORD}" 
    "${APP_URL}/register")

# Verifica il messaggio di registrazione riuscita o "utente già esistente" se si ri-esegue il test
if echo "${REGISTER_RESPONSE}" | grep -qi "Registration successful" || echo "${REGISTER_RESPONSE}" | grep -qi "User already exists" || echo "${REGISTER_RESPONSE}" | grep -qi "utente già registrato"; then
    log_success "RF1: Registrazione riuscita per '${TEST_USER}' o utente già esistente."
else
    log_error "RF1: Registrazione fallita. Risposta: ${REGISTER_RESPONSE}"
    EXIT_CODE=1
fi
echo ""

# Controllo di sicurezza: Session Fixation (Cattura del cookie pre-login)
log_info "Controllo di sicurezza (Session Fixation): Tentativo di catturare JSESSIONID pre-login..."
# Accedi alla pagina /login (o a qualsiasi pagina pubblica) per ottenere un potenziale cookie di sessione prima dell'autenticazione
curl -v -s -o /dev/null -c "${PRE_LOGIN_COOKIES_FILE}" -b "${PRE_LOGIN_COOKIES_FILE}" "${APP_URL}/login" > /dev/null 2>&1
PRE_LOGIN_JSESSIONID=$(grep -oP 'JSESSIONID=\K[^;]+' "${PRE_LOGIN_COOKIES_FILE}" | head -n 1)

if [ -n "${PRE_LOGIN_JSESSIONID}" ]; then
    log_info "JSESSIONID pre-login trovato: ${PRE_LOGIN_JSESSIONID}"
else
    log_info "Nessun JSESSIONID trovato prima del login (potrebbe essere normale se l'app emette la sessione solo dopo l'autenticazione)."
fi

# RF2/RF4 (Login & Cookie)
log_info "RF2/RF4: Login dell'utente '${TEST_USER}' e verifica dei flag del cookie..."
LOGIN_OUTPUT=$(curl -v -s -c "${COOKIES_FILE}" -b "${COOKIES_FILE}" -X POST 
    -d "username=${TEST_USER}&password=${TEST_PASSWORD}" 
    "${APP_URL}/login" 2>&1) # Reindirizza stderr a stdout per catturare gli header

if echo "${LOGIN_OUTPUT}" | grep -qi "Login successful" || echo "${LOGIN_OUTPUT}" | grep -qi "Benvenuto" || echo "${LOGIN_OUTPUT}" | grep -qi "home.jsp"; then # Aggiunto home.jsp come indicatore di successo per un redirect
    log_success "RF2: Login riuscito per ${TEST_USER}."

    # RF4: Verifica l'header Set-Cookie con i flag HttpOnly e Secure
    SET_COOKIE_HEADER=$(echo "${LOGIN_OUTPUT}" | grep -i "Set-Cookie:")
    if echo "${SET_COOKIE_HEADER}" | grep -qi "HttpOnly" && echo "${SET_COOKIE_HEADER}" | grep -qi "Secure"; then
        log_success "RF4: Cookie di sessione trovato con flag HttpOnly e Secure."
    else
        log_error "RF4: Cookie di sessione mancante dei flag HttpOnly e/o Secure. Header: ${SET_COOKIE_HEADER}"
        EXIT_CODE=1
    fi

    # Controllo di sicurezza: Session Fixation (Verifica del cookie post-login)
    POST_LOGIN_JSESSIONID=$(grep -oP 'JSESSIONID=\K[^;]+' "${COOKIES_FILE}" | head -n 1)
    log_info "JSESSIONID post-login: ${POST_LOGIN_JSESSIONID}"

    if [ -n "${PRE_LOGIN_JSESSIONID}" ]; then
        if [ -n "${POST_LOGIN_JSESSIONID}" ] && [ "${PRE_LOGIN_JSESSIONID}" != "${POST_LOGIN_JSESSIONID}" ]; then
            log_success "Controllo di sicurezza (Session Fixation): JSESSIONID è cambiato dopo il login. (Pre: ${PRE_LOGIN_JSESSIONID}, Post: ${POST_LOGIN_JSESSIONID})"
        else
            log_error "Controllo di sicurezza (Session Fixation): JSESSIONID NON è cambiato o non è stato emesso come previsto dopo il login. Questo potrebbe indicare una vulnerabilità di session fixation. (Pre: ${PRE_LOGIN_JSESSIONID}, Post: ${POST_LOGIN_JSESSIONID})"
            EXIT_CODE=1
        fi
    else
        # Se nessun JSESSIONID pre-login è stato emesso, ma uno post-login sì, è una buona pratica.
        if [ -n "${POST_LOGIN_JSESSIONID}" ]; then
            log_success "Controllo di sicurezza (Session Fixation): Nessun JSESSIONID pre-login, ma ne è stato emesso uno nuovo dopo il login. Questa è una buona pratica."
        else
            log_error "Controllo di sicurezza (Session Fixation): Nessun JSESSIONID trovato anche dopo il login riuscito."
            EXIT_CODE=1
        fi
    fi
    rm -f "${PRE_LOGIN_COOKIES_FILE}" # Pulisci il file temporaneo del cookie pre-login

else
    log_error "RF2: Login fallito. Risposta (ultime 10 righe): $(echo "${LOGIN_OUTPUT}" | tail -n 10)" # Mostra le ultime 10 righe dell'output per l'errore
    EXIT_CODE=1
fi
echo ""

# RF5 (Upload)
log_info "RF5: Creazione del file temporaneo e upload..."
echo "Questo è un file di test per RF5." > "${UPLOAD_FILE}"

UPLOAD_RESPONSE=$(curl -s -c "${COOKIES_FILE}" -b "${COOKIES_FILE}" -X POST 
    -F "file=@${UPLOAD_FILE}" 
    "${APP_URL}/upload")

if echo "${UPLOAD_RESPONSE}" | grep -qi "File uploaded successfully" || echo "${UPLOAD_RESPONSE}" | grep -qi "success"; then
    log_success "RF5: File '${UPLOAD_FILE}' caricato con successo."
else
    log_error "RF5: Caricamento del file fallito. Risposta: ${UPLOAD_RESPONSE}"
    EXIT_CODE=1
fi
echo ""

# RF6 (Visualizzazione)
log_info "RF6: Verifica che il file caricato '${UPLOAD_FILE}' sia presente nella pagina di visualizzazione dei contenuti..."
DISPLAY_CONTENT_RESPONSE=$(curl -s -c "${COOKIES_FILE}" -b "${COOKIES_FILE}" "${APP_URL}/displayContent")

if echo "${DISPLAY_CONTENT_RESPONSE}" | grep -q "${UPLOAD_FILE}"; then
    log_success "RF6: '${UPLOAD_FILE}' trovato nella pagina di visualizzazione dei contenuti."
else
    log_error "RF6: '${UPLOAD_FILE}' NON trovato nella pagina di visualizzazione dei contenuti. Snippet di risposta (prime 10 righe): $(echo "${DISPLAY_CONTENT_RESPONSE}" | head -n 10)"
    EXIT_CODE=1
fi
echo ""

# RF7 (Logout)
log_info "RF7: Logout e verifica dell'accesso a risorse protette..."

# Esegui il logout
LOGOUT_RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" -c "${COOKIES_FILE}" -b "${COOKIES_FILE}" "${APP_URL}/logout")
if [ "$LOGOUT_RESPONSE_CODE" -eq 200 ] || [ "$LOGOUT_RESPONSE_CODE" -eq 302 ]; then
    log_success "RF7: Logout avviato (stato ${LOGOUT_RESPONSE_CODE})."
else
    log_error "RF7: Il logout non ha restituito lo stato atteso (stato ${LOGOUT_RESPONSE_CODE})."
    EXIT_CODE=1
fi

# Pulisci cookies.txt dopo il logout per simulare la terminazione della sessione
rm -f "${COOKIES_FILE}"
log_info "RF7: Cookie di sessione cancellati per simulare la terminazione della sessione."

# Tenta di accedere a una risorsa protetta (es. home.jsp o displayContent.jsp)
# Assumiamo che home.jsp sia una pagina protetta dopo il login. Se non lo è, aggiustare all'URL protetto conosciuto.
PROTECTED_RESOURCE_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -b "${COOKIES_FILE}" "${APP_URL}/home.jsp")
if [ "$PROTECTED_RESOURCE_STATUS" -eq 302 ] || [ "$PROTECTED_RESOURCE_STATUS" -eq 403 ] || [ "$PROTECTED_RESOURCE_STATUS" -eq 401 ]; then
    log_success "RF7: Accesso alla risorsa protetta (${APP_URL}/home.jsp) dopo il logout correttamente negato (stato ${PROTECTED_RESOURCE_STATUS})."
else
    log_error "RF7: Accesso alla risorsa protetta (${APP_URL}/home.jsp) dopo il logout NON è stato negato come previsto (stato ${PROTECTED_RESOURCE_STATUS})."
    EXIT_CODE=1
fi
echo ""

# --- Pulizia finale degli artefatti dei test ---
log_info "Pulizia finale degli artefatti dei test..."
rm -f "${UPLOAD_FILE}"
rm -f "${COOKIES_FILE}" "${PRE_LOGIN_COOKIES_FILE}"
echo ""

# --- Final Report Summary ---
echo "--- Report Riassuntivo Test Funzionali e di Sicurezza ---"
echo "Test eseguiti contro: ${APP_URL}"

if [ "$EXIT_CODE" -eq 0 ]; then
    log_success "Tutti i controlli funzionali e di sicurezza di base sono PASSATI."
    echo "Riepilogo: L'applicazione sembra soddisfare i requisiti funzionali specificati e implementa le pratiche di sicurezza di base per la gestione delle sessioni (flag HttpOnly, Secure e mitigazione della session fixation)."
else
    log_error "Alcuni controlli funzionali o di sicurezza sono FALLITI. Si prega di rivedere i log dettagliati sopra."
    echo "Riepilogo: L'applicazione ha mostrato fallimenti in alcuni controlli funzionali o di sicurezza. Si raccomanda un'indagine immediata degli errori registrati."
fi

exit $EXIT_CODE