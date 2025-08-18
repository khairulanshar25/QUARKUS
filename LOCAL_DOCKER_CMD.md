# Local Docker Deployment Commands

This document provides quick reference commands for running the Quarkus application locally with Docker.

## 🚀 Quick Start Commands

### 0. Set Java 17
```bash
source ~/.zshrc 
```

### 1. Build and Start Everything (Force Rebuild)
```bash
./mvnw clean package && docker-compose up -d --build
```
> **Recommended**: The `--build` flag forces Docker Compose to rebuild the image even if it already exists, ensuring you always get the latest changes.

### Alternative Build Commands
```bash
# Basic build (only rebuilds if Dockerfile changed)
./mvnw clean package && docker-compose up -d

# Build without starting
./mvnw clean package && docker-compose build

# Force rebuild with no cache
./mvnw clean package && docker-compose build --no-cache

# Manual approach (not recommended)
./mvnw clean package
docker build -t simple-quarkus-app .
docker-compose up -d
```

### 2. Check Running Services
```bash
docker ps
```

### 3. Check Application Health
```bash
curl http://localhost:8080/q/health
```

### 4. Test API Endpoints
```bash
# Get all products
curl http://localhost:8080/api/products

# Create a product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 29.99,
    "sku": "TEST-001",
    "category": "ELECTRONICS",
    "quantity": 100
  }'

# Get product by ID
curl http://localhost:8080/api/products/1
```

### Check Database Sequence Value

**Check current sequence value:**
```bash
docker exec -it quarkus_postgres psql -U postgres -d inventory_db -c "SELECT last_value FROM products_seq;"
```

**Check what the next ID will be:**
```bash
docker exec -it quarkus_postgres psql -U postgres -d inventory_db -c "SELECT last_value + 1 FROM products_seq;"
```

**Check maximum product ID:**
```bash
docker exec -it quarkus_postgres psql -U postgres -d inventory_db -c "SELECT MAX(id) FROM products;"
```



# Stop services (data remains in ./DATA)
```bash
docker-compose down
```

# Complete cleanup (removes DATA folder)
```bash
docker-compose down && rm -rf DATA/
```

# Backup data
```bash
cp -r DATA/ DATA-backup-$(date +%Y%m%d)
```

# Restore data
```bash
docker-compose down
rm -rf DATA/
cp -r DATA-backup-$(date +%Y%m%d)/ DATA/
docker-compose up -d
```