import java.nio.file.Files
import java.nio.file.Paths

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("org.jreleaser") version "1.13.1"
}

allprojects {
    version = "1.0.1" // Ensure this version follows semver
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

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "io.github.alonhealth"
            artifactId = "alonandroid"
            version = "1.0.0"
            artifact("${projectDir}/build/outputs/aar/AlonAndroid-release.aar")

            pom {
                name.set("Alon Android")
                description.set("A library to fetch data from Health Connect")
                url.set("https://github.com/alonhealth/AlonAndroid")
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
            name = "sonatype"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

jreleaser {
    project {
        name.set("AlonAndroid")
        description.set("A library to fetch data from Health Connect")
        links {
            homepage.set("https://github.com/alonhealth/Alon-android")
            documentation.set("https://github.com/alonhealth/Alon-android")
            license.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    release {
        github {
            repoOwner.set("alonhealth")
            name.set("AlonAndroid")
            token.set(findProperty("jreleaser.github.token") as String? ?: System.getenv("JRELEASER_GITHUB_TOKEN"))
        }
    }
    gitRootSearch.set(true)  // Ensure JReleaser searches for the Git root
    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
        publicKey.set(file("/Users/lucaslober/public_key.gpg").readText(Charsets.UTF_8))
        secretKey.set(file("/Users/lucaslober/secret_key.gpg").readText(Charsets.UTF_8))
        passphrase.set(findProperty("jreleaser.gpg.passphrase") as String? ?: System.getenv("JRELEASER_GPG_PASSPHRASE"))
    }
    deploy {
        maven {
            nexus2 {
                register("maven-central") {
                    url.set("https://s01.oss.sonatype.org/service/local")
                    snapshotUrl.set("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                    closeRepository.set(true)
                    releaseRepository.set(true)
                    stagingRepository("build/staging-deploy")
                    username.set(findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME"))
                    password.set(findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD"))
                }
            }
        }
    }
}
