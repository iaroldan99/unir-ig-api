# 🔧 Cómo Agregar Instagram a tu App de Meta

## ❌ Error: "Invalid platform app"

Este error significa que Instagram NO está agregado como producto en tu app de Meta.

---

## ✅ Solución: Agregar Instagram Graph API

### PASO 1: Abrir tu App

Ya abrí esta página en tu navegador:
```
https://developers.facebook.com/apps/813439077768151/
```

---

### PASO 2: Encontrar el Botón "Add Product"

**En el menú LATERAL IZQUIERDO**, busca:

```
📱 Dashboard
⚙️  Settings
📊 Analytics
...
[+] Add Product  ← ESTE BOTÓN
```

**Alternativas del nombre:**
- "Add Product"
- "Agregar producto"
- "+ Add Product"
- "Add products"

**¿No lo ves?**
- Scroll hacia abajo en el menú lateral
- Puede estar al final de la lista

---

### PASO 3: Seleccionar Instagram

Una vez que haces clic en "Add Product", verás una lista de productos:

```
┌─────────────────────────────────────┐
│  Facebook Login                     │
│  [Set Up]                           │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Instagram                          │  ← ESTE
│  [Set Up]                           │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Messenger                          │
│  [Set Up]                           │
└─────────────────────────────────────┘
```

**Haz clic en "Set Up" del producto Instagram**

---

### PASO 4: Elegir el Tipo Correcto

Puede que te pregunte qué tipo de integración quieres:

**✅ Selecciona: Instagram Graph API**
- Para negocios
- Para enviar/recibir mensajes
- Requiere cuenta Business

**❌ NO selecciones: Instagram Basic Display**
- Solo para ver posts
- No sirve para mensajes
- Para cuentas personales

---

### PASO 5: Completar Setup Básico

Después de agregar Instagram, puede pedirte:

1. **Nombre de visualización**: "Unir Backend" o el que quieras
2. **Descripción**: "Plataforma de mensajería unificada"
3. **Redirect URIs**: 
   ```
   http://localhost:8080/auth/instagram/callback
   ```

---

### PASO 6: Verificar que se Agregó

Una vez completado, deberías ver en el menú lateral:

```
📱 Dashboard
📸 Instagram  ← DEBE APARECER AQUÍ
   - Settings
   - Basic Display
   - Messenger
⚙️  Settings
[+] Add Product
```

---

## 🔄 Alternativa: Crear Nueva App

Si tienes problemas con tu app actual, puedes crear una nueva:

### 1. Ir a:
```
https://developers.facebook.com/apps/create/
```

### 2. Seleccionar tipo:
- **Business** (recomendado)
- O **Other** → **Business**

### 3. Información:
- Nombre: "Unir Backend Dev"
- Email: tu email
- Propósito: Business

### 4. Agregar Instagram:
- Inmediatamente después de crear
- Add Product → Instagram Graph API

### 5. Actualizar tu código:
```bash
# Editar estos valores con los de la nueva app
INSTAGRAM_CLIENT_ID=<nuevo_app_id>
INSTAGRAM_CLIENT_SECRET=<nuevo_app_secret>
```

---

## ✅ Confirmar que Funciona

Una vez agregado Instagram, prueba de nuevo:

```bash
# En tu navegador
http://localhost:8080/auth/instagram/connect?userId=550e8400-e29b-41d4-a716-446655440000
```

**Ahora debería mostrar:**
- Pantalla de autorización de Instagram/Facebook
- No el error "Invalid platform app"

---

## 🆘 Si Sigues con Problemas

Opciones:

### A) Verificar App Mode
Settings → Basic → App Mode debe estar en "Development"

### B) Verificar App ID
Settings → Basic → Copiar App ID
Verificar que sea: `813439077768151`

### C) Crear Nueva App
Más fácil a veces que arreglar una existente

---

## 📸 Referencias Visuales

El menú lateral de Meta Developers se ve así:

```
╔══════════════════════════════╗
║  [Logo] Tu App Name          ║
╠══════════════════════════════╣
║  📱 Dashboard                ║
║  📸 Instagram (si agregado)  ║
║  📊 Analytics                ║
║  ⚙️  Settings                ║
║     - Basic                  ║
║     - Advanced               ║
║  🔔 Webhooks                 ║
║  👥 Roles                    ║
║  📋 App Review               ║
║  [+] Add Product             ║
╚══════════════════════════════╝
```

---

**Una vez agregado Instagram, escribe "listo" y continuamos con el OAuth.**

