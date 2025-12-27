# Trading Gateway - Secure API Gateway

A production-ready API Gateway implementing **Zero-Trust Security** architecture for microservices, addressing the security requirements from **Tình huống 3** (Security Scenario 3).

## 🎯 Project Overview

This API Gateway serves as the single entry point for all client requests to the trading platform's microservices. It implements comprehensive security measures including:

- ✅ **JWT Token Validation** on every request
- ✅ **Token Blacklist** support for immediate revocation
- ✅ **Gateway Signature** to prevent direct service access
- ✅ **Comprehensive Audit Logging** for security monitoring
- ✅ **Circuit Breaker** for resilience
- ✅ **Zero-Trust Architecture** - never trust, always verify

## 🏗️ Architecture

```
Client → Load Balancer → API Gateway → [Auth Service, Internal Services]
                            ↓
                    [Logging, Validation, Authentication]
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed architecture diagrams and flow charts.

## 🔒 Security Features

### Addressing Tình huống 3 Security Issues

| Security Issue | Solution Implemented |
|----------------|---------------------|
| **Direct service access** | Gateway signature + NetworkPolicies |
| **Old tokens (1 week)** | Token validation on every request + blacklist |
| **No authentication logs** | Comprehensive request/response logging |
| **Fake clients** | User context propagation + audit trail |

See [SECURITY_IMPLEMENTATION.md](SECURITY_IMPLEMENTATION.md) for detailed security analysis.

## 🚀 Quick Start

> **📖 For detailed setup instructions, see [SETUP_GUIDE.md](SETUP_GUIDE.md)**

### Prerequisites
- Java 21+
- Docker & Docker Compose (for local testing)
- OpenSSL (for generating secrets)
- Maven wrapper included (`./mvnw`) - no need to install Maven separately

### Local Development

1. **Clone the repository**
```bash
git clone <repository-url>
cd trading-gateway
```

2. **Generate secure secrets**
```bash
./generate-secrets.sh
```
This will:
- Generate secure random secrets for `GATEWAY_SECRET`, `JWT_SECRET`, and `MONGO_PASSWORD`
- Optionally create `.env` file with the generated secrets
- **Important**: Never commit `.env` to version control!

3. **Build the project**
```bash
./mvnw clean package -DskipTests
```

4. **Run with Docker Compose**
```bash
docker-compose up -d
```

5. **Access the gateway**
- Gateway: http://localhost:9000
- Auth Service: http://localhost:8081
- Health Check: http://localhost:9000/health

### Running Standalone

```bash
# Option 1: Use .env file (recommended)
# Make sure .env file exists with proper values
./mvnw spring-boot:run

# Option 2: Set environment variables manually
export AUTH_SERVICE_URL=http://localhost:8081
export GATEWAY_SECRET=your-secure-secret
export PRICE_PREDICTION_SERVICE_URL=http://localhost:8082
export PORTFOLIO_BACKTEST_SERVICE_URL=http://localhost:8083

# Run the application
./mvnw spring-boot:run
```

## 📋 API Routes

### Public Endpoints (No Authentication)
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh-token` - Refresh access token

### Protected Endpoints (Requires Authentication)
- `GET /api/v1/auth/me` - Get current user profile
- `POST /api/v1/auth/change-password` - Change password
- `POST /api/v1/auth/logout` - Logout (blacklist token)
- `GET /api/v1/predictions/**` - Price prediction service
- `GET /api/v1/backtest/**` - Portfolio backtest service

## 🧪 Testing

### Test Authentication Flow

```bash
# 1. Register a new user
curl -X POST http://localhost:9000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

# 2. Login
curl -X POST http://localhost:9000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Save the accessToken from response

# 3. Access protected endpoint
curl -X GET http://localhost:9000/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 4. Logout
curl -X POST http://localhost:9000/api/v1/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 5. Try to use the same token (should fail)
curl -X GET http://localhost:9000/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 🔧 Configuration

### Environment Variables

All configuration is managed through environment variables. See `.env.example` for all available options.

**Key Variables:**

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `GATEWAY_SECRET` | Secret for gateway signature | - | ✅ Yes |
| `AUTH_SERVICE_URL` | Auth service base URL | `http://localhost:8081` | ✅ Yes |
| `PRICE_PREDICTION_SERVICE_URL` | Price prediction service URL | `http://localhost:8082` | No |
| `PORTFOLIO_BACKTEST_SERVICE_URL` | Portfolio backtest service URL | `http://localhost:8083` | No |
| `JWT_SECRET` | JWT signing secret (for auth service) | - | ✅ Yes |
| `MONGO_PASSWORD` | MongoDB password | `password123` | ✅ Yes |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins (comma-separated) | `http://localhost:3000` | No |
| `SERVER_PORT` | Gateway server port | `9000` | No |
| `LOGGING_LEVEL_ROOT` | Root logging level | `INFO` | No |
| `LOGGING_LEVEL_GATEWAY` | Gateway logging level | `DEBUG` | No |

