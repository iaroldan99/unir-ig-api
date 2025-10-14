# 📱 Guía: Agregar Instagram a tu App de Meta

## Paso a Paso con Imágenes Mentales

### PASO 1: Abrir el Dashboard de tu App
```
Ve a: https://developers.facebook.com/apps/
Selecciona tu app recién creada
```

---

### PASO 2: Ubicar el Menú Lateral Izquierdo

Verás un menú vertical como este:

```
┌─────────────────────────┐
│ 📊 Dashboard            │
│ 📈 Analytics            │
│ ⚙️  Settings            │
│                         │
│ 🔧 App Review           │
│ 👥 Roles                │
│                         │
│ [+] Add Products  ← 👈  │  ◄── HAZ CLIC AQUÍ
│                         │
└─────────────────────────┘
```

**Haz clic en `[+] Add Products`** (o puede decir "Add Product")

---

### PASO 3: Buscar Instagram

Después de hacer clic, verás una **lista de productos** disponibles:

```
┌────────────────────────────────┐
│ 📘 Facebook Login              │
│ 📷 Instagram                   │  ◄── ESTE
│ 💬 Messenger                   │
│ 🎮 Gaming                      │
│ ... otros productos            │
└────────────────────────────────┘
```

Cada producto tiene un botón **"Set Up"** o **"Configure"**

---

### PASO 4: Agregar Instagram

1. **Encuentra la tarjeta de Instagram**
2. **Haz clic en el botón "Set Up"** que está en esa tarjeta
3. Puede que te pregunte qué API usar:
   - ✅ Selecciona: **"Instagram Graph API"** 
   - ❌ NO selecciones: "Basic Display API"

---

### PASO 5: Confirmar

Después de hacer clic en "Set Up":
- Aparecerá un checkmark ✓ o dirá "Added"
- Instagram aparecerá en tu menú lateral como un nuevo item

---

## ⚠️ Si No Ves "Add Products"

### Posibles razones:

1. **No estás en la página correcta**
   - Asegúrate de estar en `developers.facebook.com/apps/TU_APP_ID/`
   - NO en la página de "All Apps"

2. **La app no está completamente creada**
   - Verifica que completaste el formulario de creación
   - Debe tener un App ID visible

3. **Necesitas refrescar la página**
   - Presiona `Cmd + R` (Mac) o `Ctrl + R` (Windows)

---

## 🆘 Solución de Problemas

### Si dice "Add Products" pero no hay botón:
```bash
# Opción 1: Puede estar más abajo, haz scroll
# Opción 2: Puede estar colapsado, busca un "+"
# Opción 3: Puede estar en Settings → Products
```

### Si Instagram ya aparece en el menú:
¡Ya está agregado! No necesitas hacer nada más.

### Si solo ves "Facebook Login":
Es normal que Facebook Login esté por defecto. Busca más abajo en la lista.

---

## ✅ ¿Cómo Saber si Funcionó?

Después de agregar Instagram, verás en el menú lateral:

```
┌─────────────────────────┐
│ 📊 Dashboard            │
│                         │
│ Products:               │
│ 📷 Instagram            │  ◄── NUEVO!
│   └─ Settings           │
│   └─ Messenger API      │
│   └─ Webhooks           │
│                         │
└─────────────────────────┘
```

---

## 📸 Qué Esperar Visualmente

### Vista de "Add Products":
```
┌──────────────────────────────────────────────┐
│  Available Products                           │
├──────────────────────────────────────────────┤
│                                               │
│  [Instagram Icon]  Instagram                  │
│  Build apps that let people share on         │
│  Instagram and interact with content.         │
│                                 [Set Up]  ←── │
│                                               │
├──────────────────────────────────────────────┤
│                                               │
│  [Messenger Icon]  Messenger                  │
│  Let people message your business.            │
│                                 [Set Up]      │
│                                               │
└──────────────────────────────────────────────┘
```

---

## 🎯 Siguiente Paso (Después de Agregar)

Una vez que Instagram esté agregado, necesitaremos:
1. ✅ Copiar el App ID
2. ✅ Copiar el App Secret
3. ✅ Configurar Webhooks
4. ✅ Conectar una cuenta de Instagram Business

---

**💡 Tip:** Si te pierdes, toma una captura de pantalla de lo que ves y te ayudo a ubicarte.

