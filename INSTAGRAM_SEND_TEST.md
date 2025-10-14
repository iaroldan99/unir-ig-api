# 🧪 Testing del Envío de Mensajes de Instagram

Esta guía te ayudará a probar el flujo completo de envío de mensajes de Instagram.

---

## ✅ Lo que funciona AHORA

El sistema completo de envío de mensajes está implementado y funcionando:

```
Frontend → POST /v1/messages → api-gateway 
    → POST /v1/ig/send → ig-service 
    → Instagram Graph API 
    → Mensaje enviado ✅
    → Guardado en DB ✅
    → SSE notification ✅
```

---

## 📋 Pre-requisitos

1. **Cuenta de Instagram Business** conectada via OAuth
2. **Access Token** guardado en la tabla `accounts`
3. **Servicios corriendo**:
   - api-gateway `:8080`
   - ig-service `:8081`
   - Postgres `:5432`

---

## 🚀 Pasos para Probar

### 1️⃣ Crear una Cuenta de Prueba en la DB

Primero necesitas una cuenta en la tabla `accounts` con un token válido.

**Opción A: Via OAuth** (recomendado)
```bash
# Abrir en el navegador
open "http://localhost:8080/auth/instagram/connect?userId=550e8400-e29b-41d4-a716-446655440000"

# Autorizar en Instagram
# El sistema guardará automáticamente la cuenta
```

**Opción B: Insertar manualmente** (solo para testing)
```sql
-- Conectar a Postgres
psql -U postgres -d inbox

-- Insertar cuenta de prueba
INSERT INTO accounts (
    id, 
    user_id, 
    channel, 
    display_name, 
    external_ids, 
    credentials_encrypted, 
    status, 
    created_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    '660e8400-e29b-41d4-a716-446655440000'::uuid,
    'INSTAGRAM',
    'Mi Tienda Test',
    '{"ig_user_id": "TU_IG_USER_ID", "username": "mitienda_oficial"}'::jsonb,
    '{"access_token": "TU_PAGE_ACCESS_TOKEN", "token_type": "bearer"}',
    'active',
    NOW()
);
```

**Reemplaza:**
- `TU_IG_USER_ID`: El ID de tu cuenta Business de Instagram
- `TU_PAGE_ACCESS_TOKEN`: Tu token de acceso (Page Access Token de Meta)

---

### 2️⃣ Probar con cURL

```bash
# Enviar mensaje
curl -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "INSTAGRAM",
    "accountId": "550e8400-e29b-41d4-a716-446655440000",
    "to": [
      {
        "id": "INSTAGRAM_USER_ID_DEL_DESTINATARIO"
      }
    ],
    "text": "Hola! Este es un mensaje de prueba desde mi plataforma unificada."
  }'
```

**Response esperado:**
```json
{
  "id": "some-uuid",
  "channel": "INSTAGRAM",
  "bodyText": "Hola! Este es un mensaje de prueba...",
  "status": "sent",
  "externalMessageId": "mid.aF123XYZ..."
}
```

---

### 3️⃣ Verificar en la DB

```sql
-- Ver mensajes enviados
SELECT 
    id,
    channel,
    direction,
    body_text,
    status,
    created_at
FROM messages
WHERE direction = 'OUTBOUND'
ORDER BY created_at DESC
LIMIT 10;

-- Ver threads creados
SELECT 
    id,
    channel,
    external_thread_id,
    participants,
    last_message_at
FROM threads
ORDER BY last_message_at DESC
LIMIT 5;
```

---

### 4️⃣ Verificar en Instagram

1. Abre Instagram (web o app)
2. Ve a tu inbox / mensajes directos
3. Deberías ver el mensaje que enviaste al destinatario
4. El mensaje aparecerá como enviado desde tu cuenta Business

---

### 5️⃣ Probar desde el Frontend

```javascript
// Ejemplo en React
const sendInstagramMessage = async () => {
  try {
    const response = await fetch('http://localhost:8080/v1/messages', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        channel: 'INSTAGRAM',
        accountId: '550e8400-e29b-41d4-a716-446655440000',
        to: [
          { id: 'instagram_user_id_destinatario' }
        ],
        text: 'Hola desde React!'
      })
    });
    
    const data = await response.json();
    console.log('Mensaje enviado:', data);
    alert(`Mensaje enviado exitosamente! ID: ${data.externalMessageId}`);
    
  } catch (error) {
    console.error('Error:', error);
    alert('Error enviando mensaje');
  }
};
```

---

## 🔍 Troubleshooting

### Error: "Account not found"

**Causa**: El `accountId` no existe en la tabla `accounts`

**Solución**:
```sql
-- Verificar que la cuenta existe
SELECT id, channel, display_name, status 
FROM accounts 
WHERE id = '550e8400-e29b-41d4-a716-446655440000';
```

---

### Error: "Access token not configured"

**Causa**: El token en `credentials_encrypted` es null o inválido

**Solución**:
```sql
-- Verificar el token
SELECT 
    id, 
    channel, 
    credentials_encrypted 
FROM accounts 
WHERE id = '550e8400-e29b-41d4-a716-446655440000';

-- Actualizar token si es necesario
UPDATE accounts 
SET credentials_encrypted = '{"access_token": "TU_TOKEN_AQUI", "token_type": "bearer"}'
WHERE id = '550e8400-e29b-41d4-a716-446655440000';
```

---

### Error: "Invalid OAuth access token"

**Causa**: El token de Instagram expiró o es inválido

**Solución**:
1. Ve a Meta Developers: https://developers.facebook.com/tools/explorer/
2. Selecciona tu app
3. Genera un nuevo User Token
4. Para testing, usa un System User Token (no expira)
5. Actualiza en la DB o reconecta via OAuth

---

