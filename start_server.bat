@echo off
title CarrerX - Intelligent Job Matching & Recommendation System
color 0A

echo =======================================================================
echo     CarrerX - Intelligent Job Matching & Recommendation System
echo =======================================================================
echo.

:: Check Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java was not found in your system PATH.
    echo Please ensure Java 11 or higher is installed.
    pause
    exit /b 1
)

:: Compile if class files don't exist or if forced
if not exist "JobMatcherServer.class" (
    echo [1/3] Compiling Java Backend Server with ecj.jar...
    java -jar ecj.jar -source 17 -target 17 JobMatcherServer.java
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Compilation failed.
        pause
        exit /b 1
    )
    echo [OK] Compiled successfully.
) else (
    echo [1/3] Backend classes found and ready.
)

echo [2/3] Starting Java HTTP Server on http://localhost:8080 ...
start "" "http://localhost:8080"

echo [3/3] Opening application in your browser...
echo.
echo =======================================================================
echo  Server is ACTIVE at: http://localhost:8080
echo  Press Ctrl+C at any time to stop the server.
echo =======================================================================
echo.

java JobMatcherServer

pause
