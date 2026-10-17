import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("ru.astrainteractive.gradleplugin.linter")
    id("ru.astrainteractive.gradleplugin.java.version")
}

linter {
    unitKind.set("technology")
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
    implementation(libs.minecraft.astralibs.core.neoforge)
    implementation(projects.modules.core.api)
    implementation(projects.modules.core.neoforge)
    implementation(projects.modules.data.dao)
    implementation(projects.modules.service.api)

    testImplementation(libs.tests.kotlin.test)
}

dependencies {
    compileOnly(
        files(
            rootProject.project(projects.instances.neoforge.path)
                .file(".gradle")
                .resolve("repositories")
                .resolve("ng_dummy_ng")
                .resolve("net")
                .resolve("neoforged")
                .resolve("neoforge")
                .resolve(libs.versions.minecraft.neoforgeversion.get())
                .resolve("neoforge-${libs.versions.minecraft.neoforgeversion.get()}.jar")
        )
    )
    compileOnly(libs.joml)
    compileOnly(libs.minecraft.brigadier)
    compileOnly(libs.minecraft.datafixerupper)
    compileOnly(libs.minecraft.neoforged.bus)
    compileOnly(libs.minecraft.neoforgeversion)
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(":instances:neoforge:generateNeoforgeJar")
}