### Error: "Unsupported request - method type: get"

**Causa**: Instagram Graph API solo acepta POST, no GET

**Solución**: Asegúrate de que el código esté usando `.post()` en lugar de `.get()`

---

### Error: "Invalid user id"

**Causa**: El `ig_user_id` en external_ids es incorrecto

**Solución**:
```bash
# Obtener tu Instagram Business Account ID correcto
curl -X GET \
  "https://graph.facebook.com/v24.0/me/accounts?fields=instagram_business_account&access_token=TU_TOKEN"
```

---

## 📊 Flujo Completo (Debugging)

### 1. Frontend envía request

```javascript
POST /v1/messages
{
  "channel": "INSTAGRAM",
  "accountId": "uuid",
  "to": [{"id": "ig_user_id"}],
  "text": "Hola"
}
```

### 2. api-gateway recibe (MessageController)

```
MessageController.sendMessage()
  ↓
MessageService.sendMessage()
  ↓
MessageService.sendInstagramMessage()
```

Logs esperados en api-gateway:
```
INFO  MessageController - Recibiendo request para enviar mensaje: channel=INSTAGRAM, accountId=...
INFO  MessageService - Enviando mensaje a canal: INSTAGRAM desde account: ...
```

### 3. api-gateway llama a ig-service

```
POST http://localhost:8081/v1/ig/send
{
  "accountId": "uuid",
  "toId": "ig_user_id",
  "text": "Hola"
}
```

### 4. ig-service procesa (SendController)

```
SendController.sendMessage()
  ↓
InstagramSendService.sendMessage()
  ↓
1. Busca Account en DB
  2. Extrae access_token
  3. Llama a Instagram Graph API
  4. Persiste mensaje outbound en DB
```

Logs esperados en ig-service:
```
INFO  SendController - Enviando mensaje de Instagram a ig_user_id_123
INFO  InstagramSendService - Enviando mensaje de Instagram desde account uuid a ig_user_id_123
INFO  InstagramSendService - Mensaje enviado exitosamente a Instagram. Message ID: mid.ABC123
INFO  InstagramSendService - Mensaje outbound persistido: uuid en thread uuid
```

### 5. Instagram Graph API responde

```json
{
  "recipient_id": "123456789",
  "message_id": "mid.aF123XYZ..."
}
```

### 6. api-gateway retorna al frontend

```json
{
  "id": "generated-uuid",
  "channel": "INSTAGRAM",
  "bodyText": "Hola",
  "status": "sent",
  "externalMessageId": "mid.aF123XYZ..."
}
```

### 7. SSE notifica en tiempo real

```
event: message.sent
data: {"messageId":"mid.aF123...","status":"sent"}
```

---

## ✅ Checklist de Verificación

Cuando hagas una prueba exitosa, deberías ver:

- [ ] Status HTTP 200 en la respuesta
- [ ] `externalMessageId` del tipo `mid.XXX` en la respuesta
- [ ] Log "Mensaje enviado exitosamente" en ig-service
- [ ] Registro nuevo en tabla `messages` con `direction='OUTBOUND'`
- [ ] Registro en tabla `threads` (nuevo o actualizado)
- [ ] Mensaje visible en Instagram del destinatario
- [ ] Evento SSE emitido (si tienes cliente conectado)

---

## 🎯 Casos de Prueba Recomendados

### Test 1: Mensaje simple
```json
{
  "channel": "INSTAGRAM",
  "accountId": "uuid",
  "to": [{"id": "destinatario_id"}],
  "text": "Hola, este es un mensaje de prueba"
}
```
**Esperado**: ✅ Mensaje enviado y persistido

### Test 2: Mensaje con emojis
```json
{
  "channel": "INSTAGRAM",
  "accountId": "uuid",
  "to": [{"id": "destinatario_id"}],
  "text": "Hola! 👋 ¿Cómo estás? 😊"
}
```
**Esperado**: ✅ Emojis se envían correctamente

### Test 3: Mensaje largo
```json
{
  "channel": "INSTAGRAM",
  "accountId": "uuid",
  "to": [{"id": "destinatario_id"}],
  "text": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam..."
}
```
**Esperado**: ✅ Mensaje largo se envía completo (hasta límite de Instagram)

### Test 4: Account inválido
```json
{
  "channel": "INSTAGRAM",
  "accountId": "00000000-0000-0000-0000-000000000000",
  "to": [{"id": "destinatario_id"}],
  "text": "Test"
}
```
**Esperado**: ❌ Error 500 "Account not found"

### Test 5: Destinatario inválido
```json
{
  "channel": "INSTAGRAM",
  "accountId": "uuid-valido",
  "to": [{"id": "invalid_user_12345"}],
  "text": "Test"
}
```
**Esperado**: ❌ Error de Instagram Graph API "Invalid user"

---

## 📚 Referencias

- **Instagram Messaging API**: https://developers.facebook.com/docs/messenger-platform/instagram/send-messages
- **Graph API Explorer**: https://developers.facebook.com/tools/explorer/
- **Docs del Proyecto**: Ver `FRONTEND_API_GUIDE.md`

---

## 🎉 Próximos Pasos

Una vez que el envío funcione:

1. ✅ Probar recepción de mensajes (webhooks)
2. ✅ Implementar envío con imágenes/adjuntos
3. ✅ Agregar indicadores de "leído" / "entregado"
4. ✅ Implementar respuestas rápidas (quick replies)
5. ✅ Agregar plantillas de mensajes
6. ✅ Dashboard de métricas de mensajería

---

**¿Problemas?** Consulta los logs:
```bash
# Logs de api-gateway
tail -f api-gateway/logs/application.log

# Logs de ig-service
tail -f ig-service/logs/application.log
```

¡Buena suerte con el testing! 🚀

