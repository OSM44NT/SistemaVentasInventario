@echo off
rem ==============================================================================
rem Script de ejecucion de pruebas automatizadas para Windows (CMD / PowerShell)
rem Compila fuentes, compila pruebas y ejecuta las validaciones del sistema
rem ==============================================================================

chcp 65001 >nul
title Pruebas Automatizadas - Sistema de Ventas e Inventario

echo ==============================================================================
echo        SISTEMA DE VENTAS E INVENTARIO - PRUEBAS AUTOMATIZADAS
echo ==============================================================================
echo.

rem 1. Verificar JDK
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\javac.exe" (
            set "PATH=%JAVA_HOME%\bin;%PATH%"
            goto :jdk_listo
        )
    )
    echo [ERROR] No se encontro el compilador de Java (javac).
    echo Se requiere JDK 17 o superior para compilar y ejecutar pruebas.
    pause
    exit /b 1
)

:jdk_listo
if not exist "bin" mkdir "bin"

rem 2. Compilar fuentes principales
echo [INFO] Compilando fuentes principales hacia 'bin'...
javac -encoding UTF-8 -d bin src\sistemaventas\*.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Fallo en la compilacion de fuentes principales.
    pause
    exit /b %ERRORLEVEL%
)

rem 3. Compilar suite de pruebas
echo [INFO] Compilando suite de pruebas 'test\PruebasSistema.java'...
javac -encoding UTF-8 -cp bin -d bin test\PruebasSistema.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Fallo en la compilacion de la suite de pruebas.
    pause
    exit /b %ERRORLEVEL%
)

rem 4. Ejecutar suite de pruebas
echo.
echo [INFO] Ejecutando bateria de pruebas unitarias y de integracion...
echo.
java -cp bin PruebasSistema %*
set "TEST_RESULT=%ERRORLEVEL%"

echo.
if %TEST_RESULT% equ 0 (
    echo ==============================================================================
    echo [OK] TODAS LAS PRUEBAS PASARON EXITOSAMENTE AL 100%%
    echo ==============================================================================
) else (
    echo ==============================================================================
    echo [FALLO] Se presentaron errores en las pruebas (Codigo: %TEST_RESULT%)
    echo ==============================================================================
)
echo.
if not "%1"=="--silent" pause
exit /b %TEST_RESULT%
