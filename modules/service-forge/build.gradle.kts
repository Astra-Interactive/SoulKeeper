plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlin.serialization.json)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.forge)
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
    implementation(projects.modules.coreForge)
}

dependencies {
    dependencies {
        compileOnly(
            files(
                rootProject
                    .file(".gradle")
                    .resolve("mavenizer")
                    .resolve("repo")
                    .resolve("net")
                    .resolve("minecraftforge")
                    .resolve("forge")
                    .resolve(libs.versions.minecraft.forgeversion.get())
                    .resolve("forge-${libs.versions.minecraft.forgeversion.get()}.jar")
            )
        )
    }
    compileOnly(libs.minecraft.brigadier)
    compileOnly(libs.minecraft.datafixerupper)
    compileOnly(libs.joml)
    compileOnly(libs.minecraft.forgeversion)
    compileOnly(libs.minecraft.forge.bus)
}

configurations.runtimeElements {
    setExtendsFrom(emptySet())
}
