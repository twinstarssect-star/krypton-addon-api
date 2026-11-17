param([Parameter(Mandatory)][string]$JavaHome, [Parameter(Mandatory)][string]$HostJar)
$ErrorActionPreference = 'Stop'
$testRoot = Join-Path $PSScriptRoot 'api-test-build'
New-Item -ItemType Directory -Force $testRoot | Out-Null
& (Join-Path $JavaHome 'bin/javac.exe') -proc:none --release 21 -cp $HostJar -d $testRoot (Join-Path $PSScriptRoot 'tests/test/ApiChecks.java')
if ($LASTEXITCODE) { throw 'API test compilation failed.' }
$statePath = Join-Path $testRoot 'test-state.properties'
& (Join-Path $JavaHome 'bin/java.exe') -cp ($HostJar + [IO.Path]::PathSeparator + $testRoot) test.ApiChecks $statePath
if ($LASTEXITCODE) { throw 'API tests failed.' }
