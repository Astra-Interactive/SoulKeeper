plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    // Kotlin
    implementation(libs.bundles.kotlin)
    // Bukkit
    compileOnly(libs.minecraft.paper.api)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.menu.bukkit)
    implementation(libs.minecraft.astralibs.core.bukkit)
    // klibs
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.core)
    api(libs.klibs.kstorage)
}
