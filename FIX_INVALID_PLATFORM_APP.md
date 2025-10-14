# 🔧 Solución: Invalid Platform App

## 🎯 El Problema

Error: **"Invalid platform app"** al intentar conectar Instagram.

**Causa**: Tu app de Meta no tiene Instagram configurado correctamente como producto.

---

## ✅ Solución Paso a Paso

### PASO 1: Ir a Meta Developers

Ya abrí la página en tu navegador. Si no se abrió:
```
https://developers.facebook.com/apps/813439077768151/settings/basic/
```

---

### PASO 2: Verificar Estado de la App

En **Settings → Basic**:

1. Busca **"App Mode"**
2. Debe estar en: **Development** (no Live)
3. Si está en Live, cámbiala a Development

---

### PASO 3: Agregar Instagram como Producto

1. En el menú lateral izquierdo, busca **"Add Product"** o **"Productos"**

2. Busca **"Instagram"** (puede aparecer como "Instagram Graph API" o "Instagram Basic Display")

3. Haz clic en **"Set Up"** o **"Configurar"**

4. Selecciona el tipo de integración:
   - ✅ **Instagram Graph API** (para Business accounts)
   - ❌ NO uses "Instagram Basic Display" (solo para posts, no mensajes)

---

### PASO 4: Configurar Instagram Graph API

Después de agregar el producto:

1. Ve a **Instagram → Settings** o **Instagram → Configuration**

2. Verifica que tengas:
   - ✅ **App ID**: `813439077768151`
   - ✅ **Client OAuth Settings**
   - ✅ **Valid OAuth Redirect URIs**

3. En **"Valid OAuth Redirect URIs"** debe estar:
   ```
   http://localhost:8080/auth/instagram/callback
   ```
   
4. Si no está, agrégala y haz clic en **"Save Changes"**

---

### PASO 5: Configurar Permisos de la App

1. Ve a **App Review → Permissions and Features**

2. Busca y agrega estos permisos:
   - ✅ `instagram_basic`
   - ✅ `instagram_manage_messages`
   - ✅ `pages_manage_metadata`
   - ✅ `pages_read_engagement`

3. Para desarrollo, estos permisos están disponibles automáticamente.

---

### PASO 6: Agregar Usuarios de Prueba (Opcional)

Si quieres probar con cuentas que NO son administradores de la app:

1. Ve a **Roles → Test Users**
2. O ve a **Roles → Roles** y agrega tu cuenta como "Developer" o "Tester"

---

### PASO 7: Verificar App Secret

1. En **Settings → Basic**
2. Busca **"App Secret"**
3. Haz clic en **"Show"**
4. Copia el valor

**Importante**: Este valor debe estar en tu configuración.

---

## 🔄 Alternativa: Crear Nueva App

Si tu app actual tiene problemas, puedes crear una nueva:

### 1. Crear App

https://developers.facebook.com/apps/create/

1. Selecciona **"Business"** o **"Consumer"**
2. Nombre: `unir-backend-dev`
3. Email de contacto: tu email
4. Crea la app

### 2. Agregar Instagram

1. En la nueva app, ve a **Add Product**
2. Selecciona **Instagram Graph API**
3. Configura según los pasos anteriores

### 3. Actualizar tu Código

Edita la configuración:
```env
INSTAGRAM_CLIENT_ID=<nuevo_app_id>
INSTAGRAM_CLIENT_SECRET=<nuevo_app_secret>
```

---

## 🧪 Probar que Funciona

Una vez configurado:

```bash
# 1. Reiniciar servicios (si cambiaste credenciales)
kill $(cat api-gateway.pid)
# Levantar de nuevo con nuevas credenciales

# 2. Abrir navegador
open "http://localhost:8080/auth/instagram/connect?userId=550e8400-e29b-41d4-a716-446655440000"

# 3. Deberías ver pantalla de autorización de Instagram
```

---

## ⚠️ Errores Comunes

### "App Not Set Up"
**Solución**: Verifica que Instagram Graph API esté agregado como producto.

### "Redirect URI Mismatch"
**Solución**: Verifica que `http://localhost:8080/auth/instagram/callback` esté exactamente así (con http, no https).

### "Invalid Redirect URI"
**Solución**: En App Domains agrega `localhost`

---

## 📞 Si Nada Funciona

Opción rápida: Usar Graph API Explorer para obtener token manual

1. Ve a https://developers.facebook.com/tools/explorer/
2. Selecciona tu app
3. Agrega permisos manualmente
4. Genera User Access Token
5. Úsalo temporalmente para probar

---

## ✅ Checklist de Configuración

- [ ] App en modo **Development**
- [ ] **Instagram Graph API** agregado como producto
- [ ] Redirect URI configurado: `http://localhost:8080/auth/instagram/callback`
- [ ] App Domain incluye: `localhost`
- [ ] Permisos: `instagram_basic`, `instagram_manage_messages`
- [ ] App Secret copiado
- [ ] Tienes una cuenta de Instagram **Business** (no personal)
- [ ] Cuenta vinculada a una **Página de Facebook**

---

**Una vez completado**, vuelve a intentar:
```
http://localhost:8080/auth/instagram/connect?userId=550e8400-e29b-41d4-a716-446655440000
```

Deberías ver la pantalla de autorización de Instagram sin errores.

