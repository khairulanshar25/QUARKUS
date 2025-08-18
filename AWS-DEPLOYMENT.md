# AWS ECS Deployment Guide

This guide provides multiple options for deploying your Quarkus application to AWS ECS (Elastic Container Service).

## Prerequisites

1. **AWS CLI** installed and configured
   ```bash
   aws configure
   ```

2. **Docker** installed and running

3. **AWS Account** with appropriate permissions for:
   - ECR (Elastic Container Registry)
   - ECS (Elastic Container Service)
   - RDS (Relational Database Service)
   - VPC, Subnets, Security Groups
   - IAM Roles and Policies
   - Application Load Balancer
   - CloudWatch Logs
   - Systems Manager Parameter Store

## Deployment Options

### Option 1: Automated Deployment Script (Recommended)

The easiest way to deploy is using the automated script:

```bash
# Set environment variables (optional)
export AWS_REGION=us-east-1
export ENVIRONMENT=dev
export IMAGE_TAG=latest

# Run full deployment
./deploy-to-aws.sh deploy
```

**Script Commands:**
- `deploy` - Complete deployment (build + infrastructure + test)
- `build` - Build and push Docker image only
- `infrastructure` - Deploy infrastructure only
- `test` - Test the deployed application
- `clean` - Delete the entire infrastructure

### Option 2: CloudFormation Template

1. **Build and Push Docker Image:**
   ```bash
   # Get your AWS account ID
   AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
   AWS_REGION=us-east-1
   
   # Create ECR repository
   aws ecr create-repository --repository-name simple-quarkus-app --region $AWS_REGION
   
   # Build and push image
   aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
   docker build -t simple-quarkus-app:latest .
   docker tag simple-quarkus-app:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/simple-quarkus-app:latest
   docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/simple-quarkus-app:latest
   ```

2. **Create Database Password in SSM:**
   ```bash
   aws ssm put-parameter \
     --name "/quarkus/db/password" \
     --value "your-secure-password" \
     --type "SecureString" \
     --region $AWS_REGION
   ```

3. **Deploy Infrastructure:**
   ```bash
   aws cloudformation create-stack \
     --stack-name dev-quarkus-infrastructure \
     --template-body file://aws/cloudformation-template.yml \
     --parameters ParameterKey=Environment,ParameterValue=dev \
                  ParameterKey=ImageUri,ParameterValue=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/simple-quarkus-app:latest \
     --capabilities CAPABILITY_IAM \
     --region $AWS_REGION
   ```

### Option 3: Terraform

1. **Initialize Terraform:**
   ```bash
   cd aws/terraform
   terraform init
   ```

2. **Create variables file:**
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   # Edit terraform.tfvars with your values
   ```

3. **Plan and Apply:**
   ```bash
   terraform plan
   terraform apply
   ```

## Architecture Overview

The deployment creates the following AWS resources:

### Networking
- **VPC** with public and private subnets across 2 AZs
- **Internet Gateway** for public internet access
- **Security Groups** for ALB, ECS, and RDS

### Compute
- **ECS Fargate Cluster** for running containers
- **ECS Service** with 2 tasks for high availability
- **Application Load Balancer** for traffic distribution

### Database
- **RDS PostgreSQL** instance in private subnets
- **DB Subnet Group** for multi-AZ deployment

### Storage & Secrets
- **ECR Repository** for container images
- **SSM Parameter Store** for database password
- **CloudWatch Logs** for application logging

## Configuration

### Environment Variables

The application supports these environment variables:

- `QUARKUS_PROFILE` - Application profile (prod)
- `QUARKUS_LOG_LEVEL` - Logging level (INFO)
- `QUARKUS_DATASOURCE_JDBC_URL` - Database connection string
- `QUARKUS_DATASOURCE_USERNAME` - Database username
- `QUARKUS_DATASOURCE_PASSWORD` - Database password (from SSM)

### Health Checks

The deployment includes health checks at multiple levels:

1. **Container Health Check** - `/q/health` endpoint
2. **Load Balancer Health Check** - Target group health
3. **ECS Service Health Check** - Task health monitoring

## Monitoring and Logging

### CloudWatch Logs
Application logs are automatically sent to CloudWatch Logs:
- Log Group: `/ecs/{environment}-simple-quarkus-app`
- Retention: 7 days (configurable)

### Metrics
The application exposes Prometheus metrics at `/q/metrics` endpoint.

### Health Monitoring
Health status available at `/q/health` endpoint.

## API Endpoints

Once deployed, your API will be available at:
- **Base URL**: `http://{ALB-DNS-NAME}`
- **Health Check**: `GET /q/health`
- **Metrics**: `GET /q/metrics`
- **Products API**: `GET /api/products`

## Scaling

### Horizontal Scaling
Modify the `DesiredCount` in the ECS service to scale the number of tasks:

```bash
aws ecs update-service \
  --cluster dev-quarkus-cluster \
  --service dev-quarkus-service \
  --desired-count 4
```

### Vertical Scaling
Update the task definition with higher CPU/memory:
- CPU: 256, 512, 1024, 2048, 4096
- Memory: 512MB to 30GB (depends on CPU)

### Auto Scaling
Add Application Auto Scaling for automatic scaling based on metrics:

```bash
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --scalable-dimension ecs:service:DesiredCount \
  --resource-id service/dev-quarkus-cluster/dev-quarkus-service \
  --min-capacity 2 \
  --max-capacity 10
```

## Security Considerations

1. **Database Security**
   - RDS in private subnets
   - Security group allows access only from ECS tasks
   - Password stored in SSM Parameter Store (encrypted)

2. **Network Security**
   - ECS tasks in public subnets with security groups
   - ALB security group allows HTTP/HTTPS from internet
   - ECS security group allows traffic only from ALB

3. **IAM Security**
   - Minimal IAM permissions for ECS task execution
   - Separate roles for task execution and task runtime

## Cost Optimization

1. **Use Fargate Spot** for non-production environments
2. **Right-size resources** based on actual usage
3. **Enable RDS Performance Insights** for database optimization
4. **Use CloudWatch Logs retention** to control log storage costs

## Troubleshooting

### Common Issues

1. **Task fails to start**
   - Check CloudWatch logs: `/ecs/{environment}-simple-quarkus-app`
   - Verify ECR image URI is correct
   - Check IAM permissions

2. **Database connection issues**
   - Verify RDS endpoint in environment variables
   - Check security group rules
   - Verify SSM parameter for database password

3. **Load balancer health checks failing**
   - Verify application is listening on port 8080
   - Check `/q/health` endpoint response
   - Review target group health check settings

### Debugging Commands

```bash
# Check ECS service status
aws ecs describe-services --cluster dev-quarkus-cluster --services dev-quarkus-service

# View task logs
aws logs get-log-events --log-group-name "/ecs/dev-simple-quarkus-app" --log-stream-name "ecs/simple-quarkus-app/{task-id}"

# Check target group health
aws elbv2 describe-target-health --target-group-arn {target-group-arn}
```

## Cleanup

To remove all resources:

```bash
# Using the deployment script
./deploy-to-aws.sh clean

# Using CloudFormation
aws cloudformation delete-stack --stack-name dev-quarkus-infrastructure

# Using Terraform
cd aws/terraform
terraform destroy
```

## Next Steps

1. **Set up CI/CD Pipeline** with GitHub Actions or AWS CodePipeline
2. **Add SSL/TLS Certificate** to the Application Load Balancer
3. **Configure Custom Domain** with Route 53
4. **Set up Monitoring** with CloudWatch dashboards and alarms
5. **Implement Blue/Green Deployment** for zero-downtime updates
