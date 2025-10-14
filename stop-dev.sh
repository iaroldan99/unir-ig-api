#!/bin/bash

# ============================================
# Script para detener servicios de desarrollo
# ============================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🛑 Deteniendo servicios"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Detener servicios Java
if [ -f logs/api-gateway.pid ]; then
    API_GATEWAY_PID=$(cat logs/api-gateway.pid)
    echo -e "${YELLOW}→${NC} Deteniendo API Gateway (PID: $API_GATEWAY_PID)..."
    kill $API_GATEWAY_PID 2>/dev/null || echo "  Ya estaba detenido"
    rm logs/api-gateway.pid
fi

if [ -f logs/ig-service.pid ]; then
    IG_SERVICE_PID=$(cat logs/ig-service.pid)
    echo -e "${YELLOW}→${NC} Deteniendo IG Service (PID: $IG_SERVICE_PID)..."
    kill $IG_SERVICE_PID 2>/dev/null || echo "  Ya estaba detenido"
    rm logs/ig-service.pid
fi

# Detener cualquier proceso Gradle que pueda estar corriendo
echo -e "${YELLOW}→${NC} Limpiando procesos Gradle..."
pkill -f "gradle.*bootRun" 2>/dev/null || true

# Detener Docker Compose
echo -e "${YELLOW}→${NC} Deteniendo PostgreSQL..."
docker-compose down

echo ""
echo -e "${GREEN}✓${NC} Todos los servicios detenidos"
echo ""

