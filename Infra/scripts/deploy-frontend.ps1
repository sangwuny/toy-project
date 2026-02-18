$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$infraRoot = Split-Path -Parent $scriptDir
$repoRoot = Split-Path -Parent $infraRoot
$tfDir = Join-Path $infraRoot "terraform"
$frontendDir = Join-Path $repoRoot "FrontEnd"

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
$bucket = terraform output -raw frontend_bucket_name
$distributionId = terraform output -raw frontend_cloudfront_distribution_id
$apiUrl = terraform output -raw api_url

if (-not $bucket) { throw "Failed to get frontend_bucket_name output." }
if (-not $distributionId) { throw "Failed to get frontend_cloudfront_distribution_id output." }
if (-not $apiUrl) { throw "Failed to get api_url output." }

Set-Location $frontendDir
$env:VITE_API_BASE = $apiUrl
npm.cmd run build

aws s3 sync dist "s3://$bucket" --delete --region $region

if ($distributionId -and $distributionId -ne "None") {
  aws cloudfront create-invalidation --distribution-id $distributionId --paths "/*" | Out-Null
}

Write-Host "Frontend deploy completed."
