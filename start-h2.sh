#!/bin/bash

# ============================================
# Script de inicio con H2 - Ejecutando JARs
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
echo "🚀 UNIR IG API - Inicio con H2"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# ----------------------------------------
# Verificar credenciales
# ----------------------------------------
if [ -z "$INSTAGRAM_CLIENT_ID" ]; then
    echo -e "${RED}⚠️  INSTAGRAM_CLIENT_ID no configurado${NC}"
    echo ""
    echo "Usa:"
    echo "  INSTAGRAM_CLIENT_ID=\"...\" INSTAGRAM_CLIENT_SECRET=\"...\" ./start-h2.sh"
    echo ""
    exit 1
fi

echo -e "${GREEN}✓${NC} Credenciales configuradas"
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
# Detener procesos anteriores
# ----------------------------------------
echo "🛑 Deteniendo procesos anteriores..."
pkill -9 -f "api-gateway.jar" 2>/dev/null || true
pkill -9 -f "ig-service.jar" 2>/dev/null || true
sleep 2
echo ""

# ----------------------------------------
# Iniciar servicios con JARs
# ----------------------------------------
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 Iniciando servicios"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo -e "${BLUE}→ Iniciando API Gateway (puerto 8080)...${NC}"
SPRING_DATASOURCE_URL="jdbc:h2:file:./data/inbox;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE" \
SPRING_DATASOURCE_USERNAME=sa \
SPRING_DATASOURCE_PASSWORD="" \
SPRING_DATASOURCE_DRIVER="org.h2.Driver" \
SPRING_JPA_HIBERNATE_DDL_AUTO=update \
FLYWAY_ENABLED=false \
INSTAGRAM_OAUTH_CLIENT_ID="$INSTAGRAM_CLIENT_ID" \
INSTAGRAM_OAUTH_CLIENT_SECRET="$INSTAGRAM_CLIENT_SECRET" \
INSTAGRAM_OAUTH_REDIRECT_URI="http://localhost:8080/auth/instagram/callback" \
java -jar api-gateway/build/libs/api-gateway.jar \
  > logs/api-gateway.log 2>&1 &
API_GATEWAY_PID=$!
echo "  PID: $API_GATEWAY_PID"
echo "$API_GATEWAY_PID" > logs/api-gateway.pid

sleep 5

echo ""
echo -e "${BLUE}→ Iniciando IG Service (puerto 8081)...${NC}"
SPRING_DATASOURCE_URL="jdbc:h2:file:./data/inbox;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE" \
SPRING_DATASOURCE_USERNAME=sa \
SPRING_DATASOURCE_PASSWORD="" \
SPRING_DATASOURCE_DRIVER="org.h2.Driver" \
SPRING_JPA_HIBERNATE_DDL_AUTO=update \
FLYWAY_ENABLED=false \
INSTAGRAM_VERIFY_TOKEN=demo_token \
java -jar ig-service/build/libs/ig-service.jar \
  > logs/ig-service.log 2>&1 &
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
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Verificando servicios"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Verificar API Gateway
if curl -s http://localhost:8080/health > /dev/null; then
    echo -e "${GREEN}✓${NC} API Gateway: OK (http://localhost:8080)"
else
    echo -e "${RED}⚠️${NC}  API Gateway: No responde"
    echo "   Ver logs: tail -f logs/api-gateway.log"
fi

# Verificar IG Service
if curl -s http://localhost:8081/health > /dev/null; then
    echo -e "${GREEN}✓${NC} IG Service: OK (http://localhost:8081)"
else
    echo -e "${RED}⚠️${NC}  IG Service: No responde"
    echo "   Ver logs: tail -f logs/ig-service.log"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔗 CONECTAR TU CUENTA DE INSTAGRAM"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Abre esta URL en tu navegador:"
echo ""
echo -e "${GREEN}http://localhost:8080/auth/instagram/connect?userId=00000000-0000-0000-0000-000000000001${NC}"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "💡 Detener: ./stop-dev.sh"
echo "📋 Ver logs:"
echo "   • tail -f logs/api-gateway.log"
echo "   • tail -f logs/ig-service.log"
echo ""

