# Employee Management Application

| Layer          | Technology                                                    |
| -------------- | ------------------------------------------------------------- |
| Frontend       | Vue 3, Vite, Vue Router, Pinia, Axios — served by Nginx       |
| Backend        | Java 21, Spring Boot 3.5, Spring Security (JWT), JPA, Flyway  |
| Database       | PostgreSQL 16                                                 |
| Object storage | MinIO (S3-compatible)                                         |
| PDF generation | OpenPDF                                                       |
| Deployment     | Docker & Docker Compose                                       |

---

## 1. Quick start

**Prerequisite:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) (with Docker Compose v2).

```bash
# 1. Create the environment file
cp .env.example .env           

# 2. Build and start everything
docker compose up --build
```

The first build takes a few minutes (Maven and npm dependencies are downloaded inside the containers — **no Java, Maven or Node installation is needed** on the host).

When the logs show `Started EmsApplication`, open:

| Service                | URL                          | Credentials                     |
| ---------------------- | ---------------------------- | ------------------------------- |
| **Application**        | http://localhost:3000        | see below                       |
| Backend API            | http://localhost:8080/api    | JWT                             |
| MinIO console          | http://localhost:9001        | `minioadmin` / `minioadmin123`  |
| PostgreSQL             | `localhost:5433`             | `ems_user` / `ems_password`     |

### Default accounts

The two **Super Administrators** are created automatically on first start (values come from `.env`):

| Email            | Password    |
| ---------------- | ----------- |
| `admin1@ems.com` | `Admin@123` |
| `admin2@ems.com` | `Admin@123` |

### Useful commands

```bash
docker compose up -d --build     # start in the background
docker compose logs -f backend   # follow backend logs
docker compose down              # stop (data is kept)
docker compose down -v           # stop AND delete all data (database + files)
```

---

## 2. Features

### Authentication
- Login with email and password; passwords are hashed with **BCrypt**.
- Stateless authentication with a **JWT** (24 h by default).
- All routes except login are protected, in the backend and in the frontend.
- An expired or invalid session logs the user out automatically.

### Users & permissions (Super Administrators only)
- Paginated table with search (name, email).
- Create, view, edit and delete users; password is optional on edit.
- Grant or revoke the **CREATE / READ / UPDATE / DELETE** permissions.
- Safety rules: a super admin cannot delete, disable or demote **themselves**, and the **last active super admin** can never be removed.

### Employees
- Paginated table with search (name, email, job title, department).
- Create, view, edit and delete employees, each action requiring the matching permission.
- **Photo** (JPEG/PNG/WEBP, max 2 MB) and **CV** (PDF, max 5 MB) upload, view and download.
- **Employment contract** generated on demand from a predefined template.

### User interface
- Navbar (app name, current user, logout) and sidebar with the **Users** and **Employees** menus.
- Loading skeletons, empty states, error states with retry, and success/error notifications.
- Responsive layout (the sidebar becomes a slide-in menu on mobile).

---

## 3. Permission model

| Action                                   | Required authority      |
| ---------------------------------------- | ----------------------- |
| Manage users and their permissions       | `SUPER_ADMIN` role      |
| List / view employees, photo, CV, contract | `READ`                |
| Create an employee                       | `CREATE`                |
| Edit an employee                         | `UPDATE`                |
| Upload / replace photo or CV             | `CREATE` or `UPDATE`    |
| Delete an employee                       | `DELETE`                |

- A super administrator automatically has **all** permissions.
- Permissions are **enforced by the backend** with `@PreAuthorize`. The frontend only hides buttons the user cannot use; calling the API directly is still rejected (`403`).
- The user is reloaded from the database on every request, so **revoking a permission takes effect immediately**, even if the user's token has not expired.

---

## 4. Architecture

```
Browser ──► Nginx (frontend container, :3000)
              ├── /        → Vue single-page application
              └── /api/*   → Spring Boot backend (:8080)
                                ├── PostgreSQL : users, permissions, employees, file keys
                                └── MinIO      : photo and CV files (private bucket)
```

- Nginx serves the frontend **and** proxies `/api` to the backend, so the browser talks to a **single origin**: no CORS configuration is needed.
- The database schema is versioned with **Flyway** (`backend/src/main/resources/db/migration`).

### Key design decisions

