# Simple Quarkus Product Management Application

A comprehensive Java application built with Quarkus framework, featuring REST APIs, PostgreSQL database, Docker deployment, and AWS ECS production deployment capabilities.

## 🚀 Features

- ✅ **REST API**: Complete CRUD operations for product management
- ✅ **Database**: PostgreSQL with Hibernate ORM Panache
- ✅ **Docker**: Full containerization with Docker Compose
- ✅ **Health & Metrics**: Built-in monitoring with Prometheus metrics
- ✅ **Testing**: Comprehensive unit and integration tests  
- ✅ **AWS Deployment**: CloudFormation, Terraform, and GitHub Actions CI/CD
- ✅ **Data Persistence**: Local PostgreSQL data storage in `./DATA/`
- ✅ **Database Admin**: Adminer web interface for database management
- ✅ **CORS Support**: Cross-Origin Resource Sharing enabled
- ✅ **Validation**: Input validation with Hibernate Validator

## 🛠️ Technology Stack

- **Framework**: Quarkus 3.2.4.Final
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL 15
- **ORM**: Hibernate ORM with Panache
- **Containerization**: Docker & Docker Compose
- **Monitoring**: Micrometer + Prometheus
- **Health Checks**: SmallRye Health
- **Testing**: JUnit 5, RestAssured, H2 (test database)
- **Cloud**: AWS ECS, RDS, ALB, CloudWatch

## 📁 Project Structure

```
Quarkus/
├── src/
│   ├── main/
│   │   ├── java/com/example/quarkus/
│   │   │   ├── entity/
│   │   │   │   ├── Product.java
│   │   │   │   └── ProductCategory.java
│   │   │   ├── repository/
│   │   │   │   └── ProductRepository.java
│   │   │   ├── resource/
│   │   │   │   ├── ProductController.java
│   │   │   │   └── GreetingResource.java
│   │   │   └── QuarkusApp.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/schema.sql
│   └── test/java/com/example/quarkus/
│       ├── ProductControllerTest.java
│       └── GreetingResourceTest.java
├── aws/                          # AWS deployment files
│   ├── cloudformation-template.yml
│   ├── ecs-task-definition.json
│   └── terraform/
├── .github/workflows/            # GitHub Actions CI/CD
│   └── main.yml
├── DATA/                         # Local PostgreSQL data (created on first run)
├── docker-compose.yml            # Local development environment
├── Dockerfile                    # Container image definition
├── LOCAL_DOCKER_CMD.md          # Local development commands
├── AWS-DEPLOYMENT.md            # AWS deployment guide
├── fix-sequence.sh              # Database sequence fix script
└── pom.xml                      # Maven configuration
```

## 🌐 API Endpoints

### 🏠 Health & Info
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/q/health` | Application health check |
| GET | `/q/metrics` | Prometheus metrics |
| GET | `/api/hello` | Simple greeting endpoint |
| GET | `/api/hello?name=<name>` | Personalized greeting |

### 📦 Product Management API

#### Basic Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | Get all products (supports `?active=true`) |
| GET | `/api/products/{id}` | Get product by ID |
| POST | `/api/products` | Create a new product |
| PUT | `/api/products/{id}` | Update existing product |
| DELETE | `/api/products/{id}` | Delete product |

#### Search & Filter
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products/sku/{sku}` | Find product by SKU |
| GET | `/api/products/category/{category}` | Get products by category |
| GET | `/api/products/search?name={name}` | Search by product name |
| GET | `/api/products/search?minPrice={price}&maxPrice={price}` | Filter by price range |
| GET | `/api/products/low-stock?threshold={number}` | Get low stock products |

#### Inventory Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| PUT | `/api/products/{id}/activate` | Activate product |
| PUT | `/api/products/{id}/deactivate` | Deactivate product |
| PUT | `/api/products/{id}/stock` | Update stock quantity |
| PUT | `/api/products/{id}/stock/adjust` | Adjust stock (+ or -) |

#### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products/stats` | Product statistics |
| GET | `/api/products/categories` | Available categories |

### 📝 Product Categories
`ELECTRONICS`, `CLOTHING`, `BOOKS`, `HOME_GARDEN`, `SPORTS`, `TOYS`, `AUTOMOTIVE`, `BEAUTY`, `FOOD_BEVERAGE`, `OTHER`

