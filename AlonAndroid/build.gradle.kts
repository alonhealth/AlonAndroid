import java.nio.file.Files
import java.nio.file.Paths

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

allprojects {
    version = "1.1.0" // Ensure this version follows semver
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
        getByName("release") {
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

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.connect.client)
}

// Ensure AAR is built before publishing
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn("assemble")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "io.github.alonhealth"
            artifactId = "alonandroid"
            version = "1.1.0"
            
            // This is a simpler way to include the AAR
            afterEvaluate {
                from(components["release"])
            }
            
            pom {
                name.set("Alon Android")
                description.set("A library to fetch data from Health Connect")
                url.set("https://github.com/alonhealth/Alon-android")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("lucaslober")
                        name.set("Lucas Lober")
                        email.set("lucaslober@example.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/alonhealth/AlonAndroid.git")
                    developerConnection.set("scm:git:ssh://github.com:alonhealth/AlonAndroid.git")
                    url.set("https://github.com/alonhealth/AlonAndroid")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/alonhealth/AlonAndroid")
            credentials {
                username = System.getenv("GITHUB_USERNAME") ?: project.findProperty("github.username") as String? ?: System.getProperty("github.username")
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("github.token") as String? ?: System.getProperty("github.token")
            }
        }
    }
}
