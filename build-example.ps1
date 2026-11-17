param([Parameter(Mandatory)][string]$JavaHome, [Parameter(Mandatory)][string]$ApiJar)
$ErrorActionPreference = 'Stop'
$exampleRoot = Join-Path $PSScriptRoot 'example'
$buildRoot = Join-Path $PSScriptRoot 'example-build'
New-Item -ItemType Directory -Force $buildRoot | Out-Null
$sources = @(Get-ChildItem -LiteralPath $exampleRoot -Recurse -Filter '*.java' | ForEach-Object FullName)
& (Join-Path $JavaHome 'bin/javac.exe') -proc:none --release 21 -encoding UTF-8 -cp $ApiJar -d $buildRoot @sources
if ($LASTEXITCODE) { throw 'Example compilation failed.' }
Copy-Item -LiteralPath (Join-Path $exampleRoot 'fabric.mod.json') -Destination $buildRoot
$outputJar = Join-Path $PSScriptRoot 'krypton-example-addon-1.0.0.jar'
& (Join-Path $JavaHome 'bin/jar.exe') --create --file $outputJar -C $buildRoot .
if ($LASTEXITCODE) { throw 'Example packaging failed.' }
Write-Output $outputJar
