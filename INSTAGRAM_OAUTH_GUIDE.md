# 🔐 Instagram OAuth Flow - Multi-Tenant

Esta guía explica cómo implementar OAuth para que **cada usuario** pueda conectar su propia cuenta de Instagram.

## 🎯 Objetivo

Permitir que cualquier usuario de tu plataforma:
1. Haga clic en "Conectar Instagram"
2. Autorice tu app en Instagram
3. Sus mensajes de Instagram aparezcan en tu inbox unificado

## 📋 Pre-requisitos en Meta

### 1. Configurar OAuth Redirect URIs

1. Ve a https://developers.facebook.com/apps/
2. Selecciona tu app **unir-backend**
3. Ve a **Settings → Basic**
4. En **"App Domains"** agrega:
   ```
   localhost
   tudominio.com
   ```

5. Ve a **Instagram → Basic Display**
6. En **"Valid OAuth Redirect URIs"** agrega:
   ```
   http://localhost:8080/auth/instagram/callback
   https://tudominio.com/auth/instagram/callback
   ```

### 2. Obtener Client ID y Client Secret

1. En **Settings → Basic**, copia:
   - **App ID** → Este es tu `INSTAGRAM_CLIENT_ID`
   - **App Secret** → Este es tu `INSTAGRAM_CLIENT_SECRET`

## 🔄 Flujo OAuth Completo

```
┌─────────┐                                   ┌─────────────┐
│ Usuario │                                   │  Instagram  │
└────┬────┘                                   └──────┬──────┘
     │                                               │
     │ 1. Click "Conectar Instagram"                │
     │ ──────────────────────────────────>          │
     │         (Frontend)                           │
     │                                              │
     │                                              │
┌────▼──────┐                                      │
│ API       │                                      │
│ Gateway   │                                      │
└────┬──────┘                                      │
     │                                              │
     │ 2. Redirect a Instagram OAuth                │
     │ ─────────────────────────────────────────>  │
     │   /oauth/authorize?client_id=...             │
     │                                              │
     │ 3. Usuario autoriza                          │
     │ <─────────────────────────────────────────  │
     │                                              │
     │ 4. Redirect de vuelta con CODE               │
     │ <─────────────────────────────────────────  │
     │   /callback?code=ABC123                      │
     │                                              │
     │ 5. Exchange code por ACCESS_TOKEN            │
     │ ─────────────────────────────────────────>  │
     │   POST /oauth/access_token                   │
     │                                              │
     │ 6. Recibe access_token + user_id             │
     │ <─────────────────────────────────────────  │
     │                                              │
     │ 7. Guarda en DB (tabla accounts)             │
     │ ──────────────────────────────────>          │
     │         encrypted                            │
     │                                              │
     │ 8. Retorna success al frontend               │
     │ ──────────────────────────────────>          │
     │                                              │
```

## 🔧 Implementación

### PASO 1: Agregar Configuración

Edita `application.yml` del **api-gateway**:

```yaml
instagram:
  oauth:
    client-id: ${INSTAGRAM_CLIENT_ID:813439077768151}
    client-secret: ${INSTAGRAM_CLIENT_SECRET:tu_app_secret}
    redirect-uri: ${INSTAGRAM_REDIRECT_URI:http://localhost:8080/auth/instagram/callback}
    authorization-url: https://api.instagram.com/oauth/authorize
    token-url: https://api.instagram.com/oauth/access_token
    scopes: user_profile,user_media
```

### PASO 2: Actualizar .env

Agrega a tu `.env`:

```env
INSTAGRAM_CLIENT_ID=813439077768151
INSTAGRAM_CLIENT_SECRET=tu_app_secret_aqui
INSTAGRAM_REDIRECT_URI=http://localhost:8080/auth/instagram/callback
```

## 📝 Endpoints a Implementar

### 1. Iniciar OAuth Flow

```
GET /auth/instagram/connect?user_id=<uuid>

Redirige al usuario a Instagram para autorizar
```

### 2. Callback de Instagram

```
GET /auth/instagram/callback?code=ABC123

Recibe el código, lo intercambia por token, guarda en DB
```

### 3. Verificar Estado de Conexión

```
GET /auth/instagram/status?user_id=<uuid>

Retorna si el usuario tiene Instagram conectado
```

### 4. Desconectar

```
POST /auth/instagram/disconnect?user_id=<uuid>

Elimina la conexión de Instagram del usuario
```

## 🔐 Scopes Necesarios

Para Instagram Basic Display (usuarios finales):

- `user_profile` - Información básica del perfil
- `user_media` - Acceso a fotos y videos

**IMPORTANTE**: Instagram Basic Display **NO soporta mensajería**.

Para mensajería necesitas **Instagram Messaging API** que requiere:
- Usuario sea Instagram Business o Creator
- Cuenta vinculada a Página de Facebook
- Permisos: `instagram_basic`, `instagram_manage_messages`, `pages_manage_metadata`

## ⚠️ LIMITACIÓN CRÍTICA

Instagram Messaging API **SOLO funciona para cuentas Business/Creator**, NO para cuentas personales.

**Esto significa:**
- ❌ Un usuario normal NO puede usar tu plataforma para ver sus DMs
- ✅ Un negocio SÍ puede conectar su cuenta Business

**Alternativas para cuentas personales:**
1. **Instagram Basic Display** - Solo posts/fotos, NO mensajes
2. **Scraping** - Contra términos de servicio, no recomendado
3. **Extension de Chrome** - Accede como el usuario pero limitado

## 🎯 Recomendación

Para tu caso de uso (inbox unificado), enfócate en:

1. **Instagram**: Solo para cuentas Business (actual implementación)
2. **WhatsApp**: Usa WhatsApp Business API
3. **Gmail**: OAuth completo funciona para cualquier usuario

Si quieres que usuarios normales conecten Instagram:
- Solo puedes mostrar sus posts/stories
- NO puedes acceder a sus mensajes (limitación de Meta)

## 🚀 Decisión de Arquitectura

### Opción A: Solo Business Accounts (Recomendado)
```
Tu plataforma = Para negocios que quieren gestionar sus mensajes
Usuario = Business con Instagram Business/WhatsApp Business/Gmail
```

### Opción B: Híbrido
```
Instagram: Solo business accounts (mensajes)
WhatsApp: Business API
Gmail: Todos los usuarios (OAuth estándar)
```

### Opción C: Enfocarse en Gmail + WhatsApp Business
```
Deja Instagram para v2.0
Enfoca MVP en Gmail (fácil OAuth) + WhatsApp Business
```

## 📚 Documentación Oficial

- Instagram Basic Display: https://developers.facebook.com/docs/instagram-basic-display-api
- Instagram Messaging: https://developers.facebook.com/docs/messenger-platform/instagram
- OAuth 2.0: https://oauth.net/2/

---

**Siguiente paso**: ¿Quieres que implemente los endpoints de OAuth o prefieres primero definir si vas con Business accounts o necesitas otra solución?

