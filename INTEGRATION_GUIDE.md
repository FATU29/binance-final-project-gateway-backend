# Frontend Integration Guide - Trading Gateway API

This guide helps frontend developers integrate with the Trading Gateway API.

## 🚀 Quick Start

### Base URL
| Environment | URL |
|-------------|-----|
| **Local Development** | `http://localhost:9000` |
| **Docker** | `http://localhost:9000` |
| **Production** | `https://api.yourdomain.com` |

### Service Configuration
| Property | Value |
|----------|-------|
| **Gateway Port** | `9000` |
| **API Prefix** | `/api/v1` |
| **Token Type** | Bearer JWT |
| **Access Token Expiration** | 1 hour (3600000ms) |
| **Refresh Token Expiration** | 24 hours (86400000ms) |

## 👤 Account Types

| Type | Description | Permissions |
|------|-------------|-------------|
| **STANDARD** | Default account type for new users | Can view charts, news, predictions, backtest |
| **VIP** | Premium account type | All STANDARD features + AI model-based analyses (`/api/v1/ai/**`, `/api/v1/analytics/**`) |

### VIP-Only Features
The following endpoints are **restricted to VIP accounts only**:
- `/api/v1/ai/**` - AI Analysis endpoints (sentiment analysis, causal analysis, AI-analyzed news)
- `/api/v1/analytics/**` - Advanced analytics endpoints

**Standard users attempting to access VIP endpoints will receive a `403 Forbidden` error.**

## 📋 API Endpoints Quick Reference

### Authentication Endpoints
| Action | Method | Endpoint | Auth Required | Account Type |
|--------|--------|----------|---------------|--------------|
| Register | POST | `/api/v1/auth/register` | ❌ No | - |
| Login | POST | `/api/v1/auth/login` | ❌ No | - |
| Refresh Token | POST | `/api/v1/auth/refresh-token` | ❌ No | - |
| Get Profile | GET | `/api/v1/auth/me` | ✅ Yes | Any |
| Change Password | POST | `/api/v1/auth/change-password` | ✅ Yes | Any |
| Logout | POST | `/api/v1/auth/logout` | ✅ Yes | Any |
| Upgrade Account | PUT | `/api/v1/auth/upgrade-account` | ✅ Yes | Any |

### AI Analysis Endpoints (VIP Only)
| Action | Method | Endpoint | Auth Required | Account Type |
|--------|--------|----------|---------------|--------------|
| AI Analysis | GET/POST | `/api/v1/ai/**` | ✅ Yes | 🌟 VIP Only |
| Analytics | GET/POST | `/api/v1/analytics/**` | ✅ Yes | 🌟 VIP Only |

### Other Protected Endpoints
| Action | Method | Endpoint | Auth Required | Account Type |
|--------|--------|----------|---------------|--------------|
| Predictions | GET/POST | `/api/v1/predictions/**` | ✅ Yes | Any |
| Backtest | GET/POST | `/api/v1/backtest/**` | ✅ Yes | Any |
| News | GET | `/api/v1/news/**` | ❌ No | Any |

---

## 🔓 Public Endpoints (No Authentication)

### 1. Register User
**POST** `http://localhost:9000/api/v1/auth/register`

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "user": {
      "id": "507f1f77bcf86cd799439011",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "accountType": "STANDARD"
    }
  },
  "timestamp": "2025-12-27T10:00:00.000Z"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:9000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Login
**POST** `http://localhost:9000/api/v1/auth/login`

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "user": {
      "id": "507f1f77bcf86cd799439011",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "accountType": "STANDARD"
    }
  },
  "timestamp": "2025-12-27T10:00:00.000Z"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:9000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!"
  }'
```

### 3. Refresh Token
**POST** `http://localhost:9000/api/v1/auth/refresh-token`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...(new)",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...(new)",
    "tokenType": "Bearer",
    "expiresIn": 3600000
  },
  "timestamp": "2025-12-27T10:00:00.000Z"
}
```

---

## 🔐 Protected Endpoints (Authentication Required)

> **⚠️ Important:** All protected endpoints require the `Authorization` header:
> ```
> Authorization: Bearer <accessToken>
> ```

### 4. Get Current User Profile
**GET** `http://localhost:9000/api/v1/auth/me`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "enabled": true,
    "accountType": "STANDARD",
    "createdAt": "2025-12-27T10:00:00.000Z",
    "updatedAt": "2025-12-27T10:00:00.000Z"
  },
  "timestamp": "2025-12-27T10:00:00.000Z"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:9000/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 5. Change Password
