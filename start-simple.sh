#!/bin/bash

# ============================================
# Script de inicio SIMPLE - SIN DOCKER
# Usa H2 en memoria para desarrollo rápido
# ============================================

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 UNIR IG API - Inicio Simple (Sin Docker)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# ----------------------------------------
# Verificar credenciales
# ----------------------------------------
if [ -z "$INSTAGRAM_CLIENT_ID" ]; then
    echo -e "${RED}⚠️  INSTAGRAM_CLIENT_ID no configurado${NC}"
    echo ""
    echo "Usa tus credenciales:"
    echo ""
    echo "  export INSTAGRAM_CLIENT_ID=\"TU_APP_ID\""
    echo "  export INSTAGRAM_CLIENT_SECRET=\"TU_APP_SECRET\""
    echo ""
    exit 1
fi

echo -e "${GREEN}✓${NC} Credenciales configuradas"
echo "  App ID: ${INSTAGRAM_CLIENT_ID}"
echo ""

# ----------------------------------------
# Compilar
# ----------------------------------------
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔨 Compilando proyectos"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

./gradlew clean build -x test --no-daemon

echo ""
echo -e "${GREEN}✓${NC} Compilación exitosa"
echo ""

# ----------------------------------------
# Crear directorio para logs
# ----------------------------------------
mkdir -p logs

# ----------------------------------------
# Iniciar servicios
# ----------------------------------------
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 Iniciando servicios"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Usar H2 en memoria (no necesita instalación)
export SPRING_DATASOURCE_URL="jdbc:h2:mem:inbox;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
export SPRING_DATASOURCE_USERNAME="sa"
export SPRING_DATASOURCE_PASSWORD=""
export SPRING_DATASOURCE_DRIVER="org.h2.Driver"

echo -e "${YELLOW}ℹ️  Usando base de datos H2 en memoria (sin persistencia)${NC}"
echo ""

echo -e "${BLUE}→ Iniciando API Gateway (puerto 8080)...${NC}"
INSTAGRAM_CLIENT_ID="$INSTAGRAM_CLIENT_ID" \
INSTAGRAM_CLIENT_SECRET="$INSTAGRAM_CLIENT_SECRET" \
INSTAGRAM_REDIRECT_URI="http://localhost:8080/auth/instagram/callback" \
SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
./gradlew :api-gateway:bootRun --no-daemon > logs/api-gateway.log 2>&1 &
API_GATEWAY_PID=$!
echo "  PID: $API_GATEWAY_PID"
echo "$API_GATEWAY_PID" > logs/api-gateway.pid

sleep 3

echo ""
echo -e "${BLUE}→ Iniciando IG Service (puerto 8081)...${NC}"
IG_VERIFY_TOKEN="demo_token" \
SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
./gradlew :ig-service:bootRun --no-daemon > logs/ig-service.log 2>&1 &
IG_SERVICE_PID=$!
echo "  PID: $IG_SERVICE_PID"
echo "$IG_SERVICE_PID" > logs/ig-service.pid

echo ""
echo -e "${YELLOW}⏳ Esperando a que los servicios inicien...${NC}"
echo "   (Esto puede tomar 30-60 segundos)"
echo ""

# Mostrar progreso
for i in {1..30}; do
    echo -n "."
    sleep 2
done
echo ""

echo ""
echo -e "${GREEN}✓${NC} Servicios iniciados"
echo ""

# ----------------------------------------
# Resumen
# ----------------------------------------
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ TODO LISTO"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🌐 Servicios corriendo:"
echo "   • API Gateway:  http://localhost:8080"
echo "   • IG Service:   http://localhost:8081"
echo ""
echo "💾 Base de datos:"
echo "   • H2 en memoria (se borra al reiniciar)"
echo ""
echo "📄 Ver logs:"
echo "   • tail -f logs/api-gateway.log"
echo "   • tail -f logs/ig-service.log"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔗 CONECTAR TU CUENTA DE INSTAGRAM"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Copia y pega esta URL en tu navegador:"
echo ""
echo -e "${GREEN}http://localhost:8080/auth/instagram/connect?userId=00000000-0000-0000-0000-000000000001${NC}"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "💡 Detener servicios: ./stop-dev.sh"
echo ""
echo "⏳ Mostrando logs del API Gateway..."
echo "   (Presiona Ctrl+C para salir de los logs)"
echo ""
sleep 2

# Seguir los logs
tail -f logs/api-gateway.log

