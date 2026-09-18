$ErrorActionPreference = "Stop"

if (Test-Path "out") {
    Remove-Item -Recurse -Force "out"
}

New-Item -ItemType Directory -Force "out" | Out-Null

$sources = Get-ChildItem -Recurse -Filter *.java "src/main/java" |
    ForEach-Object { $_.FullName }

if (-not $sources) {
    throw "No Java files were found."
}

javac --release 17 -encoding UTF-8 -d out $sources
$javacExitCode = $LASTEXITCODE

if ($javacExitCode -ne 0) {
    throw "Compilation failed with exit code $javacExitCode."
}

Write-Host ""
Write-Host "Compilation successful."