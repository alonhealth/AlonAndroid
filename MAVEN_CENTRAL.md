# Publishing to Maven Central

This guide explains how to publish the AlonAndroid library to Maven Central.

## Prerequisites

1. Create a Sonatype OSSRH account:

   - Sign up at [Sonatype JIRA](https://issues.sonatype.org/secure/Signup)
   - Create a new project ticket requesting access to publish under the `com.alonhealth` group ID

2. Generate a GPG key pair:

   ```bash
   gpg --gen-key
   ```

3. Distribute your public key:
   ```bash
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

## Configuration

1. Create or update your `~/.gradle/gradle.properties` file with the following:

   ```properties
   signing.keyId=YOUR_KEY_ID_LAST_8_CHARS
   signing.password=YOUR_GPG_KEY_PASSWORD
   signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg

   ossrhUsername=YOUR_SONATYPE_USERNAME
   ossrhPassword=YOUR_SONATYPE_PASSWORD
   ```

2. Update the `alonhealth/build.gradle` file:

   ```gradle
   plugins {
       id 'com.android.library'
       id 'kotlin-android'
       id 'maven-publish'
       id 'signing'
   }

   // ... existing code ...

   afterEvaluate {
       publishing {
           publications {
               release(MavenPublication) {
                   from components.release

                   groupId = 'com.alonhealth'
                   artifactId = 'alonandroid'
                   version = '1.0.0'

                   pom {
                       name = 'AlonAndroid'
                       description = 'A simple wrapper for health data fetching on Android'
                       url = 'https://github.com/alonhealth/AlonAndroid'

                       licenses {
                           license {
                               name = 'MIT License'
                               url = 'https://opensource.org/licenses/MIT'
                           }
                       }

                       developers {
                           developer {
                               id = 'alonhealth'
                               name = 'Alon Health'
                               email = 'info@alonhealth.com'
                           }
                       }

                       scm {
                           connection = 'scm:git:github.com/alonhealth/AlonAndroid.git'
                           developerConnection = 'scm:git:ssh://github.com/alonhealth/AlonAndroid.git'
                           url = 'https://github.com/alonhealth/AlonAndroid/tree/main'
                       }
                   }
               }
           }

           repositories {
               maven {
                   name = "sonatype"

                   def releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                   def snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                   url = version.endsWith('SNAPSHOT') ? snapshotsRepoUrl : releasesRepoUrl

                   credentials {
                       username ossrhUsername
                       password ossrhPassword
                   }
               }
           }
       }

       signing {
           sign publishing.publications.release
       }
   }
   ```

## Publishing

1. Build and publish the library:

   ```bash
   ./gradlew clean build
   ./gradlew publishReleasePublicationToSonatypeRepository
   ```

2. Release from Sonatype Staging Repository:

   - Log in to [Sonatype Nexus](https://s01.oss.sonatype.org/)
   - Navigate to "Staging Repositories"
   - Find your repository, verify its contents
   - Click "Close" and wait for validation
   - If validation passes, click "Release"

3. After release, your library will be available in Maven Central within a few hours.

## Using the Published Library

```gradle
dependencies {
    implementation 'com.alonhealth:alonandroid:1.0.0'
}
```

## Automating the Process

You can create a script to automate the process:

```bash
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
```

Save this as `publish-maven.sh` and make it executable with `chmod +x publish-maven.sh`.