## 🏃‍♂️ Quick Start (Local Development)

### 1. Prerequisites
```bash
# Ensure Java 17 is active
source ~/.zshrc
java -version  # Should show Java 17

# Ensure Docker is running
docker --version
docker-compose --version
```

### 2. Clone and Setup
```bash
git clone https://github.com/khairulanshar25/QUARKUS.git
cd QUARKUS
```

### 3. Start Full Environment (Recommended)
```bash
# Build and start everything with latest changes
./mvnw clean package && docker-compose up -d --build

# Wait for services to be ready
sleep 15
echo "🎉 Services ready!"
```

### 4. Access Services
- **🌐 Quarkus API**: http://localhost:8080
- **💾 Database Admin (Adminer)**: http://localhost:8081
- **📊 Health Check**: http://localhost:8080/q/health
- **📈 Metrics**: http://localhost:8080/q/metrics

#### Adminer Database Access
- **System**: PostgreSQL  
- **Server**: postgres
- **Username**: postgres
- **Password**: postgres
- **Database**: inventory_db

### 5. Test the API
```bash
# Check health
curl http://localhost:8080/q/health

# Get all products  
curl http://localhost:8080/api/products

# Create a product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "A test product for demo",
    "price": 29.99,
    "sku": "TEST-001", 
    "category": "ELECTRONICS",
    "quantity": 100
  }'

# Get product by ID
curl http://localhost:8080/api/products/1
```

## 🔧 Development Workflow

### Daily Development
```bash
# Start development environment
./mvnw clean package && docker-compose up -d --build

# View logs
docker logs -f simple-quarkus-app

# Stop services (keeps data)
docker-compose down

# Clean restart (removes all data)
docker-compose down && rm -rf DATA/ && docker-compose up -d --build
```

### Database Management
```bash
# Check sequence synchronization
docker exec -it quarkus_postgres psql -U postgres -d inventory_db -c "
  SELECT 'Max ID: ' || MAX(id) FROM products; 
  SELECT 'Sequence: ' || last_value FROM products_id_seq;"

# Fix sequence sync (if needed)
./fix-sequence.sh

# Backup data
cp -r DATA/ DATA-backup-$(date +%Y%m%d)

# Restore data
docker-compose down
rm -rf DATA/
cp -r DATA-backup-20250819/ DATA/
docker-compose up -d
```

### Testing
```bash
# Run all tests (includes Byte Buddy fix)
./mvnw clean test

# Run with coverage
./mvnw clean test jacoco:report

# Integration tests
./mvnw clean verify
```

## 🐳 Docker Configuration

### Local Environment (docker-compose.yml)
- **PostgreSQL**: Port 5432, data in `./DATA/`
- **Quarkus App**: Port 8080, auto-restart enabled  
- **Adminer**: Port 8081, database management UI
- **Network**: Isolated `quarkus-network` 
- **Health Checks**: All services monitored
- **Volumes**: Persistent data storage

### Production Dockerfile
- **Multi-stage build**: Maven build + UBI runtime
- **Security**: Non-root user (185)
- **Health check**: Built-in endpoint monitoring
- **Optimized**: Minimal image size with required dependencies

## ☁️ AWS Production Deployment

### 🚀 GitHub Actions CI/CD (Recommended)

1. **Setup GitHub Secrets**:
   ```bash
   AWS_ACCESS_KEY_ID=your-access-key
   AWS_SECRET_ACCESS_KEY=your-secret-key
   ```

2. **Deploy Infrastructure** (one-time):
   ```bash
   git commit -m "Deploy infrastructure [deploy-infra]"
   git push origin main
   ```

3. **Automatic Deployments**:
   - Push to `main` → Build + Deploy to production
   - Push to `develop` → Run tests only
   - Pull requests → Run tests

### 🏗️ Manual Deployment Options

#### CloudFormation
```bash
aws cloudformation deploy \
  --template-file aws/cloudformation-template.yml \
  --stack-name quarkus-app-stack \
  --capabilities CAPABILITY_IAM
```

#### Terraform
```bash
cd aws/terraform
terraform init
terraform plan
terraform apply
```

