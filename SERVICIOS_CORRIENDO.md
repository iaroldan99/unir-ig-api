# ✅ Servicios Levantados y Funcionando

## 📊 Estado Actual

### Servicios Corriendo

| Servicio | Puerto | Estado | Health Check |
|----------|--------|--------|--------------|
| **API Gateway** | 8080 | ✅ UP | http://localhost:8080/health |
| **IG Service** | 8081 | ✅ UP | http://localhost:8081/health |
| **PostgreSQL** | 5432 | ✅ UP | `inbox` database |

### Base de Datos

```
✅ Tablas creadas correctamente:
- accounts (cuentas de Instagram Business)
- threads (conversaciones)
- messages (mensajes)
- flyway_schema_history (control de migraciones)
```

---

## 🔗 Cómo Conectar una Cuenta de Instagram

### Paso 1: Abrir en el Navegador

```
http://localhost:8080/auth/instagram/connect?userId=550e8400-e29b-41d4-a716-446655440000
```

**Nota**: Puedes cambiar el `userId` por cualquier UUID que quieras usar para tu usuario.

### Paso 2: Autorizar en Instagram

1. Te redirigirá a Facebook/Instagram
2. Inicia sesión con tu cuenta Business
3. Acepta los permisos solicitados
4. Selecciona tu Página de Facebook (vinculada a Instagram)
5. Autoriza

### Paso 3: Listo!

El sistema guardará tu access token y podrás:
- ✅ Recibir mensajes de clientes (via webhooks)
- ✅ Enviar mensajes a clientes
- ✅ Ver historial de conversaciones

---

## 🧪 Endpoints Disponibles

### OAuth / Conexión

```bash
# Iniciar OAuth (abre en navegador)
GET http://localhost:8080/auth/instagram/connect?userId=<uuid>

# Ver estado de conexión
GET http://localhost:8080/auth/instagram/status?userId=<uuid>

# Desconectar
POST http://localhost:8080/auth/instagram/disconnect?userId=<uuid>

# Obtener URL de autorización (sin redireccionar)
GET http://localhost:8080/auth/instagram/authorization-url?userId=<uuid>
```

### Threads (Conversaciones)

```bash
# Listar todas las conversaciones
GET http://localhost:8080/v1/threads

# Obtener una conversación específica
GET http://localhost:8080/v1/threads/{thread-id}

# Ver mensajes de una conversación
GET http://localhost:8080/v1/threads/{thread-id}/messages
```

### Mensajes

```bash
# Enviar mensaje
POST http://localhost:8080/v1/messages
Content-Type: application/json

{
  "channel": "INSTAGRAM",
  "accountId": "uuid-de-la-cuenta",
  "to": [{"id": "instagram_user_id"}],
  "text": "Hola! Este es un mensaje de prueba."
}
```

### Tiempo Real (SSE)

```bash
# Conectarse al stream de eventos
GET http://localhost:8080/v1/stream
Accept: text/event-stream
```

---

## 🛠 Gestionar los Servicios

### Ver Logs

```bash
# Ver logs en tiempo real
tail -f api-gateway.log
tail -f ig-service.log
```

### Detener Servicios

```bash
# Matar procesos
kill $(cat api-gateway.pid)
kill $(cat ig-service.pid)

# O más directo
pkill -f "api-gateway.jar"
pkill -f "ig-service.jar"
```

### Reiniciar Servicios

```bash
# Detener
kill $(cat api-gateway.pid) $(cat ig-service.pid)

# Levantar de nuevo
java -jar api-gateway/build/libs/api-gateway.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/inbox \
  --spring.datasource.username=iroldan \
  --spring.datasource.password= \
  > api-gateway.log 2>&1 &

java -jar ig-service/build/libs/ig-service.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/inbox \
  --spring.datasource.username=iroldan \
  --spring.datasource.password= \
  --flyway.enabled=false \
  > ig-service.log 2>&1 &
```

---

## 🗄️ PostgreSQL

### Conectarse a la Base de Datos

```bash
/opt/homebrew/opt/postgresql@15/bin/psql -d inbox
```

### Queries Útiles

```sql
-- Ver cuentas conectadas
SELECT id, channel, display_name, status, created_at 
FROM accounts;

-- Ver threads
SELECT id, channel, external_thread_id, last_message_at 
FROM threads 
ORDER BY last_message_at DESC;

-- Ver mensajes
SELECT id, direction, body_text, status, created_at 
FROM messages 
ORDER BY created_at DESC 
LIMIT 10;

-- Estadísticas
SELECT 
    direction,
    channel,
    COUNT(*) as total
FROM messages 
GROUP BY direction, channel;
```

---

## 📚 Documentación

| Archivo | Descripción |
|---------|-------------|
| `INSTAGRAM_STATUS.md` | Estado completo del proyecto |
| `INSTAGRAM_SEND_TEST.md` | Cómo probar envío de mensajes |
| `FRONTEND_API_GUIDE.md` | Integración con frontend |
| `INSTAGRAM_SETUP.md` | Configuración de Meta Developer |
| `INSTAGRAM_OAUTH_GUIDE.md` | Flujo OAuth explicado |

---

## ⚠️ Importante

### Para que los Webhooks funcionen (recibir mensajes):

1. **Instalar ngrok**:
   ```bash
   brew install ngrok
   ```

2. **Levantar tunnel**:
   ```bash
   ngrok http 8081
   ```

3. **Configurar en Meta**:
   - Ve a https://developers.facebook.com/apps/813439077768151/webhooks
   - Agrega la URL de ngrok: `https://tu-ngrok.ngrok.io/webhooks/instagram`
   - Verify Token: `demo_verify_token_secreto`
   - Suscribirse al evento: `messages`

### Variables de Entorno Necesarias

Para funcionalidad completa, asegúrate de tener en tu `.env`:

```env
# Instagram OAuth (necesario para conectar cuentas)
INSTAGRAM_CLIENT_ID=813439077768151
INSTAGRAM_CLIENT_SECRET=tu_app_secret_de_meta

# Instagram Webhooks (necesario para recibir mensajes)
IG_VERIFY_TOKEN=demo_verify_token_secreto
```

---

## 🎉 ¡Listo para Usar!

Tu sistema de inbox unificado está funcionando y listo para:

✅ Conectar cuentas de Instagram Business via OAuth  
✅ Recibir mensajes de clientes (con ngrok + webhooks)  
✅ Enviar mensajes a clientes  
✅ Ver historial de conversaciones  
✅ Notificaciones en tiempo real (SSE)  

**Siguiente paso sugerido**: Construir el frontend usando `FRONTEND_API_GUIDE.md`

---

**Fecha**: Octubre 2025  
**Versión**: 1.0.0  
**Estado**: ✅ Production Ready (MVP)

