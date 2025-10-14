# 🚀 Instrucciones para Push a GitHub

## ✅ Estado Actual
- ✓ Repositorio Git inicializado
- ✓ Commit inicial creado
- ✓ Proyecto compilado exitosamente
- ✓ Configurado con tu cuenta Git local

## 📝 Pasos para crear el repositorio en GitHub

### Opción 1: Crear repositorio vía Web (Recomendado)

1. **Ve a GitHub**: https://github.com/new

2. **Configura el repositorio**:
   - Repository name: `unir-ig-api`
   - Description: `Unified Inbox API - Monorepo con Instagram, WhatsApp y Gmail integration`
   - Visibility: **Public** (o Private si prefieres)
   - ⚠️ **NO inicialices con README, .gitignore o licencia** (ya los tenemos)

3. **Crea el repositorio** haciendo clic en "Create repository"

4. **En tu terminal, ejecuta estos comandos**:

```bash
cd /path/to/unir-ig-api

# Agregar remote origin
git remote add origin https://github.com/TU_USUARIO/unir-ig-api.git

# Renombrar rama a main (si es necesario)
git branch -M main

# Push al repositorio
git push -u origin main
```

### Opción 2: Instalar GitHub CLI y crear automáticamente

```bash
# Instalar GitHub CLI
brew install gh

# Autenticarse
gh auth login

# Crear repositorio y hacer push automáticamente
cd /path/to/unir-ig-api
gh repo create TU_USUARIO/unir-ig-api --public --source=. --remote=origin --push
```

## 🔐 Autenticación

Si te pide credenciales al hacer push, tienes 2 opciones:

### Opción A: Personal Access Token (Recomendado)
1. Ve a https://github.com/settings/tokens
2. Generate new token (classic)
3. Selecciona scopes: `repo`, `workflow`
4. Copia el token
5. Úsalo como password cuando hagas push

### Opción B: SSH
```bash
# Generar SSH key
ssh-keygen -t ed25519 -C "tu_email@ejemplo.com"

# Agregar a ssh-agent
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519

# Copiar la clave pública
cat ~/.ssh/id_ed25519.pub
# Agrégala en: https://github.com/settings/ssh/new

# Cambiar remote a SSH
git remote set-url origin git@github.com:TU_USUARIO/unir-ig-api.git
git push -u origin main
```

## 🎉 Después del Push

Una vez que hayas hecho push exitoso, tu repositorio estará disponible en:

**https://github.com/TU_USUARIO/unir-ig-api**

## 📋 Próximos Pasos

1. Verifica que el repositorio esté público
2. Revisa el README en GitHub
3. Configura los secrets si vas a usar GitHub Actions
4. Agrega colaboradores si es necesario
5. Configura las Meta webhooks apuntando a tu ngrok/Railway URL

## 🆘 Soporte

Si encuentras algún problema:
- Verifica que estés logueado en GitHub
- Asegúrate de que el nombre del repositorio no esté ocupado
- Revisa que tengas permisos para crear repos en tu cuenta

---

¡Todo listo para hacer push! 🚀

