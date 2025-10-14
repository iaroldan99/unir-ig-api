# 📸 Guía de Configuración de Instagram API

Esta guía te ayudará a configurar completamente la integración con Instagram para recibir y enviar mensajes.

## 📋 Pre-requisitos

- ✅ Cuenta de Meta Developers (https://developers.facebook.com/)
- ✅ Cuenta de Instagram Business/Creator
- ✅ Página de Facebook vinculada a tu cuenta de Instagram
- ✅ Tu aplicación creada en Meta Developers

---

## 🔧 PASO 1: Configurar la Aplicación en Meta Developers

### 1.1 Crear/Acceder a tu App

1. Ve a https://developers.facebook.com/apps/
2. Selecciona tu aplicación existente
3. Si no tienes una, crea una nueva:
   - Clic en "Create App"
   - Selecciona "Business" o "Consumer"
   - Completa los datos básicos

### 1.2 Agregar el Producto Instagram

1. En el dashboard de tu app, ve a "Add Product"
2. Busca **"Instagram"** y haz clic en "Set Up"
3. Esto agregará la sección "Instagram" al menú lateral

---

## 🔑 PASO 2: Obtener los Tokens y IDs Necesarios

### 2.1 Obtener PAGE ACCESS TOKEN

Este es el token más importante para enviar mensajes.

**Opción A: Desde Graph API Explorer (Recomendado para desarrollo)**

1. Ve a https://developers.facebook.com/tools/explorer/
2. Selecciona tu aplicación en el dropdown superior derecho
3. En "User or Page", selecciona tu **Página de Facebook** (no tu perfil personal)
4. Haz clic en "Generate Access Token"
5. Otorga los siguientes permisos:
   - `instagram_basic`
   - `instagram_manage_messages`
   - `pages_read_engagement`
   - `pages_manage_metadata`
6. Copia el token que aparece (empieza con `EAAB...`)

⚠️ **IMPORTANTE**: Este token caduca en 1-2 horas. Para producción necesitas un **Long-lived Token** o **System User Token**.

**Opción B: Token de Larga Duración (60 días)**

1. Con el token de arriba, haz este request:
```bash
curl -i -X GET "https://graph.facebook.com/v21.0/oauth/access_token?grant_type=fb_exchange_token&client_id=TU_APP_ID&client_secret=TU_APP_SECRET&fb_exchange_token=TOKEN_CORTO"
```

2. Esto te dará un token de 60 días

**Opción C: System User Token (No caduca - RECOMENDADO PRODUCCIÓN)**

1. Ve a Business Settings (https://business.facebook.com/settings/)
2. Ve a "Users" → "System Users"
3. Crea un System User o usa uno existente
4. Asigna la página de Facebook al System User con permisos completos
5. Genera un token con los permisos necesarios
6. Este token **no caduca** si no lo revocas

### 2.2 Obtener INSTAGRAM BUSINESS ACCOUNT ID (IG_USER_ID)

1. Con tu PAGE ACCESS TOKEN, ejecuta:

```bash
curl -i -X GET "https://graph.facebook.com/v21.0/me?fields=instagram_business_account&access_token=TU_PAGE_ACCESS_TOKEN"
```

2. La respuesta será algo como:
```json
{
  "instagram_business_account": {
    "id": "17841405793187218"
  },
  "id": "123456789"
}
```

3. El valor `17841405793187218` es tu **IG_USER_ID** (Instagram Business Account ID)

### 2.3 Verificar que tienes acceso correcto

Prueba que puedas leer la información del perfil:

```bash
curl -i -X GET "https://graph.facebook.com/v21.0/17841405793187218?fields=id,username,name,profile_picture_url&access_token=TU_PAGE_ACCESS_TOKEN"
```

Deberías ver los datos de tu cuenta de Instagram.

---

## 🔔 PASO 3: Configurar el Webhook

### 3.1 Levantar tu Servicio Localmente

```bash
cd /path/to/unir-ig-api

# En una terminal - Levantar base de datos (si tienes Docker)
docker compose up -d db

# Si NO tienes Docker, configura conexión a DB remota en .env

# En otra terminal - Levantar ig-service
./gradlew :ig-service:bootRun
```

Verifica que esté corriendo:
```bash
curl http://localhost:8081/health
# Respuesta: {"status":"UP","service":"ig-service","version":"1.0.0"}
```

### 3.2 Exponer con ngrok

```bash
# Si no tienes ngrok instalado
brew install ngrok

# O descarga de: https://ngrok.com/download

# Autenticar (primera vez)
ngrok config add-authtoken TU_TOKEN_DE_NGROK

# Exponer el puerto 8081
ngrok http 8081
```

Obtendrás una URL pública como:
```
Forwarding  https://abc123def456.ngrok.app -> http://localhost:8081
```

⚠️ **IMPORTANTE**: Cada vez que reinicies ngrok, la URL cambiará. Para URL fija, necesitas una cuenta de pago de ngrok.

### 3.3 Configurar Webhook en Meta

1. Ve a tu app en https://developers.facebook.com/apps/
2. En el menú lateral, ve a **Instagram → Configuration**
3. Busca la sección **"Webhooks"**
4. Haz clic en **"Configure Webhooks"** o **"Edit"**

Completa los campos:

**Callback URL:**
```
https://TU_URL_DE_NGROK.ngrok.app/webhooks/instagram
```

Ejemplo:
```
https://abc123def456.ngrok.app/webhooks/instagram
```

**Verify Token:**
```
demo_token
```
(O el valor que hayas configurado en tu `.env` como `IG_VERIFY_TOKEN`)

5. Haz clic en **"Verify and Save"**

Si todo está bien, Meta hará un GET request y recibirás un ✅ verde.

### 3.4 Suscribirse a Eventos

Después de verificar, debes suscribirte a los eventos:

1. En la misma sección de Webhooks, busca **"Webhook Fields"**
2. Suscríbete a:
   - ✅ `messages` (para recibir mensajes entrantes)
   - ✅ `messaging_postbacks` (para botones/quick replies)
   - ✅ `message_echoes` (opcional, para ver tus propios mensajes)

3. Haz clic en **"Save"**

---

## 🧪 PASO 4: Probar la Configuración

### 4.1 Test de Verificación

Simula el request de verificación de Meta:

```bash
curl -i "https://TU_URL_NGROK.ngrok.app/webhooks/instagram?hub.mode=subscribe&hub.challenge=test_challenge_12345&hub.verify_token=demo_token"
```

**Respuesta esperada:**
```
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8

test_challenge_12345
```

### 4.2 Agregar Testers de Instagram

Para poder enviar/recibir mensajes, necesitas agregar testers:

1. Ve a tu app → **Instagram → Basic Display**
2. Scroll hasta **"Instagram Testers"**
3. Haz clic en **"Add Instagram Testers"**
4. Busca y agrega cuentas de Instagram de prueba
5. **IMPORTANTE**: Los usuarios agregados deben **aceptar la invitación** desde su app de Instagram:
   - Instagram App → Settings → Apps and Websites → Tester Invites

### 4.3 Enviar un Mensaje de Prueba

Desde la cuenta de Instagram que agregaste como tester, **envía un DM** a tu cuenta de Instagram Business.

Verás en los logs de tu servicio algo como:

```
2025-01-14 10:30:15.123  INFO 12345 --- [nio-8081-exec-1] c.u.i.s.InstagramWebhookService : Creando nuevo thread para sender: 1234567890
2025-01-14 10:30:15.456  INFO 12345 --- [nio-8081-exec-1] c.u.i.s.InstagramWebhookService : Mensaje procesado: 550e8400-e29b-41d4-a716-446655440000 en thread 7c9e6679-7425-40de-944b-e03976b5e43c (idempotente por external_message_id)
```

### 4.4 Verificar que se guardó en la DB

```bash
# Via API Gateway
curl http://localhost:8080/v1/threads

# Deberías ver el thread creado con el mensaje
```

### 4.5 Responder al Mensaje

```bash
curl -X POST "http://localhost:8081/v1/ig/send" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "00000000-0000-0000-0000-000000000001",
    "toId": "1234567890",
    "text": "Hola! Este es un mensaje automático desde la API 🤖"
  }'
```

**Importante**: Reemplaza `1234567890` con el **IGSID** (Instagram Scoped ID) del usuario. Lo puedes obtener de:
- Los logs cuando recibes un mensaje
- El campo `sender.id` del webhook
- La tabla `threads` en la DB (campo `external_thread_id`)

---

## 📝 PASO 5: Configurar Variables de Entorno

Crea o actualiza tu archivo `.env` en la raíz del proyecto:

```bash
# Copia desde .env.example
cp .env.example .env
```

Edita `.env` con tus valores reales:

```env
# Database (si usas Docker local)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/inbox
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Instagram Configuration
IG_VERIFY_TOKEN=demo_token
IG_GRAPH_VERSION=v21.0
IG_PAGE_ACCESS_TOKEN=EAAB... (tu token de página)
IG_USER_ID=17841405793187218 (tu Instagram Business Account ID)

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001

# Service URLs
IG_SERVICE_URL=http://localhost:8081
```

---

## 🚨 Problemas Comunes y Soluciones

### Error: "Invalid OAuth access token"

**Causa**: Token expirado o inválido

**Solución**:
1. Regenera el token desde Graph API Explorer
2. Para producción, usa System User Token que no expira

### Error: "Forbidden" al verificar webhook

**Causa**: `IG_VERIFY_TOKEN` no coincide

**Solución**:
1. Verifica que el valor en `.env` sea exactamente igual al configurado en Meta
2. Reinicia el servicio después de cambiar `.env`

### No recibo webhooks

**Checklist**:
- ✅ ngrok está corriendo y la URL es correcta
- ✅ El servicio ig-service está corriendo (`curl http://localhost:8081/health`)
- ✅ Webhook verificado correctamente en Meta (✅ verde)
- ✅ Suscrito a eventos "messages"
- ✅ La cuenta que envía mensajes es un Instagram Tester aprobado
- ✅ Los logs de ngrok muestran el request llegando

### Error: "Cannot message users who are not in allowed list"

**Causa**: El usuario no está en la lista de testers

**Solución**:
1. Agrega al usuario como Instagram Tester en tu app
2. El usuario debe aceptar la invitación desde Instagram

### Error: "This message is sent outside the allowed window"

**Causa**: Solo puedes responder mensajes dentro de 24 horas (Instagram Policy)

**Solución**:
- Responde dentro de las 24 horas del último mensaje del usuario
- Para mensajes después de 24h, necesitas Message Tags o Sponsored Messages (requiere aprobación)

---

## 📊 Monitoreo y Debugging

### Ver logs en tiempo real

```bash
# Logs de ig-service
tail -f ig-service/logs/spring.log

# O en la consola donde corriste bootRun
```

### Ver requests de ngrok

ngrok tiene un dashboard web en:
```
http://localhost:4040
```

Ahí puedes ver todos los requests que llegan, incluyendo los webhooks de Instagram.

### Verificar payload del webhook

Los payloads completos se guardan en la columna `raw` de la tabla `messages`. Puedes consultarlos directamente en la DB para debugging.

---

## 🎯 Checklist Final

Antes de considerar la configuración completa:

- [ ] Obtuve el PAGE_ACCESS_TOKEN
- [ ] Obtuve el IG_USER_ID (Instagram Business Account ID)
- [ ] Configuré las variables en `.env`
- [ ] ig-service está corriendo y responde en `/health`
- [ ] ngrok está exponiendo el puerto 8081
- [ ] Webhook verificado exitosamente en Meta (✅ verde)
- [ ] Suscrito a eventos "messages" y "messaging_postbacks"
- [ ] Agregué testers de Instagram y aceptaron la invitación
- [ ] Envié un mensaje de prueba y se recibió en la DB
- [ ] Pude responder al mensaje correctamente

---

## 🔗 Links Útiles

- **Meta for Developers**: https://developers.facebook.com/
- **Graph API Explorer**: https://developers.facebook.com/tools/explorer/
- **Instagram API Docs**: https://developers.facebook.com/docs/instagram-api/
- **Messenger Platform (similar)**: https://developers.facebook.com/docs/messenger-platform/
- **Webhook Reference**: https://developers.facebook.com/docs/graph-api/webhooks/
- **ngrok Dashboard**: http://localhost:4040
- **Testing Tools**: https://developers.facebook.com/tools/

---

## 🆘 Soporte

Si tienes problemas:

1. **Revisa los logs**: Los logs de ig-service son muy detallados
2. **Revisa ngrok dashboard**: http://localhost:4040
3. **Consulta la documentación oficial de Meta**
4. **Verifica los permisos**: Asegúrate de tener todos los permisos necesarios
5. **Prueba con Graph API Explorer**: Verifica que tu token funcione directamente

---

**¡Listo! Tu integración con Instagram debería estar funcionando ahora.** 🎉

