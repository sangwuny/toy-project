$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$infraRoot = Split-Path -Parent $scriptDir
$repoRoot = Split-Path -Parent $infraRoot
$tfDir = Join-Path $infraRoot "terraform"
$backendDir = Join-Path $repoRoot "BackEnd"

function Get-TfvarValue([string]$Path, [string]$Key, [string]$Default = "") {
  if (-not (Test-Path $Path)) { return $Default }
  $line = Get-Content $Path | Where-Object { $_ -match "^\s*$Key\s*=" } | Select-Object -First 1
  if (-not $line) { return $Default }
  $value = ($line -split "=", 2)[1].Trim().Trim('"')
  if (-not $value) { return $Default }
  return $value
}

Set-Location $tfDir
$tfvarsPath = Join-Path $tfDir "terraform.tfvars"
$region = Get-TfvarValue -Path $tfvarsPath -Key "aws_region" -Default "ap-northeast-2"

$repoUrl = terraform output -raw ecr_backend_repository_url
$cluster = terraform output -raw ecs_cluster_name
$service = terraform output -raw ecs_backend_service_name
if (-not $repoUrl) { throw "Failed to get ecr_backend_repository_url output." }
if (-not $cluster) { throw "Failed to get ecs_cluster_name output." }
if (-not $service) { throw "Failed to get ecs_backend_service_name output." }

Set-Location $backendDir

aws ecr get-login-password --region $region | docker login --username AWS --password-stdin ($repoUrl -replace '/.*$','')
docker build -t "${repoUrl}:latest" .
docker push "${repoUrl}:latest"

aws ecs update-service `
  --region $region `
  --cluster $cluster `
  --service $service `
  --force-new-deployment | Out-Null

Write-Host "Backend deploy completed."
