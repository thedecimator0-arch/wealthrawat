@echo off
for /f "tokens=2 delims= " %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VERSION=%%~v"
for /f "tokens=1 delims=." %%m in ("%JAVA_VERSION%") do set "JAVA_MAJOR=%%m"
if not defined JAVA_MAJOR (
    echo Java 25 or newer is required.
    exit /b 1
)
if %JAVA_MAJOR% LSS 25 (
    echo Java 25 or newer is required. Found Java %JAVA_MAJOR%.
    exit /b 1
)
echo Downloading SQLite JDBC Driver...
powershell -Command "if (!(Test-Path 'lib')) { New-Item -ItemType Directory -Path 'lib' }; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar' -OutFile 'lib/sqlite-jdbc.jar'"
echo Compiling Java code...
javac --release 25 -d bin -cp "lib/*" src/main/java/com/bank/*.java src/main/java/com/bank/*/*.java
if errorlevel 1 exit /b 1
echo Running Application...
if /I "%1"=="web" (
    java -cp "bin;lib/*" com.bank.Main --web
) else (
    java -cp "bin;lib/*" com.bank.Main
)
pause
