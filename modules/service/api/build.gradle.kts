plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("ru.astrainteractive.klibs.rules.gradle.linter")
    id("ru.astrainteractive.gradleplugin.java.version")
}

dependencies {
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.klibs.kstorage)
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.extensions)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.minecraft.astralibs.command)
    implementation(libs.minecraft.astralibs.core)
    implementation(projects.modules.core.api)
    implementation(projects.modules.data.dao)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.minecraft.kyori.api)
    testImplementation(libs.tests.kotlin.test)
    testImplementation(testFixtures(projects.modules.core.api))
    testImplementation(testFixtures(projects.modules.data.dao))
}
