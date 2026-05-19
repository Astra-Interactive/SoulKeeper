plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.forge)
    implementation(libs.klibs.mikro.core)
    // Local
    implementation(projects.modules.core)
    implementation(projects.modules.dao)
    implementation(projects.modules.service)
    implementation(projects.modules.serviceForge)
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
    compileOnly(libs.minecraft.forgeversion)
    compileOnly(libs.minecraft.forge.bus)
}

configurations.runtimeElements {
    setExtendsFrom(emptySet())
}
