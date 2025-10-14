# ✅ Setup Completo - unir-ig-api

## 🎉 ¡Todo Implementado!

Tu proyecto ahora tiene un sistema completo de OAuth para Instagram Business + API Gateway lista para el frontend.

---

## 📝 Archivos Creados/Modificados

### API Gateway (BFF)
- ✅ `InstagramOAuthConfig.java` - Configuración OAuth
- ✅ `InstagramOAuthService.java` - Lógica de OAuth flow
- ✅ `InstagramOAuthController.java` - Endpoints para frontend
- ✅ `InstagramConnectionDTO.java` - DTO de estado de conexión
- ✅ `OAuthCallbackResponse.java` - DTO de respuesta callback
- ✅ `AccountRepository.java` - Repository para cuentas
- ✅ `HealthController.java` - Health check endpoint
- ✅ `application.yml` - Config actualizada con OAuth

### Instagram Service
- ✅ `HealthController.java` - Health check endpoint
- ✅ `InstagramWebhookService.java` - Mejorado con raw payload
- ✅ `WebhookControllerTest.java` - Tests de webhook verification

### Common (Entidades)
- ✅ `Thread.java` - Actualizado con subject, metadata, created_at
- ✅ `Message.java` - Actualizado con recipients, body_html, attachments, raw

### Base de Datos
- ✅ `V1__create_initial_schema.sql` - Schema completo (ambos servicios)

### Documentación
- ✅ `INSTAGRAM_SETUP.md` - Guía configuración Meta
- ✅ `INSTAGRAM_OAUTH_GUIDE.md` - Guía OAuth flow
- ✅ `FRONTEND_API_GUIDE.md` - **Guía para tu frontend**
- ✅ `SETUP_COMPLETE.md` - Este archivo

---

## 🚀 Pasos para Ejecutar

### 1. Configurar Variables de Entorno

Edita tu `.env`:

```bash
cd /Users/iroldan/Desktop/UADE/sem/unir-ig-api
cat >> .env << 'EOF'

# Instagram OAuth (Business)
INSTAGRAM_CLIENT_ID=813439077768151
INSTAGRAM_CLIENT_SECRET=tu_app_secret_de_meta
INSTAGRAM_REDIRECT_URI=http://localhost:8080/auth/instagram/callback

EOF
```

### 2. Configurar Redirect URI en Meta

1. Ve a https://developers.facebook.com/apps/813439077768151
2. Settings → Basic
3. En "App Domains" agrega: `localhost`
4. Guarda cambios

5. Ve a Instagram → Basic Display
6. En "Valid OAuth Redirect URIs" agrega:
   ```
   http://localhost:8080/auth/instagram/callback
   https://tudominio.com/auth/instagram/callback
   ```
7. Guarda cambios

### 3. Compilar y Ejecutar

```bash
# Compilar (verifica que no haya errores)
./gradlew clean build

# Terminal 1: API Gateway
./gradlew :api-gateway:bootRun

# Terminal 2: IG Service
./gradlew :ig-service:bootRun
```

### 4. Verificar que Todo Funciona

```bash
# Health checks
curl http://localhost:8080/health
# Esperado: {"status":"UP","service":"api-gateway","version":"1.0.0"}

curl http://localhost:8081/health
# Esperado: {"status":"UP","service":"ig-service","version":"1.0.0"}

# Test de OAuth URL (reemplaza USER_UUID)
curl "http://localhost:8080/auth/instagram/authorization-url?userId=00000000-0000-0000-0000-000000000001"
# Esperado: {"authorizationUrl":"https://api.instagram.com/oauth/authorize?...","userId":"..."}
```

---

## 🎯 Flujo Completo de Usuario

### Para el Comerciante (Usuario Final)

1. **Se registra en tu plataforma** → Creas un `user_id`

2. **Conecta su Instagram Business:**
   - Frontend: Botón "Conectar Instagram"
   - Llama a: `GET /auth/instagram/connect?userId={uuid}`
   - Usuario autoriza en Instagram
   - Redirige de vuelta: `/callback?code=...`
   - Backend guarda la cuenta en `accounts` table
   - Redirige al frontend: `?connected=instagram&accountId={uuid}`

3. **Recibe mensajes de sus clientes:**
   - Clientes escriben DMs en Instagram
   - Meta envía webhook a `ig-service`
   - Se guarda en `threads` y `messages`
   - SSE notifica al frontend en tiempo real

4. **Ve sus mensajes en tu inbox:**
   - Frontend lista threads: `GET /v1/threads`
   - Selecciona conversación: `GET /v1/threads/{id}/messages`
   - Ve mensajes en tiempo real via SSE

5. **Responde desde tu plataforma:**
   - Escribe respuesta en tu UI
   - Frontend: `POST /v1/messages`
   - `api-gateway` enruta a `ig-service`
   - `ig-service` llama a Graph API de Meta
   - Mensaje se envía por Instagram

---

## 📡 Endpoints Disponibles

### Auth/OAuth
- `GET /auth/instagram/connect?userId=<uuid>` - Iniciar OAuth
- `GET /auth/instagram/callback` - Callback de Instagram
- `GET /auth/instagram/status?userId=<uuid>` - Ver estado conexión
- `POST /auth/instagram/disconnect?userId=<uuid>` - Desconectar
- `GET /auth/instagram/authorization-url?userId=<uuid>` - Obtener URL OAuth

