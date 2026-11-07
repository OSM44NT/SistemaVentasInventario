@echo off
rem ==============================================================================
rem Script de ejecucion para Windows (CMD / PowerShell / Doble Clic)
rem Inicia la aplicacion de escritorio Sistema de Ventas e Inventario
rem ==============================================================================

chcp 65001 >nul
title Sistema de Ventas e Inventario

echo ==============================================================================
echo        SISTEMA DE VENTAS E INVENTARIO - INICIANDO APLICACION
echo ==============================================================================
echo.

rem 1. Verificar si java esta disponible en PATH o JAVA_HOME
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\java.exe" (
            set "PATH=%JAVA_HOME%\bin;%PATH%"
            goto :java_encontrado
        )
    )
    echo [ERROR] No se encontro el comando 'java' en el sistema.
    echo.
    echo Por favor asegurate de tener instalado Java JRE o JDK 17 o superior.
    echo Descarga: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

:java_encontrado
echo [INFO] Entorno Java detectado:
java -version
echo.

rem 2. Verificar si el proyecto ya fue compilado
if not exist "bin\sistemaventas\Main.class" (
    echo [AVISO] El proyecto no esta compilado aun. Compilando automaticamente...
    echo.
    call build.bat --silent
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] No se pudo compilar el proyecto. Revisa los errores anteriores.
        echo.
        pause
        exit /b %ERRORLEVEL%
    )
)

rem 3. Ejecutar la aplicacion principal
echo [INFO] Iniciando interfaz grafica del Sistema de Ventas...
echo.
java -cp bin sistemaventas.Main %*

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] La aplicacion finalizo con codigo de error %ERRORLEVEL%.
    pause
)
