output "ecr_backend_repository_url" {
  value       = aws_ecr_repository.backend.repository_url
  description = "ECR repository URL for backend container image."
}

output "backend_alb_dns_name" {
  value       = aws_lb.api.dns_name
  description = "ALB DNS for backend API."
}

output "frontend_bucket_name" {
  value       = aws_s3_bucket.frontend.bucket
  description = "S3 bucket name for frontend static files."
}

output "frontend_cloudfront_domain" {
  value       = aws_cloudfront_distribution.frontend.domain_name
  description = "CloudFront domain for frontend."
}

output "db_endpoint" {
  value       = aws_db_instance.main.address
  description = "RDS endpoint address."
}

output "app_secret_arn" {
  value       = aws_secretsmanager_secret.app.arn
  description = "Secrets Manager ARN containing app secrets."
}

output "ecs_cluster_name" {
  value       = aws_ecs_cluster.main.name
  description = "ECS cluster name."
}

output "ecs_backend_service_name" {
  value       = aws_ecs_service.backend.name
  description = "ECS backend service name."
}

output "frontend_cloudfront_distribution_id" {
  value       = aws_cloudfront_distribution.frontend.id
  description = "CloudFront distribution ID."
}

output "frontend_url" {
  value       = var.domain_name == "" ? "https://${aws_cloudfront_distribution.frontend.domain_name}" : "https://${local.frontend_fqdn}"
  description = "Frontend URL."
}

output "api_url" {
  value       = var.domain_name == "" ? "http://${aws_lb.api.dns_name}" : "https://${local.api_fqdn}"
  description = "API base URL."
}
