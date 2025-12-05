import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireJinfo

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.neoforgegradle)
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.serialization.json)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.neoforge)
    implementation(libs.minecraft.astralibs.command)
    // klibs
    implementation(libs.klibs.kstorage)
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.extensions)
    // Test
    testImplementation(libs.tests.kotlin.test)
    // Local
    implementation(projects.modules.core)
    implementation(projects.modules.dao)
    implementation(projects.modules.service)
}

dependencies {
    compileOnly(libs.minecraft.neoforgeversion)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(requireJinfo.jtarget.majorVersion)

configurations.runtimeElements {
    setExtendsFrom(emptySet())
}
