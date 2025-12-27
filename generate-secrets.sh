#!/bin/bash

# Script to generate secure secrets for the Trading Gateway
# This script generates random secrets for production use

echo "=========================================="
echo "Trading Gateway - Secret Generator"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Generating secure secrets...${NC}"
echo ""

# Generate Gateway Secret (32 bytes)
GATEWAY_SECRET=$(openssl rand -base64 32 | tr -d '\n')
echo -e "${GREEN}GATEWAY_SECRET:${NC}"
echo "$GATEWAY_SECRET"
echo ""

# Generate JWT Secret (64 bytes)
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo -e "${GREEN}JWT_SECRET:${NC}"
echo "$JWT_SECRET"
echo ""

# Generate MongoDB Password (32 bytes)
MONGO_PASSWORD=$(openssl rand -base64 32 | tr -d '\n')
echo -e "${GREEN}MONGO_PASSWORD:${NC}"
echo "$MONGO_PASSWORD"
echo ""

echo "=========================================="
echo "Instructions:"
echo "=========================================="
echo "1. Copy .env.example to .env:"
echo "   cp .env.example .env"
echo ""
echo "2. Update the following values in .env:"
echo "   GATEWAY_SECRET=$GATEWAY_SECRET"
echo "   JWT_SECRET=$JWT_SECRET"
echo "   MONGO_PASSWORD=$MONGO_PASSWORD"
echo ""
echo "3. NEVER commit .env to version control!"
echo ""
echo -e "${YELLOW}⚠️  Keep these secrets secure!${NC}"
echo ""

# Optionally create .env file
read -p "Do you want to create .env file automatically? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    if [ -f ".env" ]; then
        echo -e "${YELLOW}⚠️  .env file already exists. Creating backup...${NC}"
        cp .env .env.backup.$(date +%Y%m%d_%H%M%S)
    fi
    
    cp .env.example .env
    
    # Update secrets in .env file
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s|GATEWAY_SECRET=.*|GATEWAY_SECRET=$GATEWAY_SECRET|" .env
        sed -i '' "s|JWT_SECRET=.*|JWT_SECRET=$JWT_SECRET|" .env
        sed -i '' "s|MONGO_PASSWORD=.*|MONGO_PASSWORD=$MONGO_PASSWORD|" .env
    else
        # Linux
        sed -i "s|GATEWAY_SECRET=.*|GATEWAY_SECRET=$GATEWAY_SECRET|" .env
        sed -i "s|JWT_SECRET=.*|JWT_SECRET=$JWT_SECRET|" .env
        sed -i "s|MONGO_PASSWORD=.*|MONGO_PASSWORD=$MONGO_PASSWORD|" .env
    fi
    
    echo -e "${GREEN}✓ .env file created with secure secrets!${NC}"
    echo ""
    echo "You can now run:"
    echo "  docker-compose up -d"
else
    echo "Skipped .env file creation."
    echo "Please update .env manually with the secrets above."
fi

