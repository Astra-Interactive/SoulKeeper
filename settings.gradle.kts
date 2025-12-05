pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "SoulKeeper"

// Spigot
include(":instances:neoforge")
// Modules
include(":modules:service-neoforge")
include(":modules:event-neoforge")
include(":modules:command-neoforge")
