plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("ru.astrainteractive.gradleplugin.detekt")
    id("ru.astrainteractive.gradleplugin.java.version")
}

dependencies {
    compileOnly(libs.kotlin.coroutines.core)

    implementation(libs.klibs.mikro.core)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.minecraft.astralibs.command)
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.kyori.api)
    implementation(projects.modules.core.api)
    implementation(projects.modules.dao)
    implementation(projects.modules.service.api)
}

dependencies {
    compileOnly(libs.joml)
    compileOnly(libs.minecraft.datafixerupper)
    compileOnly(libs.minecraft.brigadier)
}
