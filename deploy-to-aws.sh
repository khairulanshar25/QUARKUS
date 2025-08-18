#!/bin/bash

# AWS ECS Deployment Script for Quarkus Application
# This script automates the deployment process to AWS ECS

set -e

# Configuration
AWS_REGION="${AWS_REGION:-us-east-1}"
AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:-$(aws sts get-caller-identity --query Account --output text)}"
ECR_REPOSITORY_NAME="simple-quarkus-app"
IMAGE_TAG="${IMAGE_TAG:-latest}"
ENVIRONMENT="${ENVIRONMENT:-dev}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if AWS CLI is configured
check_aws_cli() {
    if ! command -v aws &> /dev/null; then
        echo_error "AWS CLI is not installed. Please install it first."
        exit 1
    fi
    
    if ! aws sts get-caller-identity &> /dev/null; then
        echo_error "AWS CLI is not configured. Please run 'aws configure' first."
        exit 1
    fi
    
    echo_info "AWS CLI is configured ✓"
}

# Function to create ECR repository if it doesn't exist
create_ecr_repository() {
    echo_info "Checking ECR repository..."
    
    if ! aws ecr describe-repositories --repository-names $ECR_REPOSITORY_NAME --region $AWS_REGION &> /dev/null; then
        echo_info "Creating ECR repository: $ECR_REPOSITORY_NAME"
        aws ecr create-repository \
            --repository-name $ECR_REPOSITORY_NAME \
            --region $AWS_REGION \
            --image-scanning-configuration scanOnPush=true
    else
        echo_info "ECR repository already exists ✓"
    fi
}

# Function to build and push Docker image
build_and_push_image() {
    echo_info "Building and pushing Docker image..."
    
    # Get ECR login token
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
    
    # Build image
    docker build -t $ECR_REPOSITORY_NAME:$IMAGE_TAG .
    
    # Tag image for ECR
    docker tag $ECR_REPOSITORY_NAME:$IMAGE_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY_NAME:$IMAGE_TAG
    
    # Push image
    docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY_NAME:$IMAGE_TAG
    
    echo_info "Image pushed successfully ✓"
}

# Function to create database password in SSM Parameter Store
create_db_password() {
    echo_info "Creating database password in SSM Parameter Store..."
    
    if ! aws ssm get-parameter --name "/quarkus/db/password" --region $AWS_REGION &> /dev/null; then
        # Generate a random password
        DB_PASSWORD=$(openssl rand -base64 32)
        
        aws ssm put-parameter \
            --name "/quarkus/db/password" \
            --value "$DB_PASSWORD" \
            --type "SecureString" \
            --region $AWS_REGION
        
        echo_info "Database password created in SSM Parameter Store ✓"
    else
        echo_info "Database password already exists in SSM Parameter Store ✓"
    fi
}

# Function to deploy infrastructure using CloudFormation
deploy_infrastructure() {
    echo_info "Deploying infrastructure using CloudFormation..."
    
    local stack_name="${ENVIRONMENT}-quarkus-infrastructure"
    local template_file="aws/cloudformation-template.yml"
    local image_uri="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY_NAME:$IMAGE_TAG"
    
    if ! aws cloudformation describe-stacks --stack-name $stack_name --region $AWS_REGION &> /dev/null; then
        echo_info "Creating CloudFormation stack: $stack_name"
        aws cloudformation create-stack \
            --stack-name $stack_name \
            --template-body file://$template_file \
            --parameters ParameterKey=Environment,ParameterValue=$ENVIRONMENT \
                        ParameterKey=ImageUri,ParameterValue=$image_uri \
            --capabilities CAPABILITY_IAM \
            --region $AWS_REGION
        
        echo_info "Waiting for stack creation to complete..."
        aws cloudformation wait stack-create-complete --stack-name $stack_name --region $AWS_REGION
    else
        echo_info "Updating CloudFormation stack: $stack_name"
        aws cloudformation update-stack \
            --stack-name $stack_name \
            --template-body file://$template_file \
            --parameters ParameterKey=Environment,ParameterValue=$ENVIRONMENT \
                        ParameterKey=ImageUri,ParameterValue=$image_uri \
            --capabilities CAPABILITY_IAM \
            --region $AWS_REGION
        
        echo_info "Waiting for stack update to complete..."
        aws cloudformation wait stack-update-complete --stack-name $stack_name --region $AWS_REGION
    fi
    
    echo_info "Infrastructure deployment completed ✓"
}

