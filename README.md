# Simple Quarkus Application

A simple Java application built with Quarkus framework, featuring REST APIs and Docker deployment capabilities.

## Features

- ✅ RESTful API endpoints
- ✅ Health checks and metrics
- ✅ Docker containerization
- ✅ JSON response format
- ✅ Cross-Origin Resource Sharing (CORS) support
- ✅ Comprehensive logging

## Technology Stack

- **Framework**: Quarkus 3.2.4.Final
- **Language**: Java 17
- **Build Tool**: Maven
- **Containerization**: Docker
- **Metrics**: Micrometer with Prometheus
- **Health Checks**: SmallRye Health

## Project Structure

```
Quarkus/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── quarkus/
│   │   │               ├── QuarkusApp.java
│   │   │               └── GreetingResource.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── quarkus/
├── Dockerfile
├── .dockerignore
├── pom.xml
└── README.md
```

## API Endpoints

### Greeting API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/hello` | Returns a greeting message |
| GET | `/api/hello?name=<name>` | Returns a personalized greeting |
| GET | `/api/status` | Returns application status information |
| GET | `/api/info` | Returns detailed application information |
| GET | `/q/health` | Health check endpoint (Quarkus built-in) |
| GET | `/q/metrics` | Metrics endpoint (Prometheus format) |

### Product API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | Get all products (use `?active=true` for active products only) |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products/sku/{sku}` | Get product by SKU |
| GET | `/api/products/category/{category}` | Get products by category |
| GET | `/api/products/search` | Search products by name or price range |
| GET | `/api/products/low-stock` | Get low stock products (use `?threshold=N`) |
| POST | `/api/products` | Create a new product |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |
| PUT | `/api/products/{id}/deactivate` | Deactivate a product |
| PUT | `/api/products/{id}/activate` | Activate a product |
| PUT | `/api/products/{id}/stock` | Update product stock |
| PUT | `/api/products/{id}/stock/adjust` | Adjust product stock |
| GET | `/api/products/stats` | Get product statistics |
| GET | `/api/products/categories` | Get all available categories |

### Product Categories
- ELECTRONICS, CLOTHING, BOOKS, HOME_GARDEN, SPORTS, TOYS, AUTOMOTIVE, BEAUTY, FOOD_BEVERAGE, OTHER

## Prerequisites

- Java 17 or higher
- Maven 3.8.1 or higher
- Docker (for containerization)

## Local Development

### 1. Clone and Navigate

```bash
cd /Users/khairulanshar/java/copilot/management/Quarkus
```

### 2. Run in Development Mode

```bash
./mvnw quarkus:dev
```

This starts the application in development mode with live reload enabled. The application will be available at `http://localhost:8080`.

### 3. Test the API

```bash
# Basic greeting
curl http://localhost:8080/api/hello

# Personalized greeting
curl "http://localhost:8080/api/hello?name=John"

# Application status
curl http://localhost:8080/api/status

# Application info
curl http://localhost:8080/api/info

# Health check
curl http://localhost:8080/q/health

# Metrics
curl http://localhost:8080/q/metrics
```

## Building and Running

### 1. Package the Application

```bash
./mvnw clean package
```

### 2. Run the Packaged Application

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

### 3. Build Native Executable (Optional)

```bash
./mvnw clean package -Pnative
```

## Docker Deployment

### 1. Build Docker Image

```bash
docker build -t simple-quarkus-app:latest .
```

### 2. Run Docker Container

```bash
docker run -d \
  --name quarkus-app \
  -p 8080:8080 \
  simple-quarkus-app:latest
```

### 3. Check Container Status

```bash
# View running containers
docker ps

# Check application logs
docker logs quarkus-app

# Check health
curl http://localhost:8080/q/health
```

### 4. Stop and Remove Container

```bash
docker stop quarkus-app
docker rm quarkus-app
```

## Docker Compose (Optional)

Create a `docker-compose.yml` file for easier management:

