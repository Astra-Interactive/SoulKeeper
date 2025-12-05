plugins {
    kotlin("jvm")
    alias(libs.plugins.neoforgegradle)
}

dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.klibs.mikro.core)
    // klibs
    implementation(projects.modules.serviceNeoforge)
}

dependencies {
    compileOnly(libs.minecraft.neoforgeversion)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

configurations.runtimeElements {
    setExtendsFrom(emptySet())
}