- **Files are never stored in the database.** PostgreSQL stores only the MinIO object key; the binary lives in MinIO.
- **The MinIO bucket is private.** Photos and CVs are always served **through the API**, which checks the JWT and the `READ` permission. In the frontend, photos are fetched with Axios (which sends the token) and displayed through a local `blob:` URL, because a plain `<img src>` cannot send an `Authorization` header.
- **Files are validated by content, not by name.** The backend checks the file signature (magic bytes): a renamed `.exe` is rejected even if it is called `cv.pdf`. File size is limited too.
- **No orphan files.** Replacing a photo or CV deletes the previous object; deleting an employee deletes their files.
- **Clean error handling.** A global exception handler returns a consistent JSON format (`status`, `message`, `fieldErrors`). Stack traces and technical details are never exposed to the user.
- **Secrets are configuration, not code.** Credentials, the JWT secret and the super admin accounts are read from environment variables (`.env`).

### Contract template

`ContractService` contains a predefined template with placeholders such as `{{fullName}}`, `{{jobTitle}}`, `{{department}}`, `{{hireDate}}` and `{{salary}}`. They are replaced with the employee's data, and the result is rendered to PDF with OpenPDF (summary table, six articles and signature blocks).

---

## 5. REST API overview

All endpoints are under `/api` and, except login, require the header `Authorization: Bearer <token>`.

| Method | Endpoint                          | Description                               |
| ------ | --------------------------------- | ----------------------------------------- |
| POST   | `/auth/login`                     | Log in, returns a JWT and the user        |
| GET    | `/auth/me`                        | Current user and permissions              |
| GET    | `/users?search=&page=&size=`      | Paginated user list                       |
| POST   | `/users`                          | Create a user                             |
| GET / PUT / DELETE | `/users/{id}`         | Read / update / delete a user             |
| PUT    | `/users/{id}/permissions`         | Replace the user's permissions            |
| GET    | `/employees?search=&page=&size=`  | Paginated employee list                   |
| POST   | `/employees`                      | Create an employee                        |
| GET / PUT / DELETE | `/employees/{id}`     | Read / update / delete an employee        |
| POST / GET | `/employees/{id}/photo`       | Upload / read the photo (multipart `file`) |
| POST / GET | `/employees/{id}/cv`          | Upload / read the CV (`?download=true` to download) |
| GET    | `/employees/{id}/contract`        | Generate and download the contract (PDF)  |

---

## 6. Project structure

```
.
├── docker-compose.yml
├── .env.example
├── backend/                        Spring Boot application
│   ├── Dockerfile                  Multi-stage build (Maven → JRE)
│   └── src/main/java/com/ems/
│       ├── common/                 API error format, global exception handler
│       ├── config/                 Security, MinIO, properties, super admin seeding
│       ├── controller/             REST controllers
│       ├── dto/                    Request / response objects
│       ├── entity/                 JPA entities (User, Employee)
│       ├── enums/                  Role, Permission
│       ├── repository/             Spring Data repositories
│       ├── security/               JWT service and filter, authenticated principal
│       └── service/                Business logic, storage, contract generation
└── frontend/                       Vue 3 application
    ├── Dockerfile                  Multi-stage build (Node → Nginx)
    ├── nginx.conf                  SPA routing + /api reverse proxy
    └── src/
        ├── api/                    Axios client and API modules
        ├── components/             Reusable UI (modal, pagination, table states…)
        ├── composables/            Paged list logic (search, pagination, loading)
        ├── layouts/                Main layout (navbar + sidebar)
        ├── router/                 Routes and navigation guards
        ├── stores/                 Pinia stores (auth, notifications)
        └── views/                  Login, Users, Employees pages
```

---

## 7. Local development (without Docker for the app)

```bash
# Infrastructure only
docker compose up -d postgres minio

# Backend (Java 21) — or run EmsApplication from your IDE
cd backend && mvn spring-boot:run

# Frontend (Node 20+) — Vite proxies /api to localhost:8080
cd frontend && npm install && npm run dev      # http://localhost:5173
```

---

## 8. Troubleshooting

| Problem | Solution |
| ------- | -------- |
| `port is already allocated` | Another program uses port 3000, 8080, 5433, 9000 or 9001. Stop it, or change the left side of the port mapping in `docker-compose.yml` (e.g. `"3001:80"`). |
| The app shows errors right after start | The backend takes ~20–30 s to start. Wait for `Started EmsApplication` in `docker compose logs backend`. |
| Start again from a clean state | `docker compose down -v` then `docker compose up --build`. |

**Note on MinIO:** MinIO Inc. stopped publishing community Docker images, and the old `minio/minio` image can no longer be pulled. This project uses `pgsty/minio`, a maintained community fork that is a drop-in replacement (same S3 API, environment variables and data format), pinned to a fixed version for reproducible builds.