# Function to get deployment outputs
get_deployment_outputs() {
    echo_info "Getting deployment outputs..."
    
    local stack_name="${ENVIRONMENT}-quarkus-infrastructure"
    
    ALB_URL=$(aws cloudformation describe-stacks \
        --stack-name $stack_name \
        --region $AWS_REGION \
        --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerURL`].OutputValue' \
        --output text)
    
    DB_ENDPOINT=$(aws cloudformation describe-stacks \
        --stack-name $stack_name \
        --region $AWS_REGION \
        --query 'Stacks[0].Outputs[?OutputKey==`DatabaseEndpoint`].OutputValue' \
        --output text)
    
    echo_info "Application URL: $ALB_URL"
    echo_info "Database Endpoint: $DB_ENDPOINT"
}

# Function to wait for service to be healthy
wait_for_service() {
    echo_info "Waiting for ECS service to be healthy..."
    
    local cluster_name="${ENVIRONMENT}-quarkus-cluster"
    local service_name="${ENVIRONMENT}-quarkus-service"
    
    aws ecs wait services-stable \
        --cluster $cluster_name \
        --services $service_name \
        --region $AWS_REGION
    
    echo_info "ECS service is stable ✓"
}

# Function to test the deployment
test_deployment() {
    echo_info "Testing deployment..."
    
    if [ -n "$ALB_URL" ]; then
        echo_info "Testing health endpoint..."
        sleep 30  # Give the service some time to start
        
        for i in {1..10}; do
            if curl -f "$ALB_URL/q/health" &> /dev/null; then
                echo_info "Health check passed ✓"
                break
            else
                echo_warn "Health check failed, retrying in 30 seconds... (attempt $i/10)"
                sleep 30
            fi
        done
        
        echo_info "Testing API endpoints..."
        curl -s "$ALB_URL/api/products" | head -c 100
        echo
    fi
}

# Main deployment flow
main() {
    echo_info "Starting deployment to AWS ECS..."
    echo_info "Environment: $ENVIRONMENT"
    echo_info "AWS Region: $AWS_REGION"
    echo_info "AWS Account ID: $AWS_ACCOUNT_ID"
    
    check_aws_cli
    create_ecr_repository
    build_and_push_image
    create_db_password
    deploy_infrastructure
    wait_for_service
    get_deployment_outputs
    test_deployment
    
    echo_info "Deployment completed successfully! 🚀"
    echo_info "Your application is available at: $ALB_URL"
}

# Script options
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "build")
        check_aws_cli
        create_ecr_repository
        build_and_push_image
        ;;
    "infrastructure")
        check_aws_cli
        create_db_password
        deploy_infrastructure
        get_deployment_outputs
        ;;
    "test")
        get_deployment_outputs
        test_deployment
        ;;
    "clean")
        echo_warn "This will delete the entire infrastructure. Are you sure? (y/N)"
        read -r response
        if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
            aws cloudformation delete-stack --stack-name "${ENVIRONMENT}-quarkus-infrastructure" --region $AWS_REGION
            echo_info "Stack deletion initiated. This may take several minutes."
        fi
        ;;
    *)
        echo "Usage: $0 {deploy|build|infrastructure|test|clean}"
        echo "  deploy        - Full deployment (build + infrastructure + test)"
        echo "  build         - Build and push Docker image only"
        echo "  infrastructure - Deploy infrastructure only"
        echo "  test          - Test the deployed application"
        echo "  clean         - Delete the entire infrastructure"
        exit 1
        ;;
esac
