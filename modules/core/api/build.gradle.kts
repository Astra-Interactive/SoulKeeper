plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("ru.astrainteractive.gradleplugin.detekt")
    id("ru.astrainteractive.gradleplugin.java.version")
}

dependencies {
    api(libs.klibs.kstorage)

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

    testImplementation(libs.minecraft.brigadier)
    testImplementation(libs.minecraft.kyori.api)
    testImplementation(libs.tests.kotlin.test)
    testImplementation(libs.tests.kotlin.test)
}
