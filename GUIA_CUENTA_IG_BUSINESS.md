# 🏢 Guía: Configurar Cuenta de Instagram Business

## ¿Qué Necesitas?

Para probar la integración necesitas:
- ✅ Una cuenta de **Instagram Business** (o Creator)
- ✅ Vinculada a una **Página de Facebook**
- ✅ Ser **administrador** de esa página

---

## Opción 1: ¿Ya Tienes una Cuenta de Instagram?

### Paso 1: Verificar si es Business

1. Abre la app de Instagram en tu teléfono
2. Ve a tu perfil
3. Toca el menú (☰) → Settings → Account
4. Busca "Switch to Professional Account"

**Si ves esto:** Tu cuenta es personal → Necesitas convertirla

**Si ves "Switch to Personal Account":** Ya es Business ✅

---

### Paso 2: Convertir a Business (si es necesario)

**Desde la app de Instagram:**

1. Ve a Settings → Account
2. Toca "Switch to Professional Account"
3. Selecciona una categoría (ej: "Product/Service")
4. Elige **"Business"** (no "Creator")
5. Conecta a una página de Facebook (o crea una nueva)

---

## Opción 2: Crear Todo desde Cero

### Paso 1: Crear una Página de Facebook

```
1. Ve a: https://www.facebook.com/pages/create
2. Nombre: "Test Unir Backend" (o el que quieras)
3. Categoría: "Shopping & Retail" o "Local Business"
4. Completa la información básica
5. Haz clic en "Create Page"
```

### Paso 2: Vincular Instagram

**Desde la página de Facebook que creaste:**

```
1. En tu página, ve a "Settings"
2. En el menú lateral, busca "Instagram"
3. Haz clic en "Connect Account"
4. Ingresa tus credenciales de Instagram
5. Confirma la vinculación
```

**Alternativa desde Instagram:**

```
1. Abre Instagram → Settings → Account
2. Toca "Linked Accounts"
3. Selecciona "Facebook"
4. Elige la página que creaste
5. Confirma la vinculación
```

---

## ✅ Verificar que Todo Esté Listo

### Checklist:

- [ ] Tienes una cuenta de Instagram Business
- [ ] Está vinculada a una página de Facebook
- [ ] Eres administrador de esa página
- [ ] Puedes ver la página en: https://business.facebook.com/latest/inbox/

---

## 🧪 Probar la Conexión

### Paso 1: Copiar las Credenciales de la App

```bash
# Ve a developers.facebook.com/apps/TU_APP
# En el Dashboard, copia:
# - App ID
# - App Secret (en Settings → Basic)
```

### Paso 2: Actualizar la Configuración Local

Edita el archivo `.env` (o crea uno si no existe):

```bash
# Meta App Credentials
INSTAGRAM_CLIENT_ID=TU_APP_ID_AQUI
INSTAGRAM_CLIENT_SECRET=TU_APP_SECRET_AQUI
INSTAGRAM_REDIRECT_URI=http://localhost:8080/auth/instagram/callback

# Webhook
IG_VERIFY_TOKEN=demo_token
```

### Paso 3: Iniciar los Servicios

```bash
# En el directorio del proyecto:
docker-compose up -d postgres
./gradlew :api-gateway:bootRun &
./gradlew :ig-service:bootRun &
```

### Paso 4: Probar el Flujo OAuth

1. **Abre en el navegador:**
   ```
   http://localhost:8080/auth/instagram/connect?userId=00000000-0000-0000-0000-000000000001
   ```

2. **Te redirigirá a Instagram/Facebook** para autorizar

3. **Selecciona:**
   - Tu página de Facebook
   - Los permisos solicitados (acepta todos)

4. **Después del callback**, deberías ver:
   - En la DB: Un nuevo registro en la tabla `accounts`
   - En los logs: "Cuenta de Instagram conectada exitosamente"

---

## 🆘 Problemas Comunes

### "No se puede conectar la cuenta"

**Causa:** La app está en modo Development

**Solución:**
1. Ve a developers.facebook.com/apps/TU_APP/roles/test-users/
2. Agrégarte como "Test User" O
3. Agrega tu cuenta de Facebook en "Roles" → "Roles"

### "No veo mi página de Instagram"

**Causa:** La página no está vinculada correctamente

**Solución:**
1. Ve a https://business.facebook.com/
2. Business Settings → Accounts → Instagram Accounts
3. Verifica que tu cuenta aparezca ahí
4. Si no aparece, vuelve a vincularla desde Instagram

### "Insufficient permissions"

**Causa:** Faltan permisos en la app

**Solución:**
1. Ve a developers.facebook.com/apps/TU_APP/instagram-basic-display/
2. Verifica que estos permisos estén habilitados:
   - `instagram_basic`
   - `instagram_manage_messages`
   - `pages_show_list`
   - `pages_read_engagement`
   - `pages_manage_metadata`

### "La app no puede acceder a esta cuenta"

**Causa:** La app está en modo Development y necesitas agregar la cuenta

**Solución:**
1. Ve a: App Dashboard → Roles → Roles
2. Haz clic en "Add Testers" o "Add People"
3. Agrega tu cuenta de Facebook/Instagram
4. La persona recibirá una invitación que debe aceptar

---

## 📱 Estructura de una Cuenta Business

```
┌─────────────────────────────────────┐
│  CUENTA DE FACEBOOK                 │
│  (tu cuenta personal)               │
│                                     │
│  └─> PÁGINA DE FACEBOOK             │
│       (ej: "Test Unir Backend")    │
│       Role: Admin                   │
│                                     │
│       └─> INSTAGRAM BUSINESS        │
│            (vinculada a la página) │
│            @tu_usuario_ig           │
└─────────────────────────────────────┘
```

**Importante:** 
- NO es tu perfil personal de Facebook
- NO es tu cuenta personal de Instagram
- ES una **página** de Facebook con Instagram vinculado

---

## 🎯 Siguiente Paso

Una vez que tengas la cuenta Business configurada:

```bash
# 1. Actualiza las credenciales en .env
# 2. Reinicia los servicios
# 3. Prueba el flujo OAuth
# 4. Envía un mensaje de prueba desde tu cuenta personal a la Business
# 5. Verifica que llegue al webhook
```

---

## 💡 Tips

- **Para desarrollo:** No necesitas una cuenta "real", puedes usar una de prueba
- **Páginas de prueba:** Puedes crear páginas temporales para testing
- **Graph API Explorer:** Usa https://developers.facebook.com/tools/explorer/ para probar llamadas manualmente
- **Debug Tool:** Usa https://developers.facebook.com/tools/debug/ para ver qué permisos tienes

---

## 📚 Referencias

- [Instagram Messaging Setup](https://developers.facebook.com/docs/messenger-platform/instagram/get-started)
- [Instagram Business Account](https://help.instagram.com/502981923235522)
- [Connect Instagram to Page](https://www.facebook.com/business/help/898752960195806)

