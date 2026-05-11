Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
$BackendDir = Join-Path $RepoRoot "backend"

$env:GOCACHE = Join-Path $RepoRoot ".gocache"
$env:GOMODCACHE = Join-Path $RepoRoot ".gomodcache"

New-Item -ItemType Directory -Force -Path $env:GOCACHE | Out-Null
New-Item -ItemType Directory -Force -Path $env:GOMODCACHE | Out-Null

Push-Location $BackendDir
try {
    go test ./...
}
finally {
    Pop-Location
}
