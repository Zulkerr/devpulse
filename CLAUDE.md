# DevPulse – Projekt-Dokumentation

DevPulse ist ein Microservice-System zur Überwachung von Web-Services und Incident-Management.

## Architektur-Übersicht

Das Projekt besteht aus drei entkoppelten Services, die über REST-APIs kommunizieren:

- **`/backend-java`**: Core-System (Spring Boot, Maven, H2). Verwaltet Services und Incidents.
- **`/worker-python`**: Monitoring-Engine (FastAPI). Führt Health-Checks durch.
- **`/frontend-react`**: Dashboard (Vite, TS). UI für Administration und Monitoring.

## Build / Test
- Im `/backend-java`-Ordner existiert ein Maven-Wrapper (`mvnw` / `mvnw.cmd`), der Maven automatisch herunterlädt.
- Ausführung: `cd backend-java && ./mvnw test` (Windows: `mvnw.cmd test`)
- Der Build läuft mit Java 17, Spring Boot 3.2.5, H2 In-Memory DB.

## Worker (Python)
- Abhängigkeiten in `worker-python/requirements.txt` (fastapi, uvicorn, httpx, pytest).
- Start: `cd worker-python && uvicorn main:app --port 8000`.
- Tests: `cd worker-python && python -m pytest -q` (alle HTTP-Aufrufe sind gemockt, laufen ohne Backend).
- API: `GET /health`, `POST /check-all` (ruft Services vom Backend ab, pingt, meldet Status per PATCH, erstellt Incident bei DOWN).

## Frontend (React)
- Stack: Vite, React, TypeScript.
- Port: 5173 (`npm run dev`).
- Build: `npm run build` (wird via `tsc -b && vite build` geprüft).
- Features: Live-Übersicht Services, Hinzufügen von Services, Health-Check-Trigger (ruft Worker `/check-all`), Incident-Management (Ansicht & Schließen).

## Service-Details

### 1. Backend (Java)
- **Tech-Stack**: Java, Spring Boot, Maven, H2 In-Memory DB.
- **Port**: 8080
- **Verantwortung**: Persistenz von Service-Konfigurationen und Incident-Logs.
- **API**:
  - `GET /api/services`
  - `POST /api/services`
  - `GET /api/incidents`
- **Regeln**: Schichtenarchitektur (Controller -> Service -> Repository), Input-Validierung, CORS-Allow-Origin für Port 5173.

### 2. Worker (Python)
- **Tech-Stack**: Python, FastAPI, Pytest.
- **Port**: 8000
- **Verantwortung**: Zyklisches Pingen der konfigurierten Services. Meldet Statusänderungen zurück an das Java-Backend.
- **Qualität**: Automatisierte E2E-Testsuite mittels `pytest`.

### 3. Frontend (React)
- **Tech-Stack**: Vite, TypeScript/JavaScript.
- **Port**: 5173
- **Verantwortung**: Live-Dashboard, CRUD für Services, Incident-Verwaltung.

## Qualitätsstandards & Regeln

- **Secrets**: Keine hartcodierten Passwörter oder API-Keys. Nutzung von Umgebungsvariablen.
- **Fehlerbehandlung**: Konsistente Nutzung von HTTP-Statuscodes (2xx, 4xx, 5xx).
- **Kommunikation**: CORS ist explizit für Anfragen von Port 5173 zu konfigurieren.
- **Git-Workflows**: Sprechende Commit-Messages (Conventional Commits empfohlen).
- **Attribution**:
  - Commits enden mit: `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`
  - PR-Beschreibungen enden mit: `🤖 Generated with [Claude Code](https://claude.com/claude-code)`
