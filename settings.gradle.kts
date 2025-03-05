pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TamagotchiForLovers"
include(":app")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("0.9.0")
}