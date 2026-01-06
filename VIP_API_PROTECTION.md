# VIP Authorization - Backend Protection

## ✅ Đã Fix: API AI Giờ Đã Được Bảo Vệ

### 🚨 Vấn Đề Trước Đây:

User STANDARD vẫn có thể gọi API AI bằng cách:

```javascript
// Bypass frontend VipGuard
fetch("/ai/sentiment/123", {
  headers: { Authorization: "Bearer token" },
});
```

**Nguyên nhân:** Frontend chỉ ẩn UI, không kiểm soát API calls thực sự.

---

## ✅ Giải Pháp Đã Áp Dụng:

### 1. **API Endpoints Đã Được Cập Nhật**

**File:** `fe/config/api-endpoints.ts`

```typescript
news: {
  // Đã thêm /api/v1 prefix
  analyzeSentiment: (id: string) => `/api/v1/ai/sentiment/${id}`,
  getAnalyzedNews: "/api/v1/ai/analyzed-news",
}
```

### 2. **Gateway Routes Đã Được Bảo Vệ**

**File:** `binance-final-project-gateway-backend/src/main/resources/application.yml`

```yaml
# AI Analysis Service - VIP ONLY
- id: ai-analysis-service
  uri: ${crawl.service.url:http://localhost:9002}
  predicates:
    - Path=/api/v1/ai/**
  filters:
    - AuthenticationFilter # ✅ Kiểm tra user đã login
    - VipAuthorizationFilter # ✅ Kiểm tra user có VIP không
    - CircuitBreaker

# Analytics Service - VIP ONLY
- id: analytics-service
  uri: ${crawl.service.url:http://localhost:9002}
  predicates:
    - Path=/api/v1/analytics/**
  filters:
    - AuthenticationFilter # ✅ Kiểm tra user đã login
    - VipAuthorizationFilter # ✅ Kiểm tra user có VIP không
    - CircuitBreaker
```

---

## 🔒 Cách Hoạt Động:

### Request Flow:

```
Frontend (STANDARD user)
    ↓
    | GET /api/v1/ai/sentiment/123
    | Authorization: Bearer <token>
    ↓
Gateway (Port 9000)
    ↓
    | 1. AuthenticationFilter
    |    - Call auth service /me
    |    - Get user data including accountType
    |    - Set header: X-User-AccountType: STANDARD
    ↓
    | 2. VipAuthorizationFilter
    |    - Check X-User-AccountType header
    |    - If NOT "VIP" → ❌ RETURN 403 FORBIDDEN
    |    - If "VIP" → ✅ Continue
    ↓
AI Service (Port 9002)
    ↓
Response: 403 Forbidden
{
  "message": "Access denied. VIP account required to access AI model-based analyses."
}
```

---

## 🧪 Test Scenarios:

### Test 1: STANDARD User Calls AI API

```bash
# User đăng nhập với STANDARD account
curl -H "Authorization: Bearer <standard-token>" \
     http://localhost:9000/api/v1/ai/sentiment/123

# Response:
HTTP/1.1 403 Forbidden
{
  "message": "Access denied. VIP account required to access AI model-based analyses."
}
```

### Test 2: VIP User Calls AI API

```bash
# User đăng nhập với VIP account
curl -H "Authorization: Bearer <vip-token>" \
     http://localhost:9000/api/v1/ai/sentiment/123

# Response:
HTTP/1.1 200 OK
{
  "sentiment": "positive",
  "score": 0.85,
  ...
}
```

### Test 3: Direct Backend Access (Should Fail)

```bash
# Thử bypass gateway, call trực tiếp backend
curl http://localhost:9002/api/v1/ai/sentiment/123

# ❌ Should be blocked by network policy
# Connection refused / timeout
```

---

## 📋 Protected Endpoints:

### VIP-Only Endpoints (403 for STANDARD users):

✅ `/api/v1/ai/**` - All AI analysis endpoints

- `/api/v1/ai/sentiment/{id}` - Sentiment analysis
- `/api/v1/ai/analyzed-news` - AI-analyzed news
- `/api/v1/ai/causal-analysis` - Causal analysis

✅ `/api/v1/analytics/**` - Advanced analytics

- `/api/v1/analytics/trends` - Trend analysis
- `/api/v1/analytics/predictions` - Predictions

### Public Endpoints (All users):

✅ `/api/v1/news/**` - News listing (no AI analysis)
✅ `/api/v1/auth/**` - Authentication
✅ Charts & Price data

---

## 🛡️ Security Layers:

1. **Frontend (UI Layer)**
   - VipGuard component hides AI features
   - Good UX, but can be bypassed
2. **Gateway (API Layer)** ⭐ **MAIN PROTECTION**
   - AuthenticationFilter validates token
   - VipAuthorizationFilter checks accountType
   - Cannot be bypassed
3. **Network Policy (Infrastructure Layer)**
   - Block direct access to backend services
   - All traffic must go through gateway

---

## ✅ Checklist:

- [x] Frontend endpoints updated with `/api/v1` prefix
- [x] Gateway routes have AuthenticationFilter
- [x] Gateway routes have VipAuthorizationFilter
- [x] VipAuthorizationFilter implemented correctly
- [x] UserData DTO includes accountType field
- [x] AuthenticationFilter sets X-User-AccountType header
- [x] Error messages are clear for users

---

## 🎯 Result:

**GIỜ ĐÂY STANDARD USER KHÔNG THỂ GỌI API AI NỮA!**

- Frontend VipGuard: Ẩn UI
- Gateway Filter: Chặn API calls
- Network Policy: Chặn direct access

**3 layers of protection!** 🛡️🛡️🛡️
