
import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireProjectInfo
import ru.astrainteractive.gradleplugin.setupVelocityProcessor

plugins {
    kotlin("jvm")
    alias(libs.plugins.gradle.buildconfig)
}

dependencies {
    // Kotlin
    implementation(libs.bundles.kotlin)
    // Velocity
    compileOnly(libs.minecraft.velocity.api)
    annotationProcessor(libs.minecraft.velocity.api)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    // Test
    testImplementation(libs.bundles.testing.kotlin)
    testImplementation(libs.tests.kotlin.test)
}

buildConfig {
    val requireProjectInfo = requireProjectInfo
    className("BuildKonfig")
    packageName(requireProjectInfo.group)
    fun buildConfigStringField(name: String, value: String) {
        buildConfigField("String", name, "\"${value}\"")
    }
    buildConfigStringField("id", requireProjectInfo.name.toLowerCase())
    buildConfigStringField("name", requireProjectInfo.name)
    buildConfigStringField("version", requireProjectInfo.versionString)
    buildConfigStringField("url", requireProjectInfo.url)
    buildConfigStringField("description", requireProjectInfo.description)
    buildConfigStringField("author", requireProjectInfo.developersList.first().id)
}

setupVelocityProcessor()
