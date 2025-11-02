plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    compileOnly(libs.kotlin.coroutines.core)
    // Bukkit
    compileOnly(libs.minecraft.paper.api)
    compileOnly(libs.minecraft.bstats)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.command)
    implementation(libs.minecraft.astralibs.command.bukkit)
    implementation(libs.minecraft.astralibs.core.bukkit)
    // klibs
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.core)
    implementation(projects.modules.core)
    implementation(projects.modules.dao)
    implementation(projects.modules.coreBukkit)
}