```yaml
version: '3.8'
services:
  quarkus-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - QUARKUS_PROFILE=prod
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/q/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

Run with Docker Compose:

```bash
docker-compose up -d
```

## Environment Variables

You can configure the application using environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `QUARKUS_HTTP_PORT` | Application port | `8080` |
| `QUARKUS_HTTP_HOST` | Bind address | `0.0.0.0` |
| `QUARKUS_LOG_LEVEL` | Log level | `INFO` |

Example:

```bash
docker run -d \
  --name quarkus-app \
  -p 9090:9090 \
  -e QUARKUS_HTTP_PORT=9090 \
  -e QUARKUS_LOG_LEVEL=DEBUG \
  simple-quarkus-app:latest
```

## Testing

### Run Unit Tests

```bash
./mvnw test
```

### Run Integration Tests

```bash
./mvnw verify
```

## Production Deployment

### 1. Build Production Image

```bash
docker build -t simple-quarkus-app:1.0.0 .
```

### 2. Tag for Registry

```bash
docker tag simple-quarkus-app:1.0.0 your-registry/simple-quarkus-app:1.0.0
```

### 3. Push to Registry

```bash
docker push your-registry/simple-quarkus-app:1.0.0
```

### 4. Deploy to Production

```bash
docker run -d \
  --name quarkus-prod \
  -p 80:8080 \
  --restart unless-stopped \
  -e QUARKUS_PROFILE=prod \
  your-registry/simple-quarkus-app:1.0.0
```

## Monitoring

- **Health Check**: `GET /q/health`
- **Metrics**: `GET /q/metrics` (Prometheus format)
- **Application Info**: `GET /api/info`

## Troubleshooting

### Common Issues

1. **Port already in use**: Change the port using `-e QUARKUS_HTTP_PORT=8081`
2. **Memory issues**: Increase Docker memory allocation
3. **Permission denied**: Ensure Docker daemon is running

### Logs

```bash
# Application logs
docker logs quarkus-app

# Follow logs
docker logs -f quarkus-app

# Last 100 lines
docker logs --tail 100 quarkus-app
```

## Development Tips

- Use `./mvnw quarkus:dev` for hot reload during development
- Access the Quarkus Dev UI at `http://localhost:8080/q/dev/`
- Enable debug logging with `-e QUARKUS_LOG_LEVEL=DEBUG`

## AWS ECS Deployment

This application can be deployed to AWS ECS using several methods:

### 1. GitHub Actions CI/CD (Recommended)

The project includes a complete GitHub Actions pipeline for automated deployment:

1. **Setup GitHub Secrets** (see [GITHUB-ACTIONS-SETUP.md](GITHUB-ACTIONS-SETUP.md)):
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`

2. **Deploy Infrastructure** (one-time setup):
   ```bash
   git commit -m "Deploy infrastructure [deploy-infra]"
   git push origin main
   ```

3. **Automatic Deployment**:
   - Push to `main` branch triggers build and deployment
   - Push to `develop` branch runs tests only
   - Pull requests run tests

### 2. Manual Deployment Options

Choose from multiple deployment methods:

- **CloudFormation**: `aws/cloudformation-template.yml`
- **Terraform**: `aws/terraform/main.tf`
- **Direct ECS**: `aws/ecs-task-definition.json`
- **Bash Script**: `./deploy-to-aws.sh`

See [AWS-DEPLOYMENT.md](AWS-DEPLOYMENT.md) for detailed instructions.

### 3. Database Configuration

The application uses PostgreSQL and requires these environment variables:

```bash
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://your-rds-endpoint:5432/inventory_db
QUARKUS_DATASOURCE_USERNAME=postgres
QUARKUS_DATASOURCE_PASSWORD=your-password
```

Database password is automatically managed via AWS Systems Manager Parameter Store.

### 4. Container Configuration

The Docker image is optimized for ECS deployment:

- **Health Check**: Built-in container health monitoring
- **Resource Limits**: 512 CPU units, 1024 MB memory (configurable)
- **Logging**: CloudWatch integration
- **Secrets**: SSM Parameter Store integration

### 5. Load Balancer and DNS

The CloudFormation template creates:

- Application Load Balancer with health checks
- Target group for container health monitoring
- Security groups for proper network access
- Optional: Route 53 DNS (if domain is available)

## License

This project is open source and available under the [MIT License](LICENSE).

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

---

For more information about Quarkus, visit: https://quarkus.io/
