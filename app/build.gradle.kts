plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs")
    id("org.sonarqube") version "6.2.0.5505"
}

android {
    namespace = "com.github.bobryanskiy.tamagotchiforlovers"
    testBuildType = "release"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.bobryanskiy.tamagotchiforlovers"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

//    signingConfigs {
//        getByName("debug") {
//            storeFile = file("debug.keystore")
//            storePassword = "android"
//            keyAlias = "androiddebugkey"
//            keyPassword = "android"
//            storeType = "PKCS12"  // Set store type to PKCS12
//        }
//    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "test-proguard-rules.pro")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
            enableAndroidTestCoverage = true
        }
//        debug {
//            signingConfig = signingConfigs.getByName("debug")
//        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        viewBinding = true
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("androidx.dynamicanimation:dynamicanimation-ktx:1.1.0")
    implementation("com.google.guava:guava:33.4.0-android")

    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))

    implementation("com.google.android.gms:play-services-auth:21.3.0")

    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

