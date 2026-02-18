# Infra (AWS: ECS Fargate + ALB + RDS + S3/CloudFront)

이 디렉터리는 아래 AWS 아키텍처를 Terraform으로 생성합니다.

- FrontEnd: `S3 + CloudFront`
- BackEnd: `ECR + ECS Fargate + ALB`
- Database: `RDS MySQL`
- Network: `VPC (public/private/db subnet, NAT)`
- Secret: `Secrets Manager`

## 1) 사전 준비

- AWS CLI 로그인
- Terraform 1.6+
- Docker
- Route53 Hosted Zone / ACM 인증서 (HTTPS 사용 시)

## 2) Terraform 적용

```powershell
cd Infra/terraform
Copy-Item terraform.tfvars.example terraform.tfvars
# terraform.tfvars 값 수정
terraform init
terraform plan
terraform apply
```

## 3) BackEnd 이미지 배포

```powershell
cd ../scripts
./deploy-backend.ps1
```

스크립트 동작:
- ECR 로그인
- `BackEnd/Dockerfile` 빌드
- ECR push
- ECS Service 강제 새 배포

## 4) FrontEnd 정적 파일 배포

```powershell
cd ../scripts
./deploy-frontend.ps1
```

스크립트 동작:
- `FrontEnd` 빌드 (`VITE_API_BASE` 자동 주입)
- S3 sync
- CloudFront invalidation

## 5) 필수 설정 확인

- BackEnd 환경변수
  - `SPRING_PROFILES_ACTIVE=prod`
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME/PASSWORD` (Secrets Manager)
  - `APP_JWT_SECRET` (Secrets Manager)
  - `APP_CORS_ALLOWED_ORIGINS=https://<frontend-domain>`
  - `APP_AUTH_REFRESH_COOKIE_SECURE=true`
- Health check
  - ALB target group path: `/actuator/health`

## 6) 주의

- `terraform.tfstate`는 원격 백엔드(S3 + DynamoDB lock)로 옮기는 것을 권장합니다.
- 현재 RDS는 `skip_final_snapshot=true` 입니다. 운영에서는 스냅샷 전략을 별도로 잡으세요.
