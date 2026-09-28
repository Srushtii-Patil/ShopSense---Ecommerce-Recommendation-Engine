@echo off
setlocal
cd /d "%~dp0"
title Compiling ShopSense

set "JAVAC_CMD=javac"
if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot\bin\javac.exe"
)

echo [ShopSense] Compiling Java classes to bin/...
if not exist "bin" mkdir "bin"
powershell -Command "& '%JAVAC_CMD%' -d bin -cp 'lib/*' (Get-ChildItem -Path 'src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })"

if %ERRORLEVEL% EQU 0 (
    echo [ShopSense] Compilation SUCCESS! Ready to run.
) else (
    echo [ShopSense] Compilation FAILED.
)
pause
