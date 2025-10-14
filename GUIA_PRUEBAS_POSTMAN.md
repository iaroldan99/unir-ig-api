# 🧪 Guía de Pruebas con Postman

## ⚠️ Importante: El Login NO se hace con Postman

El flujo de OAuth de Instagram **se hace con el navegador**, no con Postman, porque:
- Necesitas autorizar permisos en la interfaz de Facebook/Instagram
- Hay redirecciones que Postman no maneja bien
- Es un flujo visual que requiere interacción humana

---

## 📋 Flujo Completo de Prueba

### PASO 1: Conectar tu Cuenta de Instagram (Navegador)

**1.1 Copia esta URL y pégala en tu navegador:**

```
http://localhost:8080/auth/instagram/connect?userId=00000000-0000-0000-0000-000000000001
```

**1.2 Te redirigirá a Facebook/Instagram:**
- Inicia sesión si no lo has hecho
- Selecciona la **página de Facebook** vinculada a tu Instagram Business
- Acepta los permisos solicitados

**1.3 Callback:**
- Después de autorizar, te redirigirá a una URL como:
  ```
  http://localhost:3000/settings/accounts?connected=instagram&userId=...
  ```
- Esto dará error (porque no tienes frontend), pero **es normal**
- Lo importante es que en los logs verás: "Cuenta de Instagram conectada exitosamente"

**1.4 Verificar en logs:**
```bash
# Ver logs del API Gateway
tail -50 logs/api-gateway.log | grep -i "instagram\|token\|account"
```

Deberías ver algo como:
```
Obtenido short-lived access token.
Obtenido long-lived access token...
Cuenta de Instagram conectada exitosamente
```

---

### PASO 2: Verificar Estado de Conexión (Postman)

Una vez conectada la cuenta, puedes verificar el estado:

**Request:**
```
GET http://localhost:8080/auth/instagram/status?userId=00000000-0000-0000-0000-000000000001
```

**Response esperado (si está conectada):**
```json
{
  "connected": true,
  "accountId": "a1b2c3d4-...",
  "displayName": "Mi Negocio",
  "username": "mi_negocio_ig",
  "instagramUserId": "17841400000000000",
  "connectedAt": "2025-10-14T10:30:00Z"
}
```

**Response (si NO está conectada):**
```json
{
  "connected": false
}
```

---

### PASO 3: Enviar un Mensaje (Postman)

**⚠️ IMPORTANTE:** 
- Solo puedes enviar mensajes a usuarios que **te hayan enviado un mensaje primero** en las últimas 24 horas
- Esto es una limitación de Instagram (política de "user-initiated conversations")

**Request:**
```
POST http://localhost:8080/v1/messages
Content-Type: application/json

{
  "channel": "INSTAGRAM",
  "accountId": "a1b2c3d4-...",  // El que obtuviste en el PASO 2
  "to": [
    {
      "id": "17841400000000000"  // Instagram User ID del destinatario
    }
  ],
  "text": "Hola! Este es un mensaje de prueba desde la API"
}
```

**Response esperado (éxito):**
```json
{
  "id": "uuid-del-mensaje",
  "threadId": "uuid-del-thread",
  "channel": "INSTAGRAM",
  "direction": "OUTBOUND",
  "externalMessageId": "mid.xxx",
  "sender": {
    "id": "17841400000000000",
    "name": "Mi Negocio"
  },
  "bodyText": "Hola! Este es un mensaje de prueba desde la API",
  "status": "sent",
  "createdAt": "2025-10-14T10:45:00Z"
}
```

**Response (si hay error):**
```json
{
  "bodyText": "Failed to send Instagram message: ...",
  "status": "failed"
}
```

---

### PASO 4: Recibir Mensajes (Webhook)

Para recibir mensajes, necesitas:

**4.1 Exponer tu servidor local con ngrok:**

```bash
# En otra terminal
ngrok http 8081
```

Copia la URL que te da (ej: `https://abc123.ngrok-free.app`)

**4.2 Configurar el webhook en Meta:**

1. Ve a: https://developers.facebook.com/apps/TU_APP_ID/webhooks/
2. En "Instagram", haz clic en "Edit"
3. Callback URL: `https://abc123.ngrok-free.app/webhooks/instagram`
4. Verify Token: `demo_token`
5. Haz clic en "Verify and Save"
6. Suscríbete a los campos: `messages`, `messaging_postbacks`

**4.3 Probar recepción:**

Desde **otra cuenta de Instagram** (tu cuenta personal):
1. Envía un DM a tu cuenta de Instagram Business
2. Escribe: "Hola, esto es una prueba"
3. Verás en los logs del IG Service:
   ```
   Webhook event received: {...}
   Creando nuevo thread para sender: 17841...
   Mensaje procesado: uuid en thread uuid
   ```

**4.4 Verificar en DB (opcional):**

Si quieres ver los mensajes guardados:
```bash
# Conectar a H2 (si usas H2 en memoria)
# Los datos están en memoria, no hay DB persistente
```

