variable "project_name" {
  description = "Project name prefix for resource naming."
  type        = string
  default     = "toy"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
  default     = "prod"
}

variable "aws_region" {
  description = "AWS region."
  type        = string
  default     = "ap-northeast-2"
}

variable "vpc_cidr" {
  description = "VPC CIDR block."
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDRs in two AZs."
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "Private app subnet CIDRs in two AZs."
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24"]
}

variable "db_subnet_cidrs" {
  description = "Private DB subnet CIDRs in two AZs."
  type        = list(string)
  default     = ["10.0.21.0/24", "10.0.22.0/24"]
}

variable "domain_name" {
  description = "Root domain (example.com). If empty, Route53 records are not created."
  type        = string
  default     = ""
}

variable "frontend_subdomain" {
  description = "Frontend subdomain label. Use empty string for apex."
  type        = string
  default     = ""
}

variable "api_subdomain" {
  description = "API subdomain label."
  type        = string
  default     = "api"
}

variable "route53_zone_id" {
  description = "Hosted zone ID for Route53. Required when domain_name is set."
  type        = string
  default     = ""
}

variable "alb_certificate_arn" {
  description = "ACM certificate ARN for ALB in the same region."
  type        = string
  default     = ""
}

variable "cloudfront_certificate_arn" {
  description = "ACM certificate ARN for CloudFront in us-east-1."
  type        = string
  default     = ""
}

variable "backend_container_port" {
  description = "Backend container port."
  type        = number
  default     = 8080
}

variable "backend_image_tag" {
  description = "Backend image tag in ECR."
  type        = string
  default     = "latest"
}

variable "backend_cpu" {
  description = "Fargate CPU units."
  type        = number
  default     = 512
}

variable "backend_memory" {
  description = "Fargate memory (MiB)."
  type        = number
  default     = 1024
}

variable "backend_desired_count" {
  description = "Desired ECS task count."
  type        = number
  default     = 2
}

variable "db_name" {
  description = "RDS database name."
  type        = string
  default     = "toy_project"
}

variable "db_username" {
  description = "RDS master username."
  type        = string
  default     = "toyadmin"
}

variable "db_instance_class" {
  description = "RDS instance class."
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB."
  type        = number
  default     = 20
}

variable "jwt_secret" {
  description = "JWT secret for backend."
  type        = string
  sensitive   = true
}

variable "frontend_bucket_force_destroy" {
  description = "Allow deleting non-empty frontend S3 bucket."
  type        = bool
  default     = false
}
