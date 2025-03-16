#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting AlonAndroid Maven Central publishing process...${NC}"

# Step 1: Ask for version number
echo -e "${YELLOW}Please enter the version number (e.g., 1.0.0):${NC}"
read VERSION

# Validate version format (simple check)
if [[ ! $VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo -e "${RED}Invalid version format. Please use semantic versioning (e.g., 1.0.0).${NC}"
    exit 1
fi

# Step 2: Update version in build.gradle
echo -e "${YELLOW}Updating version in build.gradle...${NC}"
sed -i '' "s/version = '[0-9]*\.[0-9]*\.[0-9]*'/version = '$VERSION'/g" alonhealth/build.gradle

# Step 3: Build and publish
echo -e "${YELLOW}Building and publishing to Maven Central...${NC}"
./gradlew clean build
./gradlew publishReleasePublicationToSonatypeRepository

# Step 4: Instructions for manual steps
echo -e "${GREEN}Library has been published to Sonatype staging repository.${NC}"
echo -e "${YELLOW}Please complete these manual steps:${NC}"
echo -e "1. Log in to https://s01.oss.sonatype.org/"
echo -e "2. Navigate to 'Staging Repositories'"
echo -e "3. Find your repository, verify its contents"
echo -e "4. Click 'Close' and wait for validation"
echo -e "5. If validation passes, click 'Release'"
echo -e "${GREEN}After release, your library will be available in Maven Central within a few hours.${NC}" 