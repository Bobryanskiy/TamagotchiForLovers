buildscript {
    val agp_version by extra("8.9.2")
//    dependencies {
//        classpath("org.bouncycastle:bcutil-jdk18on:1.80")
//    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.9.3" apply false
    id("com.android.library") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("androidx.navigation.safeargs") version "2.9.0" apply false
    id("com.github.ben-manes.versions") version "0.52.0"
}