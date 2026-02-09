# SicNelleApplicazioni

## What This Is

Sviluppo di una Web Application Java (Servlet/JSP) per la gestione e condivisione sicura di contenuti testuali tra utenti autenticati. Il progetto mira a implementare i principi di Secure Software Development e Programmazione Difensiva per mitigare le vulnerabilità web più comuni.

## Core Value

Registrazione e login con password protette da hashing robusto (SHA-256 + Salt) e gestione del lifetime delle credenziali in memoria (uso di char[] e Arrays.fill()).

## Requirements

### Validated

<!-- Shipped and confirmed valuable. -->

(None yet — ship to validate)

### Active

<!-- Current scope. Building toward these. -->

- [ ] Session Management: Gestione sicura delle sessioni HTTP tramite cookie protetti con flag HttpOnly, Secure e SameSite, prevenendo attacchi di Session Fixation e XSS.
- [ ] Infrastruttura Sicura: Comunicazione cifrata end-to-end tramite HTTPS/TLS su Apache Tomcat e connessione al database MySQL esclusivamente via SSL/TLS.
- [ ] Validazione Rigorosa: Caricamento file limitato a estensioni .txt con verifica del tipo MIME reale tramite la libreria Apache Tika per prevenire file malevoli.
- [ ] Protezione Injection: Utilizzo sistematico di PreparedStatement (JDBC) per neutralizzare SQL Injection e sanitizzazione dell'output per prevenire Cross-Site Scripting (XSS).

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- [Exclusion 1] — [why]
- [Exclusion 2] — [why]

## Context

[Background information that informs implementation:
- Technical environment or ecosystem
- Relevant prior work or experience
- User research or feedback themes
- Known issues to address]

## Constraints

- **[Type]**: [What] — [Why]
- **[Type]**: [What] — [Why]

Common types: Tech stack, Timeline, Budget, Dependencies, Compatibility, Performance, Security

## Key Decisions

<!-- Decisions that constrain future work. Add throughout project lifecycle. -->

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| [Choice] | [Why] | [✓ Good / ⚠️ Revisit / — Pending] |

## Current Milestone: v1.0 progectInit

**Goal:** Initialize the project's core planning structure and define initial requirements.

**Target features:**
- Initial project setup
- Core requirements definition
- Roadmap generation

---
*Last updated: Monday, February 9, 2026 after project description provided*
