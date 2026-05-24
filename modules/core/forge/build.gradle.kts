plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("ru.astrainteractive.gradleplugin.detekt")
    id("ru.astrainteractive.gradleplugin.java.version")
}

dependencies {
    implementation(libs.kotlin.serialization.json)
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.forge)
    implementation(projects.modules.core.api)
    implementation(projects.modules.dao)
    implementation(projects.modules.service.api)
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
    compileOnly(libs.joml)
    compileOnly(libs.minecraft.brigadier)
    compileOnly(libs.minecraft.datafixerupper)
    compileOnly(libs.minecraft.forge.bus)
    compileOnly(libs.minecraft.forgeversion)
}
