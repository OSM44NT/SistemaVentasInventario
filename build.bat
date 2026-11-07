@echo off
rem ==============================================================================
rem Script de compilacion para Windows (CMD / PowerShell)
rem Compila todos los archivos fuente Java con codificacion UTF-8 hacia 'bin'
rem ==============================================================================

chcp 65001 >nul
title Compilando Sistema de Ventas e Inventario

echo ==============================================================================
echo        SISTEMA DE VENTAS E INVENTARIO - COMPILACION DEL PROYECTO
echo ==============================================================================
echo.

rem 1. Verificar si javac esta en el PATH o en JAVA_HOME
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\javac.exe" (
            set "PATH=%JAVA_HOME%\bin;%PATH%"
            goto :javac_encontrado
        )
    )
    echo [ERROR] No se encontro el compilador de Java (javac).
    echo.
    echo Por favor asegurate de:
    echo  1. Tener instalado Java JDK 17 o superior (JDK 17 o 21 recomendado).
    echo  2. Configurar la variable de entorno JAVA_HOME o agregar la carpeta 'bin'
    echo     del JDK a la variable PATH del sistema.
    echo.
    echo Descarga de JDK: https://adoptium.net/ o https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)

:javac_encontrado
echo [INFO] Compilador javac detectado:
javac -version
echo.

rem 2. Crear la carpeta de salida bin si no existe
if not exist "bin" (
    echo [INFO] Creando directorio de salida 'bin'...
    mkdir "bin"
)

rem 3. Compilar fuentes Java ubicados en src\sistemaventas
echo [INFO] Compilando fuentes desde 'src\sistemaventas\*.java'...
javac -encoding UTF-8 -d bin src\sistemaventas\*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo ==============================================================================
    echo [OK] Compilacion completada exitosamente en la carpeta 'bin'.
    echo ==============================================================================
    echo.
    if not "%1"=="--silent" (
        echo Puedes iniciar el sistema ejecutando 'run.bat'.
        echo.
    )
    exit /b 0
) else (
    echo.
    echo ==============================================================================
    echo [ERROR] Se presentaron errores durante la compilacion.
    echo ==============================================================================
    echo.
    pause
    exit /b %ERRORLEVEL%
)
