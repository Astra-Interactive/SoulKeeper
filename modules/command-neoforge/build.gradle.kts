plugins {
    kotlin("jvm")
    alias(libs.plugins.neoforgegradle)
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.coroutines.core)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.command)
    // klibs
    implementation(libs.klibs.kstorage)
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.extensions)
    // kyori
    implementation(libs.kyori.api)
    // Test
    testImplementation(libs.tests.kotlin.test)
    // Local
}

dependencies {
    compileOnly(libs.minecraft.neoforgeversion)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

configurations.runtimeElements {
    setExtendsFrom(emptySet())
}
