plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("ru.astrainteractive.klibs.rules.gradle.linter")
    id("ru.astrainteractive.gradleplugin.java.version")
}

configurations.api {
    dependencies.removeIf { dependency ->
        dependency.group == "org.jetbrains.kotlin" && dependency.name == "kotlin-stdlib"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.klibs.mikro.core)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.forge)
    implementation(projects.modules.core.api)
    implementation(projects.modules.core.forge)
    implementation(projects.modules.data.dao)
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
    compileOnly(libs.minecraft.forge.bus)
    compileOnly(libs.minecraft.forgeversion)
}
