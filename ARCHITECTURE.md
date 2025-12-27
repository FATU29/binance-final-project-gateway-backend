# Security Architecture - API Gateway

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              Client Layer                                │
│  (Web Browser, Mobile App, Third-party Services)                        │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 │ HTTPS
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         Load Balancer / CDN                              │
│                    (SSL Termination, DDoS Protection)                    │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          API Gateway (Port 9000)                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  Filter Chain (Ordered Execution):                                │  │
│  │                                                                    │  │
│  │  [1] RequestLoggingFilter                                         │  │
│  │      • Log request details (IP, method, path, timestamp)          │  │
│  │      • Extract client IP from proxy headers                       │  │
│  │                                                                    │  │
│  │  [2] GatewayValidationFilter                                      │  │
│  │      • Add X-Gateway-Signature header                             │  │
│  │      • Add X-Request-Id for tracing                               │  │
│  │      • Add X-Gateway-Timestamp                                    │  │
│  │                                                                    │  │
│  │  [3] AuthenticationFilter (for protected routes)                  │  │
│  │      • Extract Authorization header                               │  │
│  │      • Validate token with Auth Service (/me endpoint)            │  │
│  │      • Add user context headers (X-User-Id, X-User-Email, etc.)   │  │
│  │      • Return 401 if invalid/expired/blacklisted                  │  │
│  │                                                                    │  │
│  │  [4] CircuitBreaker                                               │  │
│  │      • Protect against cascading failures                         │  │
│  │      • Fallback to error response if service down                 │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                           │
│  Routes Configuration:                                                   │
│  • Public: /auth/register, /auth/login, /auth/refresh-token             │
│  • Protected: /auth/me, /auth/logout, /predictions/**, /backtest/**     │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                 ┌───────────────┼───────────────┐
                 │               │               │
                 ▼               ▼               ▼
┌──────────────────────┐ ┌─────────────┐ ┌─────────────────┐
│  Auth Service        │ │  Price      │ │  Portfolio      │
│  (Port 8081)         │ │  Prediction │ │  Backtest       │
│                      │ │  Service    │ │  Service        │
│  Endpoints:          │ │  (8082)     │ │  (8083)         │
│  • /register         │ │             │ │                 │
│  • /login            │ │  Protected  │ │  Protected      │
│  • /refresh-token    │ │  Routes     │ │  Routes         │
│  • /me (validate)    │ │             │ │                 │
│  • /logout           │ │  Validates: │ │  Validates:     │
│  • /change-password  │ │  • Gateway  │ │  • Gateway      │
│                      │ │    Signature│ │    Signature    │
│  Features:           │ │  • User     │ │  • User         │
│  • JWT generation    │ │    Context  │ │    Context      │
│  • Token blacklist   │ │             │ │                 │
│  • User management   │ │             │ │                 │
└──────────┬───────────┘ └─────────────┘ └─────────────────┘
           │
           ▼
┌──────────────────────┐
│  MongoDB             │
│                      │
│  Collections:        │
│  • users             │
│  • token_blacklist   │
│  • refresh_tokens    │
└──────────────────────┘
```

## Security Layers

### Layer 1: Network Security
- **Kubernetes NetworkPolicies**: Block direct access to internal services
- **Service Mesh (Optional)**: mTLS for service-to-service communication
- **Private Network**: Internal services not exposed to internet

### Layer 2: Gateway Security
- **Single Entry Point**: All external traffic goes through gateway
- **Gateway Signature**: Unique header to verify requests came through gateway
- **Request Validation**: Validate all incoming requests
- **Rate Limiting** (recommended): Prevent abuse

### Layer 3: Authentication & Authorization
- **JWT Validation**: Every protected request validates token with auth service
- **Token Blacklist**: Immediate revocation on logout
- **Short-lived Tokens**: Access tokens expire in 1 hour
- **Refresh Token Flow**: Long-lived sessions without compromising security

### Layer 4: Audit & Monitoring
- **Comprehensive Logging**: All requests logged with user context
- **Request Tracing**: Unique request ID for tracking
- **Security Metrics**: Monitor authentication failures, unusual patterns
- **Alerting**: Real-time alerts for security events

## OAuth2 + JWT Flow

### 1. User Registration/Login
```
Client                  Gateway                 Auth Service
  │                       │                          │
  │──[POST /register]────>│                          │
  │                       │──[Forward]──────────────>│
  │                       │                          │──[Create User]
  │                       │                          │──[Generate JWT]
  │                       │<─[Access + Refresh]──────│
  │<─[Tokens]─────────────│                          │
```

### 2. Accessing Protected Resource
```
Client                  Gateway                 Auth Service        Internal Service
  │                       │                          │                     │
  │──[GET /predictions]──>│                          │                     │
  │   + Bearer Token      │                          │                     │
  │                       │──[Validate Token]───────>│                     │
  │                       │   (Call /me endpoint)    │                     │
  │                       │                          │──[Check DB]         │
  │                       │                          │──[Check Blacklist]  │
  │                       │<─[User Data]─────────────│                     │
  │                       │                                                │
  │                       │──[Forward + User Headers]────────────────────>│
  │                       │   + X-Gateway-Signature                        │
  │                       │   + X-User-Id                                  │
  │                       │   + X-User-Email                               │
  │                       │                                                │──[Validate]
  │                       │                                                │──[Process]
  │                       │<─[Response]────────────────────────────────────│
  │<─[Response]───────────│                                                │
```

### 3. Token Refresh
```
Client                  Gateway                 Auth Service
  │                       │                          │
  │──[POST /refresh]─────>│                          │
  │   + Refresh Token     │──[Forward]──────────────>│
  │                       │                          │──[Validate Refresh]
  │                       │                          │──[Generate New JWT]
  │                       │<─[New Tokens]────────────│
  │<─[New Tokens]─────────│                          │
```

### 4. Logout (Token Revocation)
```
Client                  Gateway                 Auth Service
  │                       │                          │
  │──[POST /logout]──────>│                          │
  │   + Bearer Token      │──[Validate + Forward]───>│
  │                       │                          │──[Add to Blacklist]
  │                       │<─[Success]───────────────│
  │<─[Success]────────────│                          │
  │                       │                          │
  │──[Try to use token]──>│──[Validate]─────────────>│
  │                       │                          │──[Check Blacklist]
  │                       │<─[401 Forbidden]─────────│
  │<─[401 Forbidden]──────│                          │
```

## Zero-Trust Implementation

### Principle: "Never Trust, Always Verify"

1. **Gateway doesn't trust tokens directly**
   - Validates every token with auth service
   - No local JWT verification (prevents old token abuse)

2. **Internal services don't trust gateway**
   - Validate X-Gateway-Signature header
   - Check user context headers
   - Implement own authorization logic

3. **Auth service doesn't trust anything**
   - Validates credentials on every request
   - Checks token blacklist
   - Verifies token expiration

4. **Network doesn't trust services**
   - NetworkPolicies restrict traffic
   - Only allow gateway → service communication
   - Block direct external access

## Addressing Tình huống 3 Attack Vectors

### Attack Vector 1: Direct Service Access
**Attack**: Bypass gateway and call internal service directly
**Defense**:
- NetworkPolicy blocks external access to internal services
- Services validate X-Gateway-Signature header
- Missing signature = reject request

### Attack Vector 2: Old Token Reuse
**Attack**: Use stolen token from 1 week ago
**Defense**:
- Token validation on every request checks expiration
- Short token lifetime (1 hour)
- Blacklist prevents use of logged-out tokens

### Attack Vector 3: Fake Client
**Attack**: Malicious client with valid token
**Defense**:
- User context headers allow service-level authorization
- Comprehensive logging tracks all actions
- Rate limiting prevents abuse
- Anomaly detection identifies suspicious patterns

### Attack Vector 4: Token Theft
**Attack**: Steal token and use from different location
**Defense**:
- Short token lifetime limits exposure window
- Refresh token rotation
- Monitor for unusual access patterns
- Optional: IP-based validation

## Deployment Checklist

- [ ] Change GATEWAY_SECRET to secure random value
- [ ] Configure JWT_SECRET in auth service
- [ ] Set up MongoDB with authentication
- [ ] Deploy Kubernetes NetworkPolicies
- [ ] Configure CORS for production origins
- [ ] Set up centralized logging (ELK/Loki)
- [ ] Configure monitoring (Prometheus/Grafana)
- [ ] Set up alerting for security events
- [ ] Enable HTTPS/TLS
- [ ] Configure rate limiting
- [ ] Set up backup and disaster recovery
- [ ] Perform security audit and penetration testing

