# 📬 unir-ig-api - Unified Inbox Demo

Monorepo completo para un sistema de inbox unificado con soporte para Instagram, WhatsApp y Gmail (próximamente).

## 🏗 Arquitectura

```
┌─────────────┐
│  Frontend   │
│ (React/Vue) │
└──────┬──────┘
       │ HTTP/SSE
       ▼
┌─────────────────┐
│  API Gateway    │  ←─── BFF (Backend for Frontend)
│  :8080          │       - REST API
└────────┬────────┘       - SSE streaming
         │                - Rate limiting
         │                - CORS
         ├──────────────────┬───────────────────┐
         ▼                  ▼                   ▼
┌─────────────────┐ ┌─────────────┐  ┌──────────────┐
│  IG Service     │ │ WA Service  │  │ Gmail Service│
│  :8081          │ │ (futuro)    │  │ (futuro)     │
└────────┬────────┘ └─────────────┘  └──────────────┘
         │
         │ Webhooks + Graph API
         ▼
┌─────────────────────────────────────┐
│   Meta Graph API (Instagram)        │
└─────────────────────────────────────┘

      │
      ▼
┌─────────────┐
│ PostgreSQL  │
│   :5432     │
└─────────────┘
```

## 📦 Estructura del Proyecto

```
unir-ig-api/
├── api-gateway/              # BFF - Backend for Frontend
│   ├── src/main/java/
│   │   └── com/unir/apigateway/
│   │       ├── controller/   # REST endpoints
│   │       ├── service/      # Business logic + routing
│   │       ├── repository/   # JPA repositories
│   │       ├── dto/          # Data Transfer Objects
│   │       └── config/       # CORS, WebClient
│   └── build.gradle
├── ig-service/               # Instagram Integration Service
│   ├── src/main/java/
│   │   └── com/unir/igservice/
│   │       ├── controller/   # Webhook + Send endpoints
│   │       ├── service/      # Instagram API logic
│   │       ├── repository/   # JPA repositories
│   │       ├── dto/          # Instagram DTOs
│   │       └── config/       # Instagram config
│   └── build.gradle
├── libs/common/              # Shared library
│   └── src/main/java/
│       └── com/unir/common/
│           ├── entity/       # JPA entities (Account, Thread, Message)
│           ├── model/        # Enums (Channel, Direction)
│           ├── dto/          # Shared DTOs
│           └── util/         # CryptoUtil (AES-256-GCM)
├── docker-compose.yml        # Postgres + services
├── .env.example              # Environment variables template
└── README.md                 # Este archivo
```

## 🛠 Tech Stack

- **Java 21** - Lenguaje principal
- **Spring Boot 3.2** - Framework backend
- **Gradle** - Build tool
- **PostgreSQL 15** - Base de datos relacional
- **Flyway** - Migraciones de DB
- **WebFlux** - Cliente HTTP asíncrono
- **JPA/Hibernate** - ORM
- **SSE** (Server-Sent Events) - Streaming en tiempo real
- **Docker Compose** - Orquestación local
- **AES-256-GCM** - Encriptación de credenciales

## 🚀 Inicio Rápido

### 1️⃣ Pre-requisitos

- **Java 21** (verificar con `java -version`)
- **Docker & Docker Compose** (para PostgreSQL)
- **ngrok** (para exponer webhooks localmente)
- **GitHub CLI** (opcional, para crear repo)
- **Cuenta Meta Developer** con app configurada

### 2️⃣ Clonar y Configurar

```bash
git clone https://github.com/iaroldan99/unir-ig-api.git
cd unir-ig-api

# Copiar variables de entorno
cp .env.example .env

# Editar .env con tus credenciales de Instagram
nano .env
```

### 3️⃣ Levantar Base de Datos

```bash
docker compose up -d db
```

Esto iniciará PostgreSQL en `localhost:5432` con:
- DB: `inbox`
- User: `postgres`
- Password: `postgres`

### 4️⃣ Ejecutar Servicios (Desarrollo Local)

#### Terminal 1: IG Service
```bash
./gradlew :ig-service:bootRun
```
Se ejecutará en `http://localhost:8081`

#### Terminal 2: API Gateway
```bash
./gradlew :api-gateway:bootRun
```
Se ejecutará en `http://localhost:8080`

Las migraciones Flyway se ejecutarán automáticamente al iniciar.

### 5️⃣ Verificar que todo funciona

```bash
# Verificar API Gateway
curl http://localhost:8080/v1/threads

# Verificar IG Service
curl http://localhost:8081/actuator/health
```

## 🔗 Exponer Webhooks con ngrok

Instagram necesita un webhook público para enviar eventos. Usa ngrok para exponer tu servicio local:

