#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting AlonAndroid publishing process...${NC}"

# Step 1: Make sure we have the latest code
echo -e "${YELLOW}Pulling latest changes from remote repository...${NC}"
git pull origin main

# Step 2: Build the library to make sure everything compiles
echo -e "${YELLOW}Building the library...${NC}"
./gradlew clean build

# Check if build was successful
if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed. Please fix the issues before publishing.${NC}"
    exit 1
fi

# Step 3: Ask for version number
echo -e "${YELLOW}Please enter the version number (e.g., 1.0.0):${NC}"
read VERSION

# Validate version format (simple check)
if [[ ! $VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo -e "${RED}Invalid version format. Please use semantic versioning (e.g., 1.0.0).${NC}"
    exit 1
fi

# Step 4: Update version in build.gradle
echo -e "${YELLOW}Updating version in build.gradle...${NC}"
sed -i '' "s/version = '[0-9]*\.[0-9]*\.[0-9]*'/version = '$VERSION'/g" alonhealth/build.gradle

# Step 5: Commit the version change
echo -e "${YELLOW}Committing version change...${NC}"
git add alonhealth/build.gradle
git commit -m "Bump version to $VERSION"

# Step 6: Create and push tag
echo -e "${YELLOW}Creating and pushing tag v$VERSION...${NC}"
git tag -a "v$VERSION" -m "Version $VERSION"
git push origin "v$VERSION"

# Step 7: Push the version change
echo -e "${YELLOW}Pushing changes to remote repository...${NC}"
git push origin main

# Step 8: Provide instructions for JitPack
echo -e "${GREEN}Successfully published version $VERSION of AlonAndroid!${NC}"
echo -e "${GREEN}The library will be available on JitPack shortly.${NC}"
echo -e "${GREEN}To use this version in your projects, add the following to your build.gradle:${NC}"
echo -e "${YELLOW}allprojects {${NC}"
echo -e "${YELLOW}    repositories {${NC}"
echo -e "${YELLOW}        maven { url 'https://jitpack.io' }${NC}"
echo -e "${YELLOW}    }${NC}"
echo -e "${YELLOW}}${NC}"
echo -e "${YELLOW}dependencies {${NC}"
echo -e "${YELLOW}    implementation 'com.github.alonhealth:AlonAndroid:v$VERSION'${NC}"
echo -e "${YELLOW}}${NC}"
echo -e "${GREEN}You can check the build status at: https://jitpack.io/#alonhealth/AlonAndroid/v$VERSION${NC}" 