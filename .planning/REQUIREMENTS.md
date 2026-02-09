# Requirements: SicNelleApplicazioni

**Defined:** Monday, February 9, 2026
**Core Value:** Registrazione e login con password protette da hashing robusto (PBKDF2/BCrypt/SCrypt con Salt) e gestione del lifetime delle credenziali in memoria (uso di char[] e Arrays.fill()).

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Autenticazione e Gestione Identità

- [ ] **AUTH-01**: Implementare registrazione e login utenti con password hashing robusto (es. PBKDF2, BCrypt, SCrypt con Salt).
- [ ] **AUTH-02**: Gestire il lifetime delle credenziali in memoria utilizzando `char[]` e `Arrays.fill()`.

### Session Management

- [ ] **AUTH-03**: Gestire sessioni HTTP sicure tramite cookie con flag HttpOnly, Secure e SameSite.
- [ ] **AUTH-04**: Prevenire attacchi di Session Fixation e XSS tramite gestione sicura delle sessioni.

### Infrastruttura e Comunicazione Sicura

- [ ] **INFRA-01**: Configurare comunicazione cifrata end-to-end tramite HTTPS/TLS su Apache Tomcat.
- [ ] **INFRA-02**: Stabilire connessione al database MySQL esclusivamente via SSL/TLS.

### Validazione e Caricamento File

- [ ] **VALID-01**: Implementare caricamento file limitato a estensioni .txt.
- [ ] **VALID-02**: Verificare il tipo MIME reale dei file caricati tramite Apache Tika.

### Protezione da Injection e XSS

- [ ] **INJ-01**: Utilizzare PreparedStatement (JDBC) per neutralizzare SQL Injection.
- [ ] **INJ-02**: Implementare sanitizzazione dell'output per prevenire Cross-Site Scripting (XSS).

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### [Category]

- **[CAT]-01**: [Requirement description]
- **[CAT]-02**: [Requirement description]

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| [Feature] | [Why excluded] |
| [Feature] | [Why excluded] |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| AUTH-01 | Phase 2 | Pending |
| AUTH-02 | Phase 2 | Pending |
| AUTH-03 | Phase 2 | Pending |
| AUTH-04 | Phase 2 | Pending |
| INFRA-01 | Phase 1 | Pending |
| INFRA-02 | Phase 1 | Pending |
| VALID-01 | Phase 3 | Pending |
| VALID-02 | Phase 3 | Pending |
| INJ-01 | Phase 3 | Pending |
| INJ-02 | Phase 3 | Pending |

**Coverage:**
- v1 requirements: 10 total
- Mapped to phases: 10 ✓
- Unmapped: 0 ✓

---
*Requirements defined: Monday, February 9, 2026*
*Last updated: 2024-07-30 (after roadmap generation)*
