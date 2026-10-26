param(
    [switch]$Cli
)

Set-Location $PSScriptRoot

$javaVersion = (& java --version | Select-Object -First 1)
if ($LASTEXITCODE -ne 0) {
    throw "Java 25 or newer is required."
}
$javaMajorMatch = [regex]::Match($javaVersion, '\b(?:openjdk|java)\s+(\d+)')
if (!$javaMajorMatch.Success -or [int]$javaMajorMatch.Groups[1].Value -lt 25) {
    throw "Java 25 or newer is required. Found: $javaVersion"
}

if (!(Test-Path 'lib')) { New-Item -ItemType Directory -Path 'lib' }
if (!(Test-Path 'lib/sqlite-jdbc.jar')) {
    Write-Host "Downloading SQLite JDBC Driver..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar" -OutFile "lib/sqlite-jdbc.jar"
}
Write-Host "Compiling Java Files..." -ForegroundColor Yellow
javac --release 25 -d bin -cp "lib/*" (Get-ChildItem -Path src/main/java -Filter *.java -Recurse).FullName
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed."
}
Write-Host "Starting Banking Engine..." -ForegroundColor Green
if ($Cli) {
    java -cp "bin;lib/*" com.bank.Main
} else {
    java -cp "bin;lib/*" com.bank.Main --web
}
