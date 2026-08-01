# BatchBridge – College Notes Sharing Platform

A full-stack web application for uploading, searching, and downloading academic notes. Built with a layered Spring Boot architecture (controller → service → repository) and backed by MySQL.

## Features

- **Full CRUD** on notes — create, read, update, delete
- **Secure file uploads** — 20MB size cap, 7-type whitelist (PDF, DOC, DOCX, PPT, PPTX, TXT, ZIP), UUID-based renaming, and path-traversal protection
- **Flexible search** — filter by subject, semester, and department (combinable), plus full-text keyword search across title, subject, and description
- **File download** — served with correct content type and disposition headers
- **Centralized error handling** — structured JSON error responses mapped to correct HTTP status codes (404, 400, 500)
- **Responsive UI** — vanilla HTML/CSS/JavaScript frontend

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot, Spring Data JPA |
| Database | MySQL |
| Frontend | HTML, CSS, JavaScript |
| Build Tool | Maven |

## Architecture

```
Controller  →  Service  →  Repository  →  MySQL
   │              │
   │              └── FileStorageService (upload validation, storage, deletion)
   │
   └── GlobalExceptionHandler (centralized error responses)
```

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/notes` | Get all notes |
| `GET` | `/api/notes/{id}` | Get a note by ID |
| `POST` | `/api/notes` | Upload a new note (multipart form) |
| `PUT` | `/api/notes/{id}` | Update an existing note |
| `DELETE` | `/api/notes/{id}` | Delete a note |
| `GET` | `/api/notes/search?subject=&semester=&department=` | Search by combinable filters |
| `GET` | `/api/notes/search/keyword?keyword=` | Full-text search across title, subject, description |
| `GET` | `/api/notes/download/{id}` | Download the attached file |

## File Upload Rules

- Maximum file size: **20MB**
- Allowed types: `pdf`, `doc`, `docx`, `ppt`, `pptx`, `txt`, `zip`
- Files are renamed with a generated UUID before storage to prevent collisions and path-traversal attacks

## Getting Started

### Prerequisites
- Java 17+
- Maven
- MySQL

### Setup

1. Create the database (or let it auto-create on first run):
   ```sql
   CREATE DATABASE notes_db;
   ```

2. Update `src/main/resources/application.properties` with your MySQL credentials.

3. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. Open your browser to:
   ```
   http://localhost:8080
   ```

## Error Handling

All API errors return structured JSON with the appropriate HTTP status code:

| Exception | Status Code |
|---|---|
| Resource not found | `404 Not Found` |
| File storage error | `400 Bad Request` |
| Validation error | `400 Bad Request` |
| Unhandled exception | `500 Internal Server Error` |

## License

This project was built as an independent learning project to practice full-stack development with Spring Boot.
