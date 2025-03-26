# AlonAndroid

A powerful Android library for accessing and managing health data through Health Connect API. AlonAndroid simplifies the integration of health metrics like steps, heart rate variability, and sleep data into your Android applications.

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
                username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

### Step 2: Configure GitHub Authentication

You'll need to provide GitHub credentials to access the package. Add the following to your `~/.gradle/gradle.properties` file:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

Make sure your Personal Access Token has at least the `read:packages` scope.

### Step 3: Add Dependency

Add the dependency to your app module's `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("io.github.alonhealth:alonandroid:1.0.6")
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
    healthConnectManager.requestAuthorization { success, exception ->
        if (success) {
            Toast.makeText(this@MainActivity, "Authorization successful", Toast.LENGTH_SHORT).show()
            // Ready to read health data
        } else {
            Toast.makeText(this@MainActivity, "Authorization failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

### Read Steps Data

```kotlin
lifecycleScope.launch {
    try {
        val stepsCount = healthConnectManager.readSteps()
        textView.text = "Steps: ${stepsCount ?: 0}"
    } catch (e: Exception) {
        Log.e("HealthData", "Error reading steps", e)
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
            textView.text = "Health Score: $score"
        } else {
            textView.text = "Failed to calculate health score"
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
```

You'll also need to request these permissions at runtime using the Health Connect permission handling.

## Requirements

- Android SDK 29 (Android 10) or higher
- Health Connect installed on the device

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

For questions, bug reports, or feature requests, please open an issue on the [GitHub repository](https://github.com/alonhealth/AlonAndroid).
