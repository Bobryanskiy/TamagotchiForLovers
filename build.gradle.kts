buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.1")
        classpath("org.bouncycastle:bcprov-jdk18on:1.80")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.6.1" apply false
    id("com.android.library") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.github.ben-manes.versions") version "0.52.0"
    id("org.sonarqube") version "6.0.1.5171"
}

sonar {
    properties {
        property("sonar.projectKey", "Bobryanskiy_TamagotchiForLovers")
        property("sonar.organization", "bobryanskiy")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}