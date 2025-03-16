# AlonAndroid

AlonAndroid is a simple wrapper library for health data fetching on Android. It provides easy access to health data from Health Connect API.

## Features

- Easy access to health data (heart rate, steps, sleep, weight)
- Heart rate variability (HRV) data access
- Health score calculation based on health data
- Permission handling for health data access
- Conversion of raw health data to easy-to-use models

## Installation

Add the following to your app's build.gradle file:

```gradle
dependencies {
    implementation 'com.alonhealth:alonandroid:1.0.0'
}
```

## Usage

### Initialize AlonAndroid

```kotlin
// Initialize AlonAndroid
val alonAndroid = AlonAndroid(context)
```

### Request Permissions

```kotlin
// Request permissions
alonAndroid.requestAuthorization { success, error ->
    if (success) {
        // Permissions granted, proceed with data fetching
    } else {
        // Handle error
        Log.e("AlonAndroid", "Permission error: ${error?.message}")
    }
}
```

### Check Health Connect Availability

```kotlin
// Check if Health Connect is available
val isAvailable = alonAndroid.isHealthConnectAvailable()
if (!isAvailable) {
    // Prompt user to install Health Connect
    alonAndroid.openHealthConnectPlayStore()
}
```

### Fetch Health Data

```kotlin
// Fetch step count data
val startTime = Instant.now().minus(7, ChronoUnit.DAYS)
val endTime = Instant.now()

alonAndroid.getStepCountData(startTime, endTime) { stepCountData ->
    // Process step count data
}

// Fetch heart rate data
alonAndroid.getHeartRateData(startTime, endTime) { heartRateData ->
    // Process heart rate data
}

// Fetch sleep data
alonAndroid.getSleepData(startTime, endTime) { sleepData ->
    // Process sleep data
}

// Fetch weight data
alonAndroid.getWeightData(startTime, endTime) { weightData ->
    // Process weight data
}

// Fetch heart rate variability data
alonAndroid.getHeartRateVariabilityData(startTime, endTime) { hrvData ->
    // Process HRV data
}
```

### Calculate Health Score

```kotlin
// Calculate health score
alonAndroid.calculateHealthScore { score ->
    // Use the health score (0-100)
    Log.d("AlonAndroid", "Health Score: $score")
}
```

## Health Score Calculation

The health score is calculated based on:

1. Sleep quality and duration
2. Heart rate variability
3. Daily step count

The score ranges from 0 to 100, with higher scores indicating better overall health.

## Sample App

The sample app demonstrates how to use the AlonAndroid library to:

1. Request necessary permissions
2. Calculate and display a health score

## Publishing

To publish a new version of the library:

1. Run the automated publishing script:

   ```bash
   ./publish.sh
   ```

2. Enter the version number when prompted (e.g., 1.0.0)

3. The script will:

   - Update the version in build.gradle
   - Commit and push the changes
   - Create and push a tag
   - Trigger the GitHub Actions workflow to create a release

4. After the GitHub Actions workflow completes, the library will be available on JitPack.

5. You can check the status of your published version using the check-jitpack script:
   ```bash
   ./check-jitpack.sh 1.0.0
   ```

To use the published library in your projects:

```gradle
// In your project's build.gradle or settings.gradle
allprojects {
    repositories {
        // ... other repositories
        maven { url 'https://jitpack.io' }
    }
}

// In your app's build.gradle
dependencies {
    implementation 'com.github.alonhealth:AlonAndroid:v1.0.0'
}
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
