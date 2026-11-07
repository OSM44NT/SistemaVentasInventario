#!/usr/bin/env bash
# ==============================================================================
# Script de compilación para Linux y macOS
# Compila todos los fuentes Java con codificación UTF-8 hacia 'bin'
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
echo -e "${AZUL}       SISTEMA DE VENTAS E INVENTARIO - COMPILACIÓN DEL PROYECTO              ${RESET}"
echo -e "${AZUL}==============================================================================${RESET}"
echo ""

# 1. Verificar existencia de javac
if ! command -v javac &> /dev/null; then
    echo -e "${ROJO}[ERROR] No se encontró el compilador de Java (javac).${RESET}"
    echo -e "Asegúrate de tener instalado Java JDK 17 o 21."
    echo -e "Descarga recomendada: https://adoptium.net/"
    exit 1
fi

echo -e "${VERDE}[INFO] Compilador javac detectado:${RESET}"
javac -version
echo ""

# 2. Crear directorio de salida bin
mkdir -p bin

# 3. Compilar fuentes Java
echo -e "${AZUL}[INFO] Compilando fuentes desde 'src/sistemaventas/*.java'...${RESET}"
javac -encoding UTF-8 -d bin src/sistemaventas/*.java

echo ""
echo -e "${VERDE}==============================================================================${RESET}"
echo -e "${VERDE}[OK] Compilación completada exitosamente en la carpeta 'bin'.${RESET}"
echo -e "${VERDE}==============================================================================${RESET}"
echo ""
if [ "${1:-}" != "--silent" ]; then
    echo -e "Puedes iniciar el sistema ejecutando '${AMARILLO}./run.sh${RESET}'."
    echo ""
fi
