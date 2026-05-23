plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlin.serialization.kaml)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.coroutines.core)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.klibs.mikro.extensions)
    // klibs
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.extensions)
    implementation(libs.klibs.mikro.core)
    api(libs.klibs.kstorage)
    // Test
    testImplementation(libs.tests.kotlin.test)
    testImplementation(libs.tests.kotlin.test)
}