### Threads (Conversaciones)
- `GET /v1/threads` - Listar todos
- `GET /v1/threads?channel=instagram` - Filtrar por canal
- `GET /v1/threads?accountId=<uuid>` - Filtrar por cuenta
- `GET /v1/threads/{id}` - Obtener uno específico

### Mensajes
- `GET /v1/threads/{id}/messages` - Listar mensajes de thread
- `POST /v1/messages` - Enviar mensaje

### Tiempo Real
- `GET /v1/stream` - SSE stream de eventos

### Health
- `GET /health` - Health check

---

## 🎨 Ejemplo de UI Flow

```
┌─────────────────────────────────────┐
│  Página de Configuración            │
│                                     │
│  ┌────────────────────────────┐   │
│  │  Instagram Business         │   │
│  │  ❌ No conectado           │   │
│  │  [Conectar Instagram]      │   │
│  └────────────────────────────┘   │
│                                     │
│  ┌────────────────────────────┐   │
│  │  WhatsApp Business          │   │
│  │  ❌ No conectado           │   │
│  │  [Conectar WhatsApp]       │   │
│  └────────────────────────────┘   │
└─────────────────────────────────────┘

Usuario hace clic en "Conectar Instagram"
↓
Redirige a Instagram OAuth
↓
Usuario autoriza
↓
Redirige de vuelta

┌─────────────────────────────────────┐
│  Página de Configuración            │
│                                     │
│  ┌────────────────────────────┐   │
│  │  Instagram Business         │   │
│  │  ✅ Conectado              │   │
│  │  @mitienda_oficial         │   │
│  │  [Desconectar]             │   │
│  └────────────────────────────┘   │
└─────────────────────────────────────┘

Usuario va al Inbox
↓

┌──────────────┬─────────────────────────┐
│ Threads      │  Mensajes               │
│              │                         │
│ 📸 Cliente 1 │  Cliente: Hola!         │
│ 💬 Cliente 2 │  Tú: ¿En qué puedo...  │
│ 📧 Cliente 3 │  Cliente: Quiero...     │
│              │                         │
│              │  [Escribe mensaje___]   │
└──────────────┴─────────────────────────┘
```

---

## 🔐 Seguridad - TODO

Actualmente los tokens se guardan sin cifrar en la DB (solo para desarrollo).

**Para producción debes:**

1. Implementar `CryptoService` usando `CryptoUtil`
2. Cifrar tokens antes de guardar:
   ```java
   String encrypted = CryptoUtil.encrypt(accessToken, cryptoKey);
   account.setCredentialsEncrypted(encrypted);
   ```

3. Descifrar cuando los uses:
   ```java
   String decrypted = CryptoUtil.decrypt(account.getCredentialsEncrypted(), cryptoKey);
   ```

4. Guardar `CRYPTO_SECRET` en variables de entorno

---

## 🐛 Troubleshooting

### Error: "Invalid platform app"
→ Asegúrate de que Instagram esté agregado como producto en tu app de Meta

### Error: "Redirect URI mismatch"
→ La URI debe coincidir EXACTAMENTE con la configurada en Meta (incluyendo puerto)

### No se reciben webhooks
→ Asegúrate de:
- ngrok está corriendo (para local)
- Webhook verificado en Meta (✅ verde)
- Suscrito a eventos "messages"

### Tokens expiran
→ Para producción usa System User Token de Meta (no expira)

### Error compilando
→ Verifica que estés usando Java 21:
```bash
java -version
# Debe mostrar: openjdk version "21..." o "23..." (compatible)
```

---

## 📚 Documentación de Referencia

- **Meta Graph API**: https://developers.facebook.com/docs/graph-api/
- **Instagram Messaging**: https://developers.facebook.com/docs/messenger-platform/instagram
- **OAuth 2.0**: https://oauth.net/2/
- **Server-Sent Events**: https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events

---

## 🎯 Próximos Pasos

1. **Frontend**: Implementar la UI usando `FRONTEND_API_GUIDE.md`
2. **Autenticación**: Agregar JWT/Session para tus usuarios
3. **WhatsApp**: Implementar OAuth similar para WhatsApp Business
4. **Gmail**: Implementar OAuth con Google
5. **Tests**: Agregar tests de integración
6. **Deploy**: Subir a Railway/Heroku/AWS

---

## 🆘 ¿Necesitas Ayuda?

Consulta las guías específicas:
- `INSTAGRAM_SETUP.md` - Configuración de Meta Developer
- `INSTAGRAM_OAUTH_GUIDE.md` - Flujo OAuth detallado
- `FRONTEND_API_GUIDE.md` - Cómo consumir la API desde el frontend
- `README.md` - Documentación general del proyecto

---

## ✨ ¡Listo!

Tu backend está completamente funcional para:
✅ OAuth de Instagram Business
✅ Recibir webhooks de Instagram
✅ Almacenar threads y mensajes
✅ Enviar mensajes
✅ Stream en tiempo real (SSE)
✅ API lista para frontend

**Ahora puedes empezar a construir tu frontend** siguiendo `FRONTEND_API_GUIDE.md`

¡Éxito con tu proyecto! 🚀

