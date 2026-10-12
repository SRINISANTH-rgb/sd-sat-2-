@echo off
TITLE Java Servlet User Management Prototype Runner
cls
echo ==================================================================
echo    JAVA SERVLET USER MANAGEMENT SYSTEM - EMBEDDED RUNNER
echo ==================================================================
echo.

if not exist "lib\javax.servlet-api-4.0.1.jar" (
    echo [INFO] Downloading javax.servlet-api dependency...
    powershell -Command "if (!(Test-Path -Path 'lib')) { New-Item -ItemType Directory -Path 'lib' }; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar' -OutFile 'lib/javax.servlet-api-4.0.1.jar'"
)

if not exist "bin" (
    mkdir bin
)

echo [BUILD] Compiling Java Servlet Source Files...
javac -encoding UTF-8 -cp "lib/*" -d bin src/main/java/com/usermanagement/model/*.java src/main/java/com/usermanagement/dao/*.java src/main/java/com/usermanagement/servlet/*.java src/main/java/com/usermanagement/MainApp.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed! Please check Java installation.
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Compilation complete. Launching server...
echo.
java -cp "lib/*;bin" com.usermanagement.MainApp
pause
