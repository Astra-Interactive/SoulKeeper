plugins {
    kotlin("jvm")
}

dependencies {
    // Kotlin
    implementation(libs.bundles.kotlin)
    // Bukkit
    compileOnly(libs.minecraft.paper.api)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.bukkit)
    // klibs
    implementation(projects.modules.core)
    implementation(projects.modules.coreBukkit)
    implementation(projects.modules.dao)
}
