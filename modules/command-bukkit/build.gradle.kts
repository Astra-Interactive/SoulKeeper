plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    compileOnly(libs.kotlin.coroutines.core)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.command)

    implementation(libs.minecraft.paper.api)
    // klibs
    implementation(libs.klibs.mikro.core)
    implementation(projects.modules.core)
    implementation(projects.modules.dao)
}
