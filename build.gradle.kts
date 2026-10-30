plugins {
    id("com.android.library") version "8.7.2"
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.0.21"
}

apply(from = "gradle/weartak-aar-signing.gradle")

android {
    namespace = "com.weartak.plugin.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 30
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        aidl = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { test ->
            if (gradle.extra.has("takArtifactoryUrl")) {
                test.systemProperty("robolectric.dependency.repo.id", "takArtifactory")
                test.systemProperty("robolectric.dependency.repo.url", gradle.extra["takArtifactoryUrl"]!!)
                test.systemProperty("robolectric.dependency.repo.username", gradle.extra["takArtifactoryUser"]!!)
                test.systemProperty("robolectric.dependency.repo.password", gradle.extra["takArtifactoryPassword"]!!)
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13")
}
