plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlin.serialization.json)
    // Bukkit
    compileOnly(libs.minecraft.paper.api)
    compileOnly(libs.minecraft.bstats)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.menu.bukkit)
    implementation(libs.minecraft.astralibs.core.bukkit)
    implementation(libs.klibs.mikro.extensions)
    // klibs
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.core)
    implementation(projects.modules.core)
    api(libs.klibs.kstorage)
}
