# GitHub Actions Secrets Setup

This document describes the required secrets for the GitHub Actions CI/CD pipeline.

## Required Secrets

To enable automatic deployment to AWS ECS, you need to configure the following secrets in your GitHub repository:

### AWS Credentials

1. **AWS_ACCESS_KEY_ID**
   - Description: AWS Access Key ID for programmatic access
   - Required for: Authenticating with AWS services
   - How to get: Create an IAM user with programmatic access

2. **AWS_SECRET_ACCESS_KEY**
   - Description: AWS Secret Access Key for programmatic access
   - Required for: Authenticating with AWS services
   - How to get: Generated when creating IAM user

## IAM User Setup

### 1. Create IAM User

Create an IAM user with the following policies attached:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "ecr:BatchDeleteImage",
                "ecr:InitiateLayerUpload",
                "ecr:UploadLayerPart",
                "ecr:CompleteLayerUpload",
                "ecr:PutImage",
                "ecr:CreateRepository",
                "ecr:DescribeRepositories"
            ],
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "ecs:DescribeClusters",
                "ecs:DescribeServices",
                "ecs:DescribeTaskDefinition",
                "ecs:RegisterTaskDefinition",
                "ecs:UpdateService",
                "ecs:DescribeTasks"
            ],
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "cloudformation:CreateStack",
                "cloudformation:UpdateStack",
                "cloudformation:DescribeStacks",
                "cloudformation:DescribeStackEvents",
                "cloudformation:DescribeStackResources",
                "cloudformation:GetTemplate"
            ],
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "ssm:GetParameter",
                "ssm:PutParameter",
                "ssm:GetParameters",
                "ssm:GetParametersByPath"
            ],
            "Resource": "arn:aws:ssm:*:*:parameter/quarkus/*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "sts:GetCallerIdentity"
            ],
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "iam:CreateRole",
                "iam:DeleteRole",
                "iam:GetRole",
                "iam:PassRole",
                "iam:AttachRolePolicy",
                "iam:DetachRolePolicy",
                "iam:ListAttachedRolePolicies"
            ],
            "Resource": "arn:aws:iam::*:role/dev-quarkus-*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "ec2:CreateVpc",
                "ec2:DeleteVpc",
                "ec2:DescribeVpcs",
                "ec2:CreateSubnet",
                "ec2:DeleteSubnet",
                "ec2:DescribeSubnets",
                "ec2:CreateInternetGateway",
                "ec2:DeleteInternetGateway",
                "ec2:AttachInternetGateway",
                "ec2:DetachInternetGateway",
                "ec2:DescribeInternetGateways",
                "ec2:CreateRouteTable",
                "ec2:DeleteRouteTable",
                "ec2:DescribeRouteTables",
                "ec2:CreateRoute",
                "ec2:DeleteRoute",
                "ec2:AssociateRouteTable",
                "ec2:DisassociateRouteTable",
                "ec2:CreateSecurityGroup",
                "ec2:DeleteSecurityGroup",
                "ec2:DescribeSecurityGroups",
                "ec2:AuthorizeSecurityGroupIngress",
                "ec2:AuthorizeSecurityGroupEgress",
                "ec2:RevokeSecurityGroupIngress",
                "ec2:RevokeSecurityGroupEgress",
                "ec2:DescribeAvailabilityZones"
            ],
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "elasticloadbalancing:CreateLoadBalancer",
                "elasticloadbalancing:DeleteLoadBalancer",
                "elasticloadbalancing:DescribeLoadBalancers",
                "elasticloadbalancing:CreateTargetGroup",
                "elasticloadbalancing:DeleteTargetGroup",
                "elasticloadbalancing:DescribeTargetGroups",
                "elasticloadbalancing:CreateListener",
                "elasticloadbalancing:DeleteListener",
                "elasticloadbalancing:DescribeListeners",
                "elasticloadbalancing:ModifyTargetGroupAttributes"
            ],
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "rds:CreateDBInstance",
                "rds:DeleteDBInstance",
                "rds:DescribeDBInstances",
                "rds:CreateDBSubnetGroup",
                "rds:DeleteDBSubnetGroup",
                "rds:DescribeDBSubnetGroups"
            ],
            "Resource": "*"
        }
    ]
}
```

### 2. Generate Access Keys

1. In IAM Console, select your user
2. Go to "Security credentials" tab
3. Click "Create access key"
4. Choose "Command Line Interface (CLI)"
5. Save the Access Key ID and Secret Access Key

## Adding Secrets to GitHub

1. Go to your GitHub repository
2. Click on "Settings" tab
3. In the left sidebar, click "Secrets and variables" → "Actions"
4. Click "New repository secret"
5. Add each secret:
   - Name: `AWS_ACCESS_KEY_ID`, Value: Your AWS Access Key ID
   - Name: `AWS_SECRET_ACCESS_KEY`, Value: Your AWS Secret Access Key

## Pipeline Workflow

### Triggers

The pipeline runs on:

1. **Push to main branch**: Runs tests, builds, and deploys application
2. **Push to develop branch**: Runs tests only
3. **Pull requests**: Runs tests only
4. **Push to main with `[deploy-infra]` in commit message**: Deploys infrastructure

### Jobs

1. **test**: Runs unit tests with PostgreSQL service
2. **build-and-deploy**: Builds Docker image, pushes to ECR, deploys to ECS
3. **deploy-infrastructure**: Creates/updates AWS infrastructure (triggered by commit message)

### Deployment Process

1. Checkout code and setup Java 17
2. Run tests with PostgreSQL service
3. Build application JAR
4. Build and push Docker image to ECR
5. Update ECS task definition with new image
6. Deploy to ECS cluster
7. Wait for service stability
8. Test deployment health endpoints

## Environment Variables

The pipeline uses these environment variables:

- `AWS_REGION`: us-east-1 (can be changed in workflow)
- `ECR_REPOSITORY`: simple-quarkus-app

## Infrastructure Deployment

To deploy infrastructure for the first time:

1. Ensure secrets are configured
2. Make a commit with `[deploy-infra]` in the message:
   ```bash
   git commit -m "Initial infrastructure deployment [deploy-infra]"
   git push origin main
   ```

This will create:
- ECR repository
- ECS cluster and service
- RDS PostgreSQL database
- VPC with subnets and security groups
- Application Load Balancer

## Monitoring

The pipeline includes:

- Test result uploads
- Health check validation
- Deployment status notifications
- Application URL output

## Troubleshooting

### Common Issues

1. **IAM Permissions**: Ensure the IAM user has all required permissions
2. **ECR Authentication**: The pipeline handles ECR login automatically
3. **ECS Service**: If service doesn't exist, deploy infrastructure first
4. **Database Connection**: RDS instance may take several minutes to become available

### Manual Verification

After deployment, you can verify:

```bash
# Check ECS service status
aws ecs describe-services --cluster dev-quarkus-cluster --services dev-quarkus-service

# Check task definition
aws ecs describe-task-definition --task-definition dev-simple-quarkus-app

# Check application health
curl https://your-alb-dns/q/health

# Check API endpoints
curl https://your-alb-dns/api/products
```
