$projectRoot = Split-Path -Parent $PSScriptRoot
$outputRoot = Join-Path $projectRoot "out"

if (-not (Test-Path $outputRoot)) {
    Write-Host "Folder out belum ada. Jalankan compile.ps1 terlebih dahulu."
    exit 1
}

$javaCommand = Get-Command java -ErrorAction SilentlyContinue
$localJava = Join-Path $env:USERPROFILE ".jdks\openjdk-25.0.2\bin\java.exe"

if ($javaCommand) {
    & $javaCommand.Source -cp $outputRoot fantasyrpg.Main
} elseif (Test-Path $localJava) {
    & $localJava -cp $outputRoot fantasyrpg.Main
} else {
    Write-Host "java tidak ditemukan. Install JDK atau tambahkan java ke PATH."
    exit 1
}

