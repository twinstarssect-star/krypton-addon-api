param([Parameter(Mandatory)][string]$JavaHome)
$ErrorActionPreference = 'Stop'
$sourceRoot = Join-Path $PSScriptRoot 'src/krypton/addons/api'
$buildRoot = Join-Path $PSScriptRoot 'sdk-build'
New-Item -ItemType Directory -Force $buildRoot | Out-Null
$sources = @(Get-ChildItem -LiteralPath $sourceRoot -Filter '*.java' | ForEach-Object FullName)
& (Join-Path $JavaHome 'bin/javac.exe') -proc:none --release 21 -encoding UTF-8 -d $buildRoot @sources
if ($LASTEXITCODE) { throw 'SDK compilation failed.' }
$outputJar = Join-Path $PSScriptRoot 'krypton-addon-api-1.0.0.jar'
& (Join-Path $JavaHome 'bin/jar.exe') --create --file $outputJar -C $buildRoot .
if ($LASTEXITCODE) { throw 'SDK packaging failed.' }
Write-Output $outputJar
