param(
    [string]$Task = "testDebugUnitTest"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
$AndroidDir = Join-Path $RepoRoot "android-app"

$env:GRADLE_USER_HOME = Join-Path $RepoRoot ".gradle-user"
New-Item -ItemType Directory -Force -Path $env:GRADLE_USER_HOME | Out-Null

Push-Location $AndroidDir
try {
    & ".\gradlew.bat" $Task --no-daemon "-Dkotlin.compiler.execution.strategy=in-process"
}
finally {
    Pop-Location
}
