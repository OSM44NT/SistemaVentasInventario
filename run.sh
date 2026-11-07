#!/usr/bin/env bash
# ==============================================================================
# Script de ejecución para Linux y macOS
# Inicia la aplicación de escritorio Sistema de Ventas e Inventario
# ==============================================================================

set -euo pipefail

# Colores para mensajes de consola
VERDE='\033[0;32m'
ROJO='\033[0;31m'
AZUL='\033[0;34m'
AMARILLO='\033[1;33m'
RESET='\033[0m'

# Directorio del script (raíz del proyecto)
DIR_RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${DIR_RAIZ}"

echo -e "${AZUL}==============================================================================${RESET}"
echo -e "${AZUL}        SISTEMA DE VENTAS E INVENTARIO - INICIANDO APLICACIÓN                ${RESET}"
echo -e "${AZUL}==============================================================================${RESET}"
echo ""

# 1. Verificar existencia de java
if ! command -v java &> /dev/null; then
    echo -e "${ROJO}[ERROR] No se encontró el comando 'java' en el sistema.${RESET}"
    echo -e "Asegúrate de tener instalado Java JRE o JDK 17 o superior."
    echo -e "Descarga recomendada: https://adoptium.net/"
    exit 1
fi

echo -e "${VERDE}[INFO] Entorno Java detectado:${RESET}"
java -version
echo ""

# 2. Compilar automáticamente si no existe el binario principal
if [ ! -f "bin/sistemaventas/Main.class" ]; then
    echo -e "${AMARILLO}[AVISO] No se encontró la versión compilada. Compilando automáticamente...${RESET}"
    echo ""
    ./build.sh --silent
fi

# 3. Ejecutar la aplicación principal
echo -e "${VERDE}[INFO] Iniciando interfaz gráfica del Sistema de Ventas...${RESET}"
echo ""
java -cp bin sistemaventas.Main "$@"
