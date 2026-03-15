plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    compileOnly(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)

    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.command)

    implementation(libs.minecraft.kyori.api)

    implementation(libs.klibs.mikro.core)
    implementation(projects.modules.core)
    implementation(projects.modules.dao)
    implementation(projects.modules.service)
}

dependencies {
    compileOnly("org.joml:joml:1.10.8")
    compileOnly("com.mojang:datafixerupper:8.0.16")
    compileOnly("com.mojang:brigadier:1.3.10")
}
