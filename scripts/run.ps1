$projectRoot = Split-Path -Parent $PSScriptRoot
$outputRoot = Join-Path $projectRoot "out"

if (-not (Test-Path $outputRoot)) {
    Write-Host "Folder out belum ada. Jalankan compile.ps1 terlebih dahulu."
    exit 1
}

java -cp $outputRoot fantasyrpg.Main

