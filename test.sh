#!/usr/bin/env bash
# ==============================================================================
# Script de ejecución de pruebas automatizadas para Linux y macOS
# Compila fuentes, compila suite de pruebas y ejecuta validaciones al 100%
# ==============================================================================

set -euo pipefail

VERDE='\033[0;32m'
ROJO='\033[0;31m'
AZUL='\033[0;34m'
AMARILLO='\033[1;33m'
RESET='\033[0m'

DIR_RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${DIR_RAIZ}"

echo -e "${AZUL}==============================================================================${RESET}"
echo -e "${AZUL}        SISTEMA DE VENTAS E INVENTARIO - PRUEBAS AUTOMATIZADAS                ${RESET}"
echo -e "${AZUL}==============================================================================${RESET}"
echo ""

# 1. Verificar herramientas JDK
if ! command -v javac &> /dev/null || ! command -v java &> /dev/null; then
    echo -e "${ROJO}[ERROR] Se requiere Java JDK 17 o superior instalado y disponible en PATH.${RESET}"
    exit 1
fi

# 2. Compilar fuentes del proyecto
mkdir -p bin
echo -e "${AZUL}[INFO] Compilando fuentes principales hacia 'bin'...${RESET}"
javac -encoding UTF-8 -d bin src/sistemaventas/*.java

# 3. Compilar suite de pruebas
echo -e "${AZUL}[INFO] Compilando suite de pruebas 'test/PruebasSistema.java'...${RESET}"
javac -encoding UTF-8 -cp bin -d bin test/PruebasSistema.java

# 4. Ejecutar suite de pruebas
echo ""
echo -e "${AZUL}[INFO] Ejecutando batería de pruebas unitarias y de integración...${RESET}"
echo ""
java -cp bin PruebasSistema "$@"

CODIGO_SALIDA=$?
echo ""
if [ ${CODIGO_SALIDA} -eq 0 ]; then
    echo -e "${VERDE}==============================================================================${RESET}"
    echo -e "${VERDE}[OK] TODAS LAS PRUEBAS PASARON EXITOSAMENTE AL 100%${RESET}"
    echo -e "${VERDE}==============================================================================${RESET}"
else
    echo -e "${ROJO}==============================================================================${RESET}"
    echo -e "${ROJO}[FALLO] Se presentaron fallos en la suite de pruebas (Código: ${CODIGO_SALIDA})${RESET}"
    echo -e "${ROJO}==============================================================================${RESET}"
fi

exit ${CODIGO_SALIDA}
