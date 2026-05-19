$projectRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $projectRoot "src"
$outputRoot = Join-Path $projectRoot "out"

if (Test-Path $outputRoot) {
    Remove-Item -Recurse -Force $outputRoot
}

New-Item -ItemType Directory -Path $outputRoot | Out-Null

$sources = Get-ChildItem -Path $sourceRoot -Recurse -Filter *.java | ForEach-Object { $_.FullName }
$javacCommand = Get-Command javac -ErrorAction SilentlyContinue
$localJavac = Join-Path $env:USERPROFILE ".jdks\openjdk-25.0.2\bin\javac.exe"

if (-not $sources) {
    Write-Host "Tidak ada file Java di folder src."
    exit 1
}

if ($javacCommand) {
    & $javacCommand.Source -d $outputRoot $sources
} elseif (Test-Path $localJavac) {
    & $localJavac -d $outputRoot $sources
} else {
    Write-Host "javac tidak ditemukan. Install JDK atau tambahkan javac ke PATH."
    exit 1
}

$resources = Get-ChildItem -Path $sourceRoot -Recurse -File | Where-Object { $_.Extension -ne ".java" }

foreach ($resource in $resources) {
    $relativePath = $resource.FullName.Substring($sourceRoot.Length).TrimStart("\", "/")
    $targetPath = Join-Path $outputRoot $relativePath
    $targetDirectory = Split-Path -Parent $targetPath

    if (-not (Test-Path $targetDirectory)) {
        New-Item -ItemType Directory -Path $targetDirectory | Out-Null
    }

    Copy-Item -Path $resource.FullName -Destination $targetPath -Force
}

