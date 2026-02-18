project_name = "toy"
environment  = "prod"
aws_region   = "ap-northeast-2"

# Domain and certs
domain_name                = "sangwonlabs.com"
frontend_subdomain         = ""
api_subdomain              = "api"
route53_zone_id            = "Z08940682SRB0FCFI3KLD"
alb_certificate_arn        = "arn:aws:acm:ap-northeast-2:693104801861:certificate/1a35e096-4ff4-4d49-935f-6710d6b22451"
cloudfront_certificate_arn = "arn:aws:acm:us-east-1:693104801861:certificate/d47bd741-fa1f-4739-bc99-74ad808ed7ae"

# Backend
backend_image_tag   = "latest"
backend_desired_count = 2

# Database
db_name     = "toy_project"
db_username = "toyadmin"

# Required secret value
jwt_secret = "Qm7vR3nP8xK2tL9sH4dF1zB6wC0yN5uJqA2mE8rT"
