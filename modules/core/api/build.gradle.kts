plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("ru.astrainteractive.klibs.rules.gradle.linter")
    id("ru.astrainteractive.gradleplugin.java.version")
    `java-test-fixtures`
}

configurations.api {
    dependencies.removeIf { dependency ->
        dependency.group == "org.jetbrains.kotlin" && dependency.name == "kotlin-stdlib"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.klibs.kstorage)

    compileOnly(libs.minecraft.brigadier)

    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.extensions)
    implementation(libs.klibs.mikro.extensions)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.serialization.kaml)
    implementation(libs.minecraft.astralibs.command)
    implementation(libs.minecraft.astralibs.core)

    testImplementation(libs.tests.kotlin.test)
    testImplementation(libs.tests.kotlin.test)

    testFixturesImplementation(libs.minecraft.astralibs.core)
}