```bash
# Instalar ngrok (si no lo tienes)
brew install ngrok  # macOS
# O descarga desde https://ngrok.com/download

# Exponer el puerto 8081 (ig-service)
ngrok http 8081
```

Obtendrás una URL pública, por ejemplo:
```
Forwarding  https://abc123.ngrok.app -> http://localhost:8081
```

## 🎯 Configurar Meta Webhook (Instagram)

1. Ve a [Meta for Developers](https://developers.facebook.com/)
2. Selecciona tu app o crea una nueva
3. Agrega el producto **Instagram**
4. Ve a **Webhooks** → **Instagram**
5. Configura:
   - **Callback URL**: `https://abc123.ngrok.app/webhooks/instagram`
   - **Verify Token**: `demo_token` (o el valor de `IG_VERIFY_TOKEN` en tu `.env`)
6. Suscríbete a los eventos: `messages`, `messaging_postbacks`

### Verificar Webhook

Meta hará un GET request para verificar el webhook:

```bash
curl -i "https://abc123.ngrok.app/webhooks/instagram?hub.mode=subscribe&hub.challenge=123456&hub.verify_token=demo_token"
```

Debería responder `200 OK` con el `hub.challenge`.

## 📨 Endpoints Principales

### API Gateway (Port 8080)

#### 1. Listar Threads
```bash
GET /v1/threads?channel=instagram&accountId=<uuid>&q=search&page=0&size=20

curl "http://localhost:8080/v1/threads?channel=instagram"
```

#### 2. Obtener Thread por ID
```bash
GET /v1/threads/{id}

curl "http://localhost:8080/v1/threads/<thread-uuid>"
```

#### 3. Listar Mensajes de un Thread
```bash
GET /v1/threads/{id}/messages?page=0&size=50

curl "http://localhost:8080/v1/threads/<thread-uuid>/messages"
```

#### 4. Enviar Mensaje
```bash
POST /v1/messages
Content-Type: application/json

{
  "channel": "INSTAGRAM",
  "accountId": "<uuid>",
  "to": [{"id": "<instagram-scoped-id>"}],
  "text": "Hola desde la API 👋"
}
```

Ejemplo cURL:
```bash
curl -X POST "http://localhost:8080/v1/messages" \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "INSTAGRAM",
    "accountId": "550e8400-e29b-41d4-a716-446655440000",
    "to": [{"id": "1234567890"}],
    "text": "Hola desde Instagram API!"
  }'
```

#### 5. Stream SSE (Server-Sent Events)
```bash
GET /v1/stream

curl -N http://localhost:8080/v1/stream
```

Recibirás eventos en tiempo real:
```
event: message.created
data: {"messageId":"...","status":"sent"}

event: thread.updated
data: {"threadId":"...","lastMessageAt":"..."}
```

### IG Service (Port 8081)

#### 1. Webhook Verification (GET)
```bash
GET /webhooks/instagram?hub.mode=subscribe&hub.challenge=123&hub.verify_token=demo_token
```

#### 2. Webhook Events (POST)
```bash
POST /webhooks/instagram
Content-Type: application/json

# Meta enviará eventos automáticamente
```

#### 3. Enviar Mensaje directo
```bash
POST /v1/ig/send
Content-Type: application/json

{
  "accountId": "<uuid>",
  "toId": "<instagram-scoped-id>",
  "text": "Hola desde IG Service"
}
```

Ejemplo cURL:
```bash
curl -X POST "http://localhost:8081/v1/ig/send" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "550e8400-e29b-41d4-a716-446655440000",
    "toId": "1234567890",
    "text": "Hola desde IG 👋"
  }'
```

## 🧪 Testing con Instagram

### 1. Configurar Meta App

1. Crea una app en [Meta for Developers](https://developers.facebook.com/)
2. Agrega el producto **Instagram**
3. Obtén:
   - `IG_PAGE_ACCESS_TOKEN` - Token de acceso de página
   - `IG_USER_ID` - ID de usuario de Instagram Business

### 2. Agregar Testers

1. En tu Meta App, ve a **Roles** → **Instagram Testers**
2. Agrega una cuenta de Instagram de prueba
3. Acepta la invitación desde Instagram

### 3. Enviar Mensaje de Prueba

Desde Instagram, envía un DM a tu cuenta de negocio. El webhook recibirá el evento y lo almacenará en la DB.

Verifica en logs:
```bash
# Logs de ig-service
tail -f ig-service/logs/application.log
```

## 🐳 Ejecutar con Docker Compose (Todo junto)

```bash
# Build y ejecutar todos los servicios
docker compose up --build

# O en background
docker compose up -d --build

# Ver logs
docker compose logs -f

# Detener todo
docker compose down
```

Esto iniciará:
- **postgres**: `localhost:5432`
- **ig-service**: `localhost:8081`
- **api-gateway**: `localhost:8080`

## 🗄 Schema de Base de Datos

### `accounts` table
```sql
- id (UUID, PK)
- user_id (UUID)
- channel (ENUM: 'INSTAGRAM', 'WHATSAPP', 'GMAIL')
- display_name (VARCHAR)
- external_ids (JSONB)
- credentials_encrypted (TEXT)
- status (VARCHAR: 'active', 'inactive', 'suspended')
- created_at (TIMESTAMPTZ)
```

### `threads` table
```sql
- id (UUID, PK)
- account_id (UUID, FK → accounts)
- channel (ENUM)
- external_thread_id (VARCHAR, UNIQUE per account)
- participants (JSONB)
- last_message_at (TIMESTAMPTZ)
```

### `messages` table
```sql
- id (UUID, PK)
- thread_id (UUID, FK → threads)
- channel (ENUM)
- direction (ENUM: 'INBOUND', 'OUTBOUND')
- external_message_id (VARCHAR, UNIQUE per thread)
- sender (JSONB)
- body_text (TEXT)
- status (VARCHAR)
- created_at (TIMESTAMPTZ)
```

## 🔐 Seguridad

### Encriptación de Credenciales

Se usa **AES-256-GCM** para encriptar tokens de acceso:

```java
// Encriptar
String encrypted = CryptoUtil.encrypt(plaintext, base64Key);

// Desencriptar
String decrypted = CryptoUtil.decrypt(encrypted, base64Key);

// Generar clave
String key = CryptoUtil.generateKey();
```

### Variables de Entorno Sensibles

**NUNCA** commitees `.env` con credenciales reales. Usa `.env.example` como template.

## 🚢 Desplegar en Railway

### 1. Crear cuenta en [Railway.app](https://railway.app/)

### 2. Crear Postgres Database

```bash
# Desde Railway Dashboard
New Project → Add Postgres → Deploy
```

Obtendrás una `DATABASE_URL` como:
```
postgresql://user:pass@host.railway.app:5432/railway
```

### 3. Desplegar IG Service

```bash
# Conectar repo de GitHub
New Service → GitHub Repo → Select `unir-ig-api`

# Configurar variables de entorno
SPRING_DATASOURCE_URL=jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=...
IG_VERIFY_TOKEN=demo_token
IG_PAGE_ACCESS_TOKEN=EAAB...
IG_USER_ID=1784...
IG_GRAPH_VERSION=v21.0

# Configurar build
Root Directory: /
Build Command: ./gradlew :ig-service:bootJar
Start Command: java -jar ig-service/build/libs/ig-service.jar
```

### 4. Desplegar API Gateway

Repite el proceso para `api-gateway`, agregando:
```
IG_SERVICE_URL=https://ig-service.railway.app
```

### 5. Actualizar Meta Webhook

Cambia la Callback URL a:
```
https://ig-service.railway.app/webhooks/instagram
```

## 🧰 Comandos Útiles

```bash
# Limpiar build
./gradlew clean

# Compilar todo
./gradlew build

# Ejecutar tests
./gradlew test

# Ver dependencias
./gradlew dependencies

# Verificar formato de código
./gradlew checkstyleMain

# Generar JAR
./gradlew :ig-service:bootJar
./gradlew :api-gateway:bootJar

# Conectar a Postgres (desde Docker)
docker exec -it unir-postgres psql -U postgres -d inbox

# Ver logs de servicios
docker compose logs -f ig-service
docker compose logs -f api-gateway

# Reiniciar servicios
docker compose restart ig-service
```

## 🤝 Contribuir

1. Fork el repositorio
2. Crea una rama: `git checkout -b feature/nueva-funcionalidad`
3. Commit: `git commit -m "Agregar nueva funcionalidad"`
4. Push: `git push origin feature/nueva-funcionalidad`
5. Abre un Pull Request

## 📝 Roadmap

- [x] Instagram Integration
- [ ] WhatsApp Integration (wa-service)
- [ ] Gmail Integration (gm-service)
- [ ] Rate limiting mejorado (Redis)
- [ ] Autenticación JWT
- [ ] Métricas con Prometheus
- [ ] Frontend React con Inbox UI
- [ ] Multi-tenancy (soporte para múltiples usuarios)
- [ ] Attachments (imágenes, videos)
- [ ] Message templates
- [ ] Typing indicators
- [ ] Read receipts

## 📄 Licencia

MIT License - Ver [LICENSE](LICENSE) para más detalles

## 🙏 Agradecimientos

- [Meta Graph API Docs](https://developers.facebook.com/docs/graph-api/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [PostgreSQL](https://www.postgresql.org/)

---

**Desarrollado con ❤️ por el equipo de UNIR**

Para dudas o soporte: [GitHub Issues](https://github.com/iaroldan99/unir-ig-api/issues)

