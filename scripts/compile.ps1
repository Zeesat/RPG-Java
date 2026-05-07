$projectRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $projectRoot "src"
$outputRoot = Join-Path $projectRoot "out"

if (Test-Path $outputRoot) {
    Remove-Item -Recurse -Force $outputRoot
}

New-Item -ItemType Directory -Path $outputRoot | Out-Null

$sources = Get-ChildItem -Path $sourceRoot -Recurse -Filter *.java | ForEach-Object { $_.FullName }

if (-not $sources) {
    Write-Host "Tidak ada file Java di folder src."
    exit 1
}

javac -d $outputRoot $sources

