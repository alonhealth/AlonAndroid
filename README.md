# AlonAndroid

AlonAndroid is an Android library that provides easy integration with the Android Health Connect API. It allows developers to easily fetch health data, calculate health scores, and insert steps data into Health Connect.

## Latest Release

- **Version 1.1.0** - Improved implementation with proper Health Connect API integration
  - Fixed compatibility issues with the latest Health Connect API
  - Improved error handling for data access methods
  - Enhanced code stability and reliability

## Features

- Easy integration with Android Health Connect API
- Fetch steps data from the last 7 days
- Retrieve heart rate variability (HRV) data
- Access sleep data and calculate a sleep score
- Calculate a comprehensive health score based on multiple metrics
- Insert steps data into Health Connect

## Installation

### Step 1: Add GitHub Packages Repository

Add the following to your project's top-level `settings.gradle.kts` file:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add GitHub Packages repository
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/alonhealth/AlonAndroid")
            credentials {
                username = providers.gradleProperty("github.username").getOrElse(System.getenv("GITHUB_USERNAME") ?: "")
                password = providers.gradleProperty("github.token").getOrElse(System.getenv("GITHUB_TOKEN") ?: "")
            }
        }
    }
}
```

### Step 2: Configure GitHub Authentication

You'll need to provide GitHub credentials to access the package. Add the following to your `~/.gradle/gradle.properties` file:

```properties
github.username=YOUR_GITHUB_USERNAME
github.token=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

Make sure your Personal Access Token has at least the `read:packages` scope.

### Step 3: Add Dependency

Add the dependency to your app module's `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("io.github.alonhealth:alonandroid:1.1.0")
}
```

## Usage Examples

### Initializing HealthConnectManager

```kotlin
import com.alonhealth.alonandroid.HealthConnectManager

class MainActivity : AppCompatActivity() {
    private lateinit var healthConnectManager: HealthConnectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize HealthConnectManager with your activity context
        healthConnectManager = HealthConnectManager(this)
    }
}
```

### Request Authorization

```kotlin
lifecycleScope.launch {
    healthConnectManager.requestAuthorization { success, error ->
        if (success) {
            // Permissions granted, you can now use Health Connect
        } else {
            // Handle error
            Log.e("HealthConnect", "Error: ${error?.message}")
        }
    }
}
```

### Read Steps Data

```kotlin
lifecycleScope.launch {
    val stepsData = healthConnectManager.fetchStepsData()
    if (stepsData != null) {
        Log.d("HealthConnect", "Steps: $stepsData")
    } else {
        Log.d("HealthConnect", "No steps data available")
    }
}
```

### Insert Steps Data

```kotlin
lifecycleScope.launch {
    try {
        val now = Instant.now()
        val startTime = now.minusSeconds(3600) // 1 hour ago
        healthConnectManager.insertSteps(1000, startTime, now)
        Toast.makeText(this@MainActivity, "Steps inserted successfully", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("HealthData", "Error inserting steps", e)
    }
}
```

### Calculate Health Score

```kotlin
lifecycleScope.launch {
    healthConnectManager.calculateHealthScore { score ->
        if (score != null) {
            Log.d("HealthConnect", "Health Score: $score")
        } else {
            Log.d("HealthConnect", "Could not calculate health score")
        }
    }
}
```

## Permissions

This library requires Health Connect permissions to access health data. Add the following to your app's `AndroidManifest.xml`:

```xml
<!-- Health Connect permissions -->
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.WRITE_STEPS" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE" />
<uses-permission android:name="android.permission.health.READ_SLEEP" />
<uses-permission android:name="android.permission.health.READ_DISTANCE" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE_VARIABILITY" />
```

You'll also need to request these permissions at runtime using the Health Connect permission handling.

## Requirements

- Android SDK 29 (Android 10) or higher
- Health Connect installed on the device

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

For questions, bug reports, or feature requests, please open an issue on the [GitHub repository](https://github.com/alonhealth/AlonAndroid).