**POST** `http://localhost:9000/api/v1/auth/change-password`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json
```

**Request:**
```json
{
  "currentPassword": "SecurePass123!",
  "newPassword": "NewSecurePass456!",
  "confirmPassword": "NewSecurePass456!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Password changed successfully",
  "timestamp": "2025-12-27T10:00:00.000Z"
}
```

### 6. Logout
**POST** `http://localhost:9000/api/v1/auth/logout`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "timestamp": "2025-12-27T10:00:00.000Z"
}
```

> **Note:** After logout, the access token is blacklisted and cannot be used again.

### 7. Upgrade Account
**PUT** `http://localhost:9000/api/v1/auth/upgrade-account`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json
```

**Request:**
```json
{
  "accountType": "VIP"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Account upgraded successfully",
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "enabled": true,
    "accountType": "VIP",
    "createdAt": "2025-12-27T10:00:00.000Z",
    "updatedAt": "2025-12-27T10:00:00.000Z"
  },
  "timestamp": "2025-12-27T10:00:00.000Z"
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:9000/api/v1/auth/upgrade-account \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"accountType": "VIP"}'
```

> **Note:** Account types are `STANDARD` (default) and `VIP`. VIP accounts can access AI model-based analyses.

---

## 🌟 VIP-Only Endpoints (AI Analysis & Analytics)

> **⚠️ Important:** These endpoints require a **VIP account**. Standard accounts will receive a `403 Forbidden` error.

### VIP Protected Endpoints

| Endpoint Pattern | Description |
|------------------|-------------|
| `/api/v1/ai/**` | AI Analysis endpoints |
| `/api/v1/analytics/**` | Advanced Analytics endpoints |

### AI Analysis Endpoints
**Base Path:** `http://localhost:9000/api/v1/ai/**`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/ai/sentiment/{id}` | GET | Get sentiment analysis for a news item |
| `/api/v1/ai/analyzed-news` | GET | Get AI-analyzed news list |
| `/api/v1/ai/causal-analysis` | GET | Get causal analysis data |

### Analytics Endpoints
**Base Path:** `http://localhost:9000/api/v1/analytics/**`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/analytics/trends` | GET | Get trend analysis |
| `/api/v1/analytics/predictions` | GET | Get AI predictions |

### Headers Required
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Access Denied Response (403 Forbidden) - For Standard Accounts
```json
{
  "success": false,
  "message": "Access denied. VIP account required to access AI model-based analyses.",
  "timestamp": "2026-01-03T10:00:00.000Z"
}
```

### Successful Response Example (VIP Account)
```bash
curl -X GET http://localhost:9000/api/v1/ai/analyzed-news \
  -H "Authorization: Bearer YOUR_VIP_ACCESS_TOKEN"
```

```json
{
  "success": true,
  "data": [
    {
      "id": "123",
      "title": "Bitcoin reaches new high",
      "sentiment": "positive",
      "score": 0.85,
      "analyzedAt": "2026-01-03T10:00:00.000Z"
    }
  ],
  "timestamp": "2026-01-03T10:00:00.000Z"
}
```

> **Tip:** To access AI analysis endpoints, first upgrade your account to VIP using the `/api/v1/auth/upgrade-account` endpoint.

---

## 🔄 VIP Access Flow

```
1. User registers/logs in → Gets STANDARD account
2. User calls /api/v1/ai/** → Gets 403 Forbidden
3. User upgrades account → PUT /api/v1/auth/upgrade-account {"accountType": "VIP"}
4. User calls /api/v1/ai/** → Gets 200 OK with data
```

### How VIP Protection Works

The gateway implements a **3-layer security model**:

1. **AuthenticationFilter** - Validates JWT token by calling auth service `/me` endpoint
2. **VipAuthorizationFilter** - Checks if user's `accountType` is "VIP"
3. **CircuitBreaker** - Provides fallback when downstream services are unavailable

```
Request Flow:
┌─────────────────┐
│   Frontend      │
│  (with token)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Gateway      │
│   (Port 9000)   │
├─────────────────┤
│ 1. AuthFilter   │ ──► Validates token with Auth Service
│    ↓            │     Sets X-User-AccountType header
│ 2. VipFilter    │ ──► Checks accountType == "VIP"
│    ↓            │     Returns 403 if not VIP
│ 3. CircuitBreaker│ ──► Handles service failures
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  AI Service     │
│  (Port 9002)    │
└─────────────────┘
```

---

## ❌ Error Responses

### HTTP Status Codes
| Status | Description |
|--------|-------------|
| 400 | Bad Request - Validation errors, email already exists, account already VIP |
| 401 | Unauthorized - Invalid credentials, expired/invalid token, missing Authorization header |
| 403 | Forbidden - Blacklisted token (after logout) OR VIP access required for AI endpoints |
| 404 | Not Found - User not found |
| 503 | Service Unavailable - Backend service is down (only for server errors 500-504) |

### Error Response Format
```json
{
  "success": false,
  "message": "Error description here",
  "timestamp": "2026-01-03T10:00:00.000Z"
}
```

### Common Error Messages
| Error | Cause | Solution |
|-------|-------|----------|
| `Email already exists` | Registration with existing email | Use different email or login |
| `Invalid credentials` | Wrong email/password | Check credentials |
| `Missing or invalid Authorization header` | No Bearer token | Add Authorization header |
| `Invalid or expired token` | Token expired or invalid | Refresh token or re-login |
| `Authentication service is temporarily unavailable` | Auth service down | Retry later |
| `Access denied. VIP account required...` | Standard account accessing VIP endpoint | Upgrade to VIP account |
| `Account is already VIP` | Trying to upgrade an already VIP account | No action needed |

---

## 💻 Frontend Implementation Examples

### JavaScript/TypeScript (Axios)

```typescript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:9000';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token refresh on 401
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const { data } = await axios.post(`${API_BASE_URL}/api/v1/auth/refresh-token`, {
            refreshToken,
          });
          localStorage.setItem('accessToken', data.data.accessToken);
          localStorage.setItem('refreshToken', data.data.refreshToken);
          error.config.headers.Authorization = `Bearer ${data.data.accessToken}`;
          return api.request(error.config);
        } catch {
          // Refresh failed, redirect to login
          localStorage.clear();
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

// API functions
export const authApi = {
  register: (data: { email: string; password: string; firstName: string; lastName: string }) =>
    api.post('/api/v1/auth/register', data),

  login: (data: { email: string; password: string }) =>
    api.post('/api/v1/auth/login', data),

  getProfile: () => api.get('/api/v1/auth/me'),

  changePassword: (data: { currentPassword: string; newPassword: string; confirmPassword: string }) =>
    api.post('/api/v1/auth/change-password', data),

  logout: () => api.post('/api/v1/auth/logout'),

  upgradeAccount: (accountType: 'STANDARD' | 'VIP') =>
    api.put('/api/v1/auth/upgrade-account', { accountType }),
};

// AI Analysis API (VIP only)
export const aiApi = {
  getAnalysis: () => api.get('/api/v1/ai/analysis'),
  // Add more AI endpoints as needed
};
```

### React Hook Example

```typescript
import { useState, useEffect, createContext, useContext } from 'react';
import { authApi } from './api';

type AccountType = 'STANDARD' | 'VIP';

interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  accountType: AccountType;
}

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  upgradeToVip: () => Promise<void>;
  isAuthenticated: boolean;
  isVip: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      authApi.getProfile()
        .then(({ data }) => setUser(data.data))
        .catch(() => localStorage.clear());
    }
  }, []);

  const login = async (email: string, password: string) => {
    const { data } = await authApi.login({ email, password });
    localStorage.setItem('accessToken', data.data.accessToken);
    localStorage.setItem('refreshToken', data.data.refreshToken);
    setUser(data.data.user);
  };

  const logout = async () => {
    await authApi.logout();
    localStorage.clear();
    setUser(null);
  };

  const upgradeToVip = async () => {
    const { data } = await authApi.upgradeAccount('VIP');
    setUser(data.data);
  };

  return (
    <AuthContext.Provider value={{
      user,
      login,
      logout,
      upgradeToVip,
      isAuthenticated: !!user,
      isVip: user?.accountType === 'VIP'
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

### VIP Feature Guard Component

```typescript
import { useAuth } from './AuthContext';

interface VipGuardProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export const VipGuard: React.FC<VipGuardProps> = ({ children, fallback }) => {
  const { isVip } = useAuth();

  if (!isVip) {
    return fallback || (
      <div className="vip-required">
        <h3>🌟 VIP Access Required</h3>
        <p>Upgrade your account to access AI model-based analyses.</p>
        <button onClick={() => window.location.href = '/upgrade'}>
          Upgrade to VIP
        </button>
      </div>
    );
  }

  return <>{children}</>;
};

// Usage example:
// <VipGuard>
//   <AIAnalysisComponent />
// </VipGuard>
```

---

## 🔧 CORS Configuration

The gateway is configured to allow requests from:
- `http://localhost:3000` (default React dev server)

To add more origins, set the `CORS_ALLOWED_ORIGINS` environment variable:
```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

---

## 📊 JWT Token Details

| Property | Value |
|----------|-------|
| Algorithm | HS512 |
| Access Token Lifetime | 1 hour |
| Refresh Token Lifetime | 24 hours |
| Token Claims | `sub` (email), `iat` (issued at), `exp` (expiration) |

### Token Storage Recommendations
- **Access Token:** Store in memory or `localStorage`
- **Refresh Token:** Store in `localStorage` or HTTP-only cookie
- **Never** store tokens in `sessionStorage` for persistent sessions

---

## 🧪 Testing with Postman

Import the Postman collection from:
- `Trading-Gateway-API.postman_collection.json`
- `Trading-Gateway-Local.postman_environment.json`

The collection includes automatic token extraction from login/register responses.

