#!/bin/bash

# Security Testing Script for Trading Gateway
# This script tests all security features implemented for Tình huống 3

set -e

# Load environment variables from .env if it exists
if [ -f ".env" ]; then
    echo "Loading environment variables from .env..."
    export $(cat .env | grep -v '^#' | xargs)
elif [ -f ".env.development" ]; then
    echo "Loading environment variables from .env.development..."
    export $(cat .env.development | grep -v '^#' | xargs)
fi

GATEWAY_URL="${GATEWAY_URL:-http://localhost:${SERVER_PORT:-9000}}"
AUTH_URL="${AUTH_URL:-${AUTH_SERVICE_URL:-http://localhost:8081}}"

echo "=========================================="
echo "Trading Gateway Security Test Suite"
echo "=========================================="
echo ""
echo "Gateway URL: $GATEWAY_URL"
echo "Auth Service URL: $AUTH_URL"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Helper function to print test results
test_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        ((TESTS_FAILED++))
    fi
}

echo "=========================================="
echo "Test 1: Health Check"
echo "=========================================="
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" $GATEWAY_URL/health)
if [ "$RESPONSE" = "200" ]; then
    test_result 0 "Gateway health check"
else
    test_result 1 "Gateway health check (got $RESPONSE)"
fi
echo ""

echo "=========================================="
echo "Test 2: User Registration"
echo "=========================================="
REGISTER_RESPONSE=$(curl -s -X POST $GATEWAY_URL/api/v1/auth/register \
    -H "Content-Type: application/json" \
    -d '{
        "email": "test'$(date +%s)'@example.com",
        "password": "password123",
        "firstName": "Test",
        "lastName": "User"
    }')

if echo "$REGISTER_RESPONSE" | grep -q "success"; then
    test_result 0 "User registration"
    TEST_EMAIL=$(echo "$REGISTER_RESPONSE" | jq -r '.data.user.email')
    echo "  Registered user: $TEST_EMAIL"
else
    test_result 1 "User registration"
fi
echo ""

echo "=========================================="
echo "Test 3: User Login"
echo "=========================================="
LOGIN_RESPONSE=$(curl -s -X POST $GATEWAY_URL/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{
        "email": "'$TEST_EMAIL'",
        "password": "password123"
    }')

if echo "$LOGIN_RESPONSE" | grep -q "accessToken"; then
    test_result 0 "User login"
    ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken')
    REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.refreshToken')
    echo "  Access token obtained: ${ACCESS_TOKEN:0:20}..."
else
    test_result 1 "User login"
fi
echo ""

echo "=========================================="
echo "Test 4: Access Protected Endpoint"
echo "=========================================="
ME_RESPONSE=$(curl -s -X GET $GATEWAY_URL/api/v1/auth/me \
    -H "Authorization: Bearer $ACCESS_TOKEN")

if echo "$ME_RESPONSE" | grep -q "$TEST_EMAIL"; then
    test_result 0 "Access protected endpoint with valid token"
else
    test_result 1 "Access protected endpoint with valid token"
fi
echo ""

echo "=========================================="
echo "Test 5: Access Without Token (Should Fail)"
echo "=========================================="
NO_TOKEN_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" \
    -X GET $GATEWAY_URL/api/v1/auth/me)

if [ "$NO_TOKEN_RESPONSE" = "401" ]; then
    test_result 0 "Reject request without token"
else
    test_result 1 "Reject request without token (got $NO_TOKEN_RESPONSE)"
fi
echo ""

echo "=========================================="
echo "Test 6: Access With Invalid Token (Should Fail)"
echo "=========================================="
INVALID_TOKEN_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" \
    -X GET $GATEWAY_URL/api/v1/auth/me \
    -H "Authorization: Bearer invalid.token.here")

if [ "$INVALID_TOKEN_RESPONSE" = "401" ]; then
    test_result 0 "Reject request with invalid token"
else
    test_result 1 "Reject request with invalid token (got $INVALID_TOKEN_RESPONSE)"
fi
echo ""

echo "=========================================="
echo "Test 7: Token Refresh"
echo "=========================================="
REFRESH_RESPONSE=$(curl -s -X POST $GATEWAY_URL/api/v1/auth/refresh-token \
    -H "Content-Type: application/json" \
    -d '{
        "refreshToken": "'$REFRESH_TOKEN'"
    }')

if echo "$REFRESH_RESPONSE" | grep -q "accessToken"; then
    test_result 0 "Token refresh"
    NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESPONSE" | jq -r '.data.accessToken')
    echo "  New access token obtained: ${NEW_ACCESS_TOKEN:0:20}..."
else
    test_result 1 "Token refresh"
fi
echo ""

echo "=========================================="
echo "Test 8: Logout (Token Blacklist)"
echo "=========================================="
LOGOUT_RESPONSE=$(curl -s -X POST $GATEWAY_URL/api/v1/auth/logout \
    -H "Authorization: Bearer $ACCESS_TOKEN")

if echo "$LOGOUT_RESPONSE" | grep -q "success"; then
    test_result 0 "User logout"
else
    test_result 1 "User logout"
fi
echo ""

echo "=========================================="
echo "Test 9: Use Blacklisted Token (Should Fail)"
echo "=========================================="
sleep 2  # Wait for blacklist to propagate
BLACKLISTED_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" \
    -X GET $GATEWAY_URL/api/v1/auth/me \
    -H "Authorization: Bearer $ACCESS_TOKEN")

if [ "$BLACKLISTED_RESPONSE" = "401" ] || [ "$BLACKLISTED_RESPONSE" = "403" ]; then
    test_result 0 "Reject blacklisted token"
else
    test_result 1 "Reject blacklisted token (got $BLACKLISTED_RESPONSE)"
fi
echo ""

echo "=========================================="
echo "Test 10: Gateway Logging"
echo "=========================================="
if [ -f "logs/gateway.log" ]; then
    LOG_ENTRIES=$(grep -c "Incoming Request" logs/gateway.log || echo "0")
    if [ "$LOG_ENTRIES" -gt "0" ]; then
        test_result 0 "Gateway logging enabled ($LOG_ENTRIES entries)"
    else
        test_result 1 "Gateway logging enabled"
    fi
else
    echo -e "${YELLOW}⚠ SKIP${NC}: Log file not found (check console logs)"
fi
echo ""

echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed! ✓${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed. Please check the implementation.${NC}"
    exit 1
fi