---

### PASO 5: Listar Threads (Postman)

Después de recibir mensajes, puedes listar las conversaciones:

**Request:**
```
GET http://localhost:8080/v1/threads?page=0&size=20
```

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "accountId": "uuid",
      "channel": "INSTAGRAM",
      "externalThreadId": "17841...",
      "participants": [
        {
          "id": "17841...",
          "name": "Instagram User"
        }
      ],
      "lastMessageAt": "2025-10-14T10:50:00Z",
      "createdAt": "2025-10-14T10:45:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

---

### PASO 6: Listar Mensajes de un Thread (Postman)

**Request:**
```
GET http://localhost:8080/v1/messages/threads/{threadId}?page=0&size=20
```

Reemplaza `{threadId}` con el ID del thread del paso anterior.

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "threadId": "uuid",
      "channel": "INSTAGRAM",
      "direction": "INBOUND",
      "externalMessageId": "mid.xxx",
      "sender": {
        "id": "17841...",
        "name": "Instagram User"
      },
      "bodyText": "Hola, esto es una prueba",
      "status": "received",
      "createdAt": "2025-10-14T10:45:00Z"
    },
    {
      "id": "uuid",
      "threadId": "uuid",
      "channel": "INSTAGRAM",
      "direction": "OUTBOUND",
      "externalMessageId": "mid.yyy",
      "sender": {
        "id": "17841...",
        "name": "Mi Negocio"
      },
      "bodyText": "Hola! Este es un mensaje de prueba desde la API",
      "status": "sent",
      "createdAt": "2025-10-14T10:46:00Z"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

---

## 📊 Colección de Postman

### Crear una Colección

1. Abre Postman
2. Crea una nueva colección: "Unir IG API"
3. Agrega estas variables de colección:
   - `baseUrl`: `http://localhost:8080`
   - `userId`: `00000000-0000-0000-0000-000000000001`
   - `accountId`: (lo obtendrás después del login)
   - `threadId`: (lo obtendrás después de recibir mensajes)

### Requests a Crear

#### 1. Check Instagram Status
```
GET {{baseUrl}}/auth/instagram/status?userId={{userId}}
```

#### 2. Send Message
```
POST {{baseUrl}}/v1/messages
Content-Type: application/json

{
  "channel": "INSTAGRAM",
  "accountId": "{{accountId}}",
  "to": [{"id": "INSTAGRAM_USER_ID_AQUI"}],
  "text": "Mensaje de prueba"
}
```

#### 3. List Threads
```
GET {{baseUrl}}/v1/threads?page=0&size=20
```

#### 4. List Messages
```
GET {{baseUrl}}/v1/messages/threads/{{threadId}}?page=0&size=20
```

---

## 🐛 Troubleshooting

### "Account not found"
→ Aún no has conectado tu cuenta de Instagram. Ve al PASO 1.

### "Access token not configured"
→ El token no se guardó correctamente. Revisa los logs y vuelve a conectar la cuenta.

### "Failed to send Instagram message"
→ Posibles causas:
- El destinatario no te envió un mensaje en las últimas 24 horas
- El Instagram User ID es incorrecto
- El token expiró (reconecta la cuenta)

### "No veo mensajes entrantes"
→ Verifica:
- El webhook está configurado correctamente en Meta
- Ngrok está corriendo
- El verify token es "demo_token"
- Los logs del IG Service muestran las peticiones

---

## 💡 Tips

- **Ver logs en tiempo real:**
  ```bash
  # Terminal 1
  tail -f logs/api-gateway.log
  
  # Terminal 2
  tail -f logs/ig-service.log
  ```

- **Detener servicios:**
  ```bash
  ./stop-dev.sh
  ```

- **Reiniciar servicios:**
  ```bash
  ./stop-dev.sh
  INSTAGRAM_CLIENT_ID="TU_APP_ID" \
  INSTAGRAM_CLIENT_SECRET="TU_APP_SECRET" \
  ./start-simple.sh
  ```

- **Probar webhook manualmente (sin ngrok):**
  ```bash
  curl -X POST http://localhost:8081/webhooks/instagram \
    -H "Content-Type: application/json" \
    -d '{
      "object": "instagram",
      "entry": [{
        "id": "123",
        "time": 1234567890,
        "messaging": [{
          "sender": {"id": "123"},
          "recipient": {"id": "456"},
          "timestamp": 1234567890,
          "message": {
            "mid": "mid.123",
            "text": "Test message"
          }
        }]
      }]
    }'
  ```

---

## 🎯 Resumen del Flujo

```
1. Navegador → OAuth → Conectar cuenta IG
2. Postman → GET /auth/instagram/status → Verificar conexión
3. Instagram App → Enviar DM a tu cuenta Business → Webhook recibe mensaje
4. Postman → GET /v1/threads → Ver conversaciones
5. Postman → GET /v1/messages/threads/{id} → Ver mensajes
6. Postman → POST /v1/messages → Responder mensaje
```

