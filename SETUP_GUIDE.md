# Setup Guide - Trading Gateway

Complete step-by-step guide to set up the Trading Gateway from scratch.

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21+** - [Download](https://adoptium.net/)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- **Docker** - [Download](https://www.docker.com/get-started)
- **Docker Compose** - Included with Docker Desktop
- **Git** - [Download](https://git-scm.com/downloads)
- **curl** - For testing (usually pre-installed)
- **jq** - For JSON parsing: `brew install jq` (macOS) or `apt install jq` (Linux)

### Verify Prerequisites

```bash
java -version    # Should show Java 21+
mvn -version     # Should show Maven 3.9+
docker --version # Should show Docker 20+
docker-compose --version
git --version
curl --version
jq --version
```

## 🚀 Quick Setup (5 Minutes)

### Option 1: Automated Setup (Recommended)

```bash
# 1. Clone the repository
git clone <repository-url>
cd trading-gateway

# 2. Generate secure secrets and create .env
./generate-secrets.sh
# Answer 'y' when prompted to create .env file

# 3. Start all services
docker-compose up -d

# 4. Wait for services to be healthy (30-60 seconds)
docker-compose ps

# 5. Run security tests
./test-security.sh
```

### Option 2: Manual Setup

```bash
# 1. Clone the repository
git clone <repository-url>
cd trading-gateway

# 2. Create .env file
cp .env.example .env

# 3. Generate secrets manually
echo "GATEWAY_SECRET=$(openssl rand -base64 32)" >> .env
echo "JWT_SECRET=$(openssl rand -base64 64)" >> .env
echo "MONGO_PASSWORD=$(openssl rand -base64 32)" >> .env

# 4. Build the project
./mvnw clean package

# 5. Start services
docker-compose up -d

# 6. Test
./test-security.sh
```

## 📝 Detailed Setup

### Step 1: Clone Repository

```bash
git clone <repository-url>
cd trading-gateway
```

### Step 2: Environment Configuration

#### Using the Generator Script (Recommended)

```bash
./generate-secrets.sh
```

This will:
- Generate cryptographically secure secrets
- Create `.env` file with proper values
- Backup existing `.env` if present

#### Manual Configuration

```bash
# Copy example file
cp .env.example .env

# Edit .env file
nano .env  # or use your preferred editor
```

**Required changes:**
- `GATEWAY_SECRET` - Generate: `openssl rand -base64 32`
- `JWT_SECRET` - Generate: `openssl rand -base64 64`
- `MONGO_PASSWORD` - Generate: `openssl rand -base64 32`

**Optional changes:**
- `CORS_ALLOWED_ORIGINS` - Add your frontend URLs
- `SERVER_PORT` - Change if 9000 is in use (default: 9000)
- `LOGGING_LEVEL_*` - Adjust logging verbosity

### Step 3: Build the Application

```bash
# Clean and build
./mvnw clean package

# Skip tests for faster build
./mvnw clean package -DskipTests
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 30.123 s
```

### Step 4: Start Services

#### Using Docker Compose (Recommended)

```bash
# Start all services in background
docker-compose up -d

# View logs
docker-compose logs -f

# Check service status
docker-compose ps
```

Expected services:
- `trading-gateway` - API Gateway (port 9000)
- `trading-auth-service` - Auth Service (port 8081)
- `trading-mongodb` - MongoDB (port 27017)
- `trading-price-prediction` - Mock service (port 8082)
- `trading-portfolio-backtest` - Mock service (port 8083)

#### Running Standalone (Development)

```bash
# Terminal 1: Start MongoDB
docker run -d -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=password123 \
  mongo:7.0

# Terminal 2: Start Auth Service (if available)
# Follow auth service setup instructions

# Terminal 3: Start Gateway
./mvnw spring-boot:run
```

### Step 5: Verify Installation

#### Health Checks

```bash
# Gateway health
curl http://localhost:9000/health

# Expected: {"status":"UP"}

# Auth service health
curl http://localhost:8081/actuator/health

# Expected: {"status":"UP"}
```

#### Run Security Tests

```bash
./test-security.sh
```

Expected output:
```
==========================================
Trading Gateway Security Test Suite
==========================================

✓ PASS: Gateway health check
✓ PASS: User registration
✓ PASS: User login
✓ PASS: Access protected endpoint with valid token
✓ PASS: Reject request without token
✓ PASS: Reject request with invalid token
✓ PASS: Token refresh
✓ PASS: User logout
✓ PASS: Reject blacklisted token
✓ PASS: Gateway logging enabled

Passed: 10
Failed: 0

All tests passed! ✓
```

## 🧪 Testing the Setup

### Manual API Testing

#### 1. Register a User

```bash
curl -X POST http://localhost:9000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user": {
      "id": "...",
      "email": "test@example.com",
      "firstName": "Test",
      "lastName": "User"
    }
  }
}
```

#### 2. Login

```bash
curl -X POST http://localhost:9000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

Save the `accessToken` from the response.

#### 3. Access Protected Endpoint

```bash
# Replace YOUR_TOKEN with the actual token
curl -X GET http://localhost:9000/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🔧 Troubleshooting

### Port Already in Use

```bash
# Check what's using port 9000
lsof -i :9000

# Kill the process
kill -9 <PID>

# Or change port in .env
echo "SERVER_PORT=9001" >> .env
```

### Services Not Starting

```bash
# Check Docker logs
docker-compose logs gateway
docker-compose logs auth-service
docker-compose logs mongodb

# Restart services
docker-compose restart

# Full reset
docker-compose down -v
docker-compose up -d
```

### Authentication Fails

```bash
# Check auth service is running
curl http://localhost:8081/actuator/health

# Check MongoDB is accessible
docker exec -it trading-mongodb mongosh \
  -u admin -p password123 --authenticationDatabase admin

# Check gateway logs
tail -f logs/gateway.log
```

### Build Fails

```bash
# Clean Maven cache
./mvnw clean

# Update dependencies
./mvnw dependency:purge-local-repository

# Rebuild
./mvnw clean package -U
```

## 📚 Next Steps

After successful setup:

1. **Read Documentation**
   - [ARCHITECTURE.md](ARCHITECTURE.md) - Understand the system
   - [SECURITY_IMPLEMENTATION.md](SECURITY_IMPLEMENTATION.md) - Security details
   - [SECRETS_MANAGEMENT.md](SECRETS_MANAGEMENT.md) - Manage secrets

2. **Explore APIs**
   - Review [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md)
   - Test all endpoints with Postman/Insomnia
   - Check [QUICK_REFERENCE.md](QUICK_REFERENCE.md)

3. **Development**
   - Set up your IDE (IntelliJ IDEA / VS Code)
   - Configure code formatting
   - Run tests: `./mvnw test`

4. **Production Preparation**
   - Review [Production Checklist](README.md#production-checklist)
   - Set up monitoring
   - Configure CI/CD

## 🆘 Getting Help

If you encounter issues:

1. **Check Logs**
   ```bash
   # Gateway logs
   tail -f logs/gateway.log
   
   # Docker logs
   docker-compose logs -f
   ```

2. **Verify Configuration**
   ```bash
   # Check .env file
   cat .env
   
   # Verify environment variables
   docker-compose config
   ```

3. **Run Diagnostics**
   ```bash
   # Health checks
   curl http://localhost:9000/health
   curl http://localhost:8081/actuator/health

   # Test connectivity
   docker-compose exec gateway ping auth-service
   ```

4. **Review Documentation**
   - Check README.md for common issues
   - Review TROUBLESHOOTING section
   - Check GitHub issues (if applicable)

## ✅ Setup Checklist

- [ ] Prerequisites installed and verified
- [ ] Repository cloned
- [ ] `.env` file created with secure secrets
- [ ] Project built successfully
- [ ] Docker services running
- [ ] Health checks passing
- [ ] Security tests passing
- [ ] Can register and login users
- [ ] Protected endpoints working
- [ ] Logs being generated

Congratulations! Your Trading Gateway is now set up and ready to use! 🎉