**Setup:**

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Generate secure secrets:
   ```bash
   ./generate-secrets.sh
   ```

3. Or manually update `.env` with your values:
   ```bash
   # Generate secrets manually
   GATEWAY_SECRET=$(openssl rand -base64 32)
   JWT_SECRET=$(openssl rand -base64 64)
   ```

### Application Configuration

See `src/main/resources/application.yml` for detailed configuration including:
- Route definitions
- Circuit breaker settings
- CORS configuration
- Logging levels

**Note:** `application.yml` uses environment variables, so you don't need to edit it directly.

## 📊 Monitoring

### Health Checks
- Gateway: `http://localhost:9000/health`
- Actuator: `http://localhost:9000/actuator/health`

### Logs
Logs are written to:
- Console (stdout)
- File: `logs/gateway.log`

Log format includes:
- Timestamp
- Request method and path
- Client IP address
- Response status
- Processing duration
- User ID (if authenticated)

## 🚢 Deployment

### Kubernetes

1. **Apply NetworkPolicies** (Zero-Trust)
```bash
kubectl apply -f k8s/network-policy.yaml
```

2. **Deploy the gateway**
```bash
kubectl apply -f k8s/deployment.yaml
```

See [k8s/](k8s/) directory for Kubernetes manifests.

### Production Checklist

**Security:**
- [ ] Generate secure secrets using `./generate-secrets.sh`
- [ ] Use Kubernetes Secrets (not `.env` files)
- [ ] Change all default passwords
- [ ] Configure proper CORS origins
- [ ] Enable HTTPS/TLS
- [ ] Set up rate limiting
- [ ] Perform security audit
- [ ] Review [SECRETS_MANAGEMENT.md](SECRETS_MANAGEMENT.md)

**Infrastructure:**
- [ ] Set up centralized logging (ELK/Loki)
- [ ] Configure monitoring (Prometheus/Grafana)
- [ ] Configure backup and disaster recovery
- [ ] Set up auto-scaling
- [ ] Configure health checks

**Documentation:**
- [ ] Update service URLs in `.env`
- [ ] Document deployment procedures
- [ ] Create runbooks for incidents

## 📚 Documentation

### Getting Started
- [SETUP_GUIDE.md](SETUP_GUIDE.md) - **Complete setup instructions (START HERE)**
- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Quick reference for common tasks
- [SECRETS_MANAGEMENT.md](SECRETS_MANAGEMENT.md) - Secrets and environment variables guide

### Architecture & Security
- [ARCHITECTURE.md](ARCHITECTURE.md) - Detailed architecture and flow diagrams
- [SECURITY_IMPLEMENTATION.md](SECURITY_IMPLEMENTATION.md) - Security analysis and implementation
- [TINH_HUONG_3_ANALYSIS.md](TINH_HUONG_3_ANALYSIS.md) - Vietnamese security analysis

### Integration
- [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) - Auth service integration guide
- [REQUIREMENT.md](REQUIREMENT.md) - Original requirements (Vietnamese)

## 🛠️ Technology Stack

- **Spring Boot 4.0.0** - Application framework
- **Spring Cloud Gateway** - Reactive API Gateway
- **Spring WebFlux** - Reactive web framework
- **Resilience4j** - Circuit breaker
- **Lombok** - Reduce boilerplate code
- **Java 21** - Programming language

## 📝 License

This project is part of a university assignment for HCMUS KTPM course.

## 👥 Contributors

- Your Team Name
- Course: KTPM - HCMUS

## 🤝 Contributing

This is an educational project. For improvements or suggestions, please create an issue or pull request.

