#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if version is provided
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}Please provide a version number (e.g., 1.0.0):${NC}"
    read VERSION
else
    VERSION=$1
fi

# Validate version format (simple check)
if [[ ! $VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo -e "${RED}Invalid version format. Please use semantic versioning (e.g., 1.0.0).${NC}"
    exit 1
fi

# Remove 'v' prefix if present
VERSION=${VERSION#v}

echo -e "${YELLOW}Checking JitPack status for AlonAndroid v$VERSION...${NC}"

# Use curl to check the status
STATUS=$(curl -s "https://jitpack.io/api/builds/com.github.alonhealth/AlonAndroid/v$VERSION" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ -z "$STATUS" ]; then
    echo -e "${RED}Could not retrieve status from JitPack. The version might not exist or JitPack might be unavailable.${NC}"
    echo -e "${YELLOW}You can check manually at: https://jitpack.io/#alonhealth/AlonAndroid/v$VERSION${NC}"
    exit 1
fi

case $STATUS in
    "ok")
        echo -e "${GREEN}Version v$VERSION is successfully published and ready to use!${NC}"
        echo -e "${GREEN}You can use it in your projects with:${NC}"
        echo -e "${YELLOW}implementation 'com.github.alonhealth:AlonAndroid:v$VERSION'${NC}"
        ;;
    "queued")
        echo -e "${YELLOW}Version v$VERSION is queued for building on JitPack.${NC}"
        echo -e "${YELLOW}Please check again later.${NC}"
        ;;
    "building")
        echo -e "${YELLOW}Version v$VERSION is currently building on JitPack.${NC}"
        echo -e "${YELLOW}Please check again later.${NC}"
        ;;
    "error")
        echo -e "${RED}There was an error building version v$VERSION on JitPack.${NC}"
        echo -e "${YELLOW}Please check the build logs at: https://jitpack.io/#alonhealth/AlonAndroid/v$VERSION${NC}"
        ;;
    *)
        echo -e "${YELLOW}Status: $STATUS${NC}"
        echo -e "${YELLOW}Please check at: https://jitpack.io/#alonhealth/AlonAndroid/v$VERSION${NC}"
        ;;
esac 