#### Direct ECS
```bash
./deploy-to-aws.sh production
```

### 🗄️ Database Configuration (AWS RDS)
The application uses PostgreSQL and requires:
```bash
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://your-rds-endpoint:5432/inventory_db
QUARKUS_DATASOURCE_USERNAME=postgres
QUARKUS_DATASOURCE_PASSWORD=your-password  # From SSM Parameter Store
```

## 🔍 Monitoring & Troubleshooting

### Health Monitoring
```bash
# Application health
curl http://localhost:8080/q/health

# Database health  
curl http://localhost:8080/q/health/ready

# Detailed metrics
curl http://localhost:8080/q/metrics
```

### Common Issues & Solutions

#### 🚨 Duplicate Key Error
**Symptoms**: `{"details":"Error id xxx","stack":""}`

**Solutions** (fastest to slowest):
```bash
# Method 1: Database fix (instant)
docker exec -it quarkus_postgres psql -U postgres -d inventory_db -c \
  "SELECT setval('products_id_seq', (SELECT MAX(id) FROM products) + 50);"

# Method 2: Use fix script  
./fix-sequence.sh

# Method 3: Container restart (15 seconds)
docker restart simple-quarkus-app && sleep 15
```

#### 🚨 Container Issues
```bash
# Check container status
docker ps

# View logs
docker logs simple-quarkus-app
docker logs quarkus_postgres

# Restart specific service
docker-compose restart quarkus-app

# Complete rebuild
docker-compose down && docker-compose up -d --build
```

#### 🚨 Port Conflicts
```bash
# Check what's using ports
lsof -i :8080
lsof -i :8081  
lsof -i :5432

# Change ports in docker-compose.yml if needed
```

### Database Administration
```bash
# Connect to database
docker exec -it quarkus_postgres psql -U postgres -d inventory_db

# Run queries
SELECT * FROM products LIMIT 5;
SELECT COUNT(*) FROM products;
\dt  -- List tables
\q   -- Quit
```

## 🧪 Testing

### Test Configuration  
- **Unit Tests**: H2 in-memory database
- **Integration Tests**: TestContainers with PostgreSQL
- **Coverage**: JaCoCo reports
- **Fixed Issues**: Byte Buddy Java compatibility

### Running Tests
```bash
# All tests
./mvnw clean test

# Specific test class
./mvnw test -Dtest=ProductControllerTest

# Integration tests
./mvnw clean verify

# Skip tests
./mvnw clean package -DskipTests
```

## 📊 Performance & Configuration

### JVM Configuration
```bash
# Production JVM options
JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# Development JVM options  
JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

### Database Connection Pool
```properties
# In application.properties
quarkus.datasource.jdbc.max-size=16
quarkus.datasource.jdbc.min-size=2
```

### Container Resources
```yaml
# docker-compose.yml
services:
  quarkus-app:
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
```

## 🤝 Contributing

1. **Fork** the repository
2. **Create** feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** changes (`git commit -m 'Add amazing feature'`)
4. **Push** to branch (`git push origin feature/amazing-feature`)
5. **Open** Pull Request

### Development Guidelines
- Follow Java conventions and use proper naming
- Add tests for new features
- Update documentation
- Ensure Docker builds successfully
- Test locally before pushing

## 📄 Documentation

- **[LOCAL_DOCKER_CMD.md](LOCAL_DOCKER_CMD.md)**: Local development commands
- **[AWS-DEPLOYMENT.md](AWS-DEPLOYMENT.md)**: AWS deployment guide  
- **[GITHUB-ACTIONS-SETUP.md](GITHUB-ACTIONS-SETUP.md)**: CI/CD setup
- **[API Documentation](http://localhost:8080/q/swagger-ui/)**: Interactive API docs (when running)

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙋‍♂️ Support

- **Issues**: [GitHub Issues](https://github.com/khairulanshar25/QUARKUS/issues)
- **Discussions**: [GitHub Discussions](https://github.com/khairulanshar25/QUARKUS/discussions)
- **Quarkus Docs**: [https://quarkus.io/](https://quarkus.io/)

---

**Made with ❤️ using Quarkus - Supersonic Subatomic Java**
