import org.jreleaser.model.Distribution.DistributionType
import org.jreleaser.model.Stereotype

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jreleaser") version "1.12.0"
}

android {
    namespace = "io.github.alonhealth.alonandroid"
    compileSdk = 34

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

jreleaser {
    project {
        name.set("alon-android")
        version.set("1.0.0")
        description.set("A library to fetch data from Health Connect")
        longDescription.set("A detailed description of the library.")
        inceptionYear.set("2023")
        copyright.set("2023 The AlonHealth Authors")
        vendor.set("AlonHealth")

        authors.set(listOf("Author Name"))
        tags.set(listOf("android", "health", "library"))

        links {
            homepage.set("https://github.com/alonhealth/AlonAndroid")
            documentation.set("https://github.com/alonhealth/AlonAndroid/wiki")
            license.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            bugTracker.set("https://github.com/alonhealth/AlonAndroid/issues")
            vcsBrowser.set("https://github.com/alonhealth/AlonAndroid")
        }
    }

    release {
        github {
            name.set("AlonAndroid")
            tagName.set("v1.0.0")
            releaseName.set("alon-android-1.0.0")
            branch.set("main")
            draft.set(false)
            discussionCategoryName.set("General")
            prerelease {
                enabled.set(false)
            }
            releaseNotes {
                enabled.set(true)
                configurationFile.set("release-notes.md")
            }
        }
    }

    distributions {
        create("alon-android") {
            distributionType.set(DistributionType.JAVA_BINARY)
            stereotype.set(Stereotype.MOBILE)
            artifact {
                path.set(layout.buildDirectory.file("outputs/aar/AlonAndroid-release.aar")) // Adjust path if necessary
            }
            tags.set(listOf("android", "health", "library"))
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.connect.client)
}
