plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.metro)
}

dependencies {
    compileOnly(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)

    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.bukkit)
    implementation(libs.minecraft.astralibs.command)
    implementation(libs.minecraft.astralibs.command.bukkit)

    compileOnly(libs.minecraft.paper.api)

    implementation(libs.klibs.mikro.core)
    implementation(projects.modules.core)
    implementation(projects.modules.coreBukkit)
    implementation(projects.modules.dao)
}
