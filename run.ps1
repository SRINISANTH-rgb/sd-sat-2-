# PowerShell Launcher Script for Java Servlet User Management Prototype

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "   JAVA SERVLET USER MANAGEMENT SYSTEM - EMBEDDED RUNNER" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

# Ensure lib folder and servlet-api jar
if (-not (Test-Path -Path "lib\javax.servlet-api-4.0.1.jar")) {
    Write-Host "[INFO] Downloading javax.servlet-api dependency..." -ForegroundColor Yellow
    if (-not (Test-Path -Path "lib")) { New-Item -ItemType Directory -Path "lib" | Out-Null }
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar" -OutFile "lib/javax.servlet-api-4.0.1.jar"
}

# Ensure bin directory
if (-not (Test-Path -Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[BUILD] Compiling Java Servlet Source Files..." -ForegroundColor Green
javac -encoding UTF-8 -cp "lib/*" -d bin src/main/java/com/usermanagement/model/*.java src/main/java/com/usermanagement/dao/*.java src/main/java/com/usermanagement/servlet/*.java src/main/java/com/usermanagement/MainApp.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed! Check Java setup." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[SUCCESS] Compilation complete. Launching server..." -ForegroundColor Green
Write-Host ""

java -cp "lib/*;bin" com.usermanagement.MainApp
