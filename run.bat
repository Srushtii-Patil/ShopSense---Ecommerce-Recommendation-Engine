@echo off
setlocal
cd /d "%~dp0"
title ShopSense Recommendation Engine

echo ====================================================================
echo   Starting ShopSense - AI Recommendation Engine & Dynamic Cart
echo ====================================================================

:: Locate Java 17 or default java
set "JAVA_CMD=java"
if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot\bin\java.exe"
)

:: Locate Javac 17 or default javac
set "JAVAC_CMD=javac"
if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot\bin\javac.exe"
)

:: Compile if bin doesn't exist
if not exist "bin\com\shopsense\Main.class" (
    echo [ShopSense] Compiling Java source files...
    if not exist "bin" mkdir "bin"
    powershell -Command "& '%JAVAC_CMD%' -d bin -cp 'lib/*' (Get-ChildItem -Path 'src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })"
)

echo [ShopSense] Launching ShopSense Web Dashboard & REST Server...
start "" "http://localhost:8080"
& "%JAVA_CMD%" -cp "bin;lib/*" com.shopsense.Main --interactive

pause
