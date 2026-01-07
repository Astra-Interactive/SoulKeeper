import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireProjectInfo

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.klibs.minecraft.resource.processor)
    alias(libs.plugins.gradle.shadow)
}

dependencies {
    implementation(libs.kotlin.coroutines.core)
    // Spigot dependencies
    compileOnly(libs.minecraft.paper.api)
    implementation(libs.minecraft.bstats)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.klibs.mikro.core)
    implementation(libs.minecraft.astralibs.menu.bukkit)
    implementation(libs.minecraft.astralibs.core.bukkit)
    implementation(libs.minecraft.astralibs.command)
    implementation(libs.minecraft.astralibs.command.bukkit)
    compileOnly(libs.minecraft.vaultapi)
    compileOnly(libs.driver.h2)
    compileOnly(libs.driver.jdbc)
    compileOnly(libs.driver.mysql)
    // Spigot
    compileOnly(libs.minecraft.luckperms)
    compileOnly(libs.minecraft.discordsrv)
    compileOnly(libs.minecraft.essentialsx)
    implementation(projects.modules.core)
    implementation(projects.modules.coreBukkit)
    implementation(projects.modules.eventBukkit)
    implementation(projects.modules.dao)
    implementation(projects.modules.service)
    implementation(projects.modules.serviceBukkit)
    implementation(projects.modules.commandBukkit)
    implementation(projects.modules.buildKonfig)
    implementation(projects.modules.libLoader)
}

minecraftProcessResource {
    bukkit()
}

val shadowJar = tasks.named<ShadowJar>("shadowJar")
shadowJar.configure {
    mergeServiceFiles()
    dependsOn(configurations)
    archiveClassifier = (null as String?)
    archiveVersion = (requireProjectInfo.versionString)
    isReproducibleFileOrder = true
    archiveBaseName = "${requireProjectInfo.name}-${project.name}"
    destinationDirectory = rootDir.resolve("build")
        .resolve(project.name)
        .resolve("plugins")
        .takeIf(File::exists)
        ?: File(rootDir, "jars").also(File::mkdirs)
    dependencies {
        // deps
        include(dependency(libs.minecraft.astralibs.core.asProvider()))
        include(dependency(libs.minecraft.astralibs.core.bukkit))
        include(dependency(libs.minecraft.astralibs.menu.bukkit))
        include(dependency(libs.minecraft.astralibs.command.bukkit))
        include(dependency(libs.minecraft.astralibs.command.asProvider()))
        include(dependency(libs.klibs.kstorage))
        include(dependency(libs.klibs.mikro.core))
        include(dependency(libs.klibs.mikro.extensions))
        // modules
        include(dependency(projects.modules.buildKonfig))
        include(dependency(projects.modules.commandBukkit))
        include(dependency(projects.modules.core))
        include(dependency(projects.modules.coreBukkit))
        include(dependency(projects.modules.dao))
        include(dependency(projects.modules.eventBukkit))
        include(dependency(projects.modules.libLoader))
        include(dependency(projects.modules.service))
        include(dependency(projects.modules.serviceBukkit))
        // core
        include(dependency("com.alessiodp.libby:libby-core:.*"))
        include(dependency("com.alessiodp.libby:libby-standalone:.*"))
        include(dependency("org.jetbrains.kotlin:kotlin-stdlib:.*"))
        include(dependency("org.bstats:bstats-bukkit:.*"))
        include(dependency("org.bstats:bstats-base:.*"))
    }
    listOf(
        "com.alessiodp.libby",
        "ru.astrainteractive.klibs",
        "ru.astrainteractive.astralibs"
    ).forEach { pattern ->
        relocate(
            pattern = pattern,
            destination = "${requireProjectInfo.group}.shade.$pattern"
        )
    }
    relocate("org.bstats", requireProjectInfo.group)
}
