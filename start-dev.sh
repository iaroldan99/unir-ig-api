#!/bin/bash

# ============================================
# Script de inicio para desarrollo local
# ============================================

set -e  # Exit on error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 UNIR IG API - Inicio de Desarrollo"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# ----------------------------------------
# Verificar credenciales
# ----------------------------------------
if [ -z "$INSTAGRAM_CLIENT_ID" ]; then
    echo -e "${RED}⚠️  INSTAGRAM_CLIENT_ID no configurado${NC}"
    echo ""
    echo "Por favor configura las variables de entorno:"
    echo ""
    echo "  export INSTAGRAM_CLIENT_ID=\"tu_app_id\""
    echo "  export INSTAGRAM_CLIENT_SECRET=\"tu_app_secret\""
    echo ""
    echo "O ejecútalo así:"
    echo ""
    echo "  INSTAGRAM_CLIENT_ID=xxx INSTAGRAM_CLIENT_SECRET=yyy ./start-dev.sh"
    echo ""
    exit 1
fi

echo -e "${GREEN}✓${NC} Credenciales configuradas"
echo "  App ID: ${INSTAGRAM_CLIENT_ID}"
echo ""

# ----------------------------------------
# Paso 1: Levantar base de datos
# ----------------------------------------
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 PASO 1: Iniciando PostgreSQL"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

docker-compose up -d db

echo ""
echo -e "${YELLOW}⏳ Esperando a que PostgreSQL esté listo...${NC}"
sleep 5

# Verificar que postgres está listo
until docker-compose exec -T db pg_isready -U postgres &>/dev/null; do
    echo "   Esperando..."
    sleep 2
done

echo -e "${GREEN}✓${NC} PostgreSQL está listo"
echo ""

# ----------------------------------------
# Paso 2: Compilar proyectos
# ----------------------------------------
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔨 PASO 2: Compilando proyectos"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

./gradlew clean build -x test

echo ""
echo -e "${GREEN}✓${NC} Compilación exitosa"
echo ""

# ----------------------------------------
# Paso 3: Iniciar servicios
# ----------------------------------------
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 PASO 3: Iniciando servicios"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Crear directorio para logs
mkdir -p logs

echo -e "${BLUE}→ Iniciando API Gateway (puerto 8080)...${NC}"
INSTAGRAM_CLIENT_ID="$INSTAGRAM_CLIENT_ID" \
INSTAGRAM_CLIENT_SECRET="$INSTAGRAM_CLIENT_SECRET" \
INSTAGRAM_REDIRECT_URI="http://localhost:8080/auth/instagram/callback" \
./gradlew :api-gateway:bootRun > logs/api-gateway.log 2>&1 &
API_GATEWAY_PID=$!
echo "  PID: $API_GATEWAY_PID"

echo ""
echo -e "${BLUE}→ Iniciando IG Service (puerto 8081)...${NC}"
IG_VERIFY_TOKEN="demo_token" \
./gradlew :ig-service:bootRun > logs/ig-service.log 2>&1 &
IG_SERVICE_PID=$!
echo "  PID: $IG_SERVICE_PID"

# Guardar PIDs
echo "$API_GATEWAY_PID" > logs/api-gateway.pid
echo "$IG_SERVICE_PID" > logs/ig-service.pid

echo ""
echo -e "${YELLOW}⏳ Esperando a que los servicios inicien...${NC}"
echo "   (Esto puede tomar 30-60 segundos)"
echo ""

# Esperar a que los servicios estén listos
attempt=0
max_attempts=60

check_service() {
    local port=$1
    local name=$2
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s http://localhost:$port/actuator/health &>/dev/null; then
            echo -e "${GREEN}✓${NC} $name está listo"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 2
    done
    
    echo -e "${RED}✗${NC} $name no respondió a tiempo"
    return 1
}

# Intentar verificar salud de los servicios
# (Si no tienen actuator, solo esperamos un tiempo fijo)
sleep 15

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
echo "   • PostgreSQL:   localhost:5432"
echo ""
echo "📄 Logs en:"
echo "   • logs/api-gateway.log"
echo "   • logs/ig-service.log"
echo ""
echo "🔗 Para conectar tu cuenta de Instagram:"
echo ""
echo "   Abre en tu navegador:"
echo -e "   ${BLUE}http://localhost:8080/auth/instagram/connect?userId=00000000-0000-0000-0000-000000000001${NC}"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "💡 Tips:"
echo "   • Ver logs: tail -f logs/api-gateway.log"
echo "   • Detener:  ./stop-dev.sh"
echo "   • Estado:   docker-compose ps"
echo ""
echo "⏳ Presiona Ctrl+C para ver los logs en tiempo real..."
echo ""

# Seguir los logs
tail -f logs/api-gateway.log

