# stock-your-lot-api

API for stock your lot.

## Run locally

1. **Start PostgreSQL** (from project root):
   ```bash
   docker compose up -d
   ```

2. **Run the app** (Java 17 required):
   ```bash
   ./gradlew bootRun
   ```

   API: **http://localhost:8080**

   Default DB (matches docker-compose): `localhost:5432`, database `stock-your-lot`, user/password `postgres`.

### Endpoints

- `POST /api/register` – register (username, email, password)
- `POST /api/login` – login (username, password)
- `GET /actuator/health` – health check
