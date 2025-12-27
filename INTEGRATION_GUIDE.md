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

## 📋 API Endpoints Quick Reference

| Action | Method | Endpoint | Auth Required |
|--------|--------|----------|---------------|
| Register | POST | `/api/v1/auth/register` | ❌ No |
| Login | POST | `/api/v1/auth/login` | ❌ No |
| Refresh Token | POST | `/api/v1/auth/refresh-token` | ❌ No |
| Get Profile | GET | `/api/v1/auth/me` | ✅ Yes |
| Change Password | POST | `/api/v1/auth/change-password` | ✅ Yes |
| Logout | POST | `/api/v1/auth/logout` | ✅ Yes |

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
      "lastName": "Doe"
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
      "lastName": "Doe"
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

---

## ❌ Error Responses

### HTTP Status Codes
| Status | Description |
|--------|-------------|
| 400 | Bad Request - Validation errors, email already exists |
| 401 | Unauthorized - Invalid credentials, expired/invalid token |
| 403 | Forbidden - Blacklisted token (after logout) |
| 404 | Not Found - User not found |
| 503 | Service Unavailable - Auth service is down |

### Error Response Format
```json
{
  "success": false,
  "message": "Error description here",
  "timestamp": "2025-12-27T10:00:00.000Z"
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
};
```

### React Hook Example

```typescript
import { useState, useEffect, createContext, useContext } from 'react';
import { authApi } from './api';

interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
}

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
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

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
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

