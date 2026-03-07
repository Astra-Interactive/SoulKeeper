plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    // Kotlin
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.kotlin.serialization.json)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.klibs.mikro.extensions)
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.kstorage)

    testImplementation(libs.tests.kotlin.test)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.driver.jdbc)
}
