import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import ru.astrainteractive.gradleplugin.model.Developer
import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireJinfo
import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireProjectInfo

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.gradle.neoforgegradle)
    alias(libs.plugins.klibs.minecraft.resource.processor)
    alias(libs.plugins.gradle.shadow)
}

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.coroutines.core)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.neoforge)
    implementation(libs.minecraft.astralibs.command)
    implementation(libs.kotlin.serialization.kaml)
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.kstorage)
    implementation(libs.driver.h2)
    implementation(libs.driver.jdbc)
    implementation(libs.minecraft.kyori.plain)
    implementation(libs.minecraft.kyori.legacy)
    implementation(libs.minecraft.kyori.gson)
    // Local
    implementation(projects.modules.core)
    implementation(projects.modules.commandNeoforge)
    implementation(projects.modules.dao)
    implementation(projects.modules.service)
    implementation(projects.modules.serviceNeoforge)
    implementation(projects.modules.eventNeoforge)
    implementation(projects.modules.buildKonfig)
    implementation(projects.modules.libLoader)
}

tasks.named<ProcessResources>("processResources") {
    filteringCharset = "UTF-8"
    duplicatesStrategy = DuplicatesStrategy.WARN
    val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
    val resDirs = sourceSets
        .map(SourceSet::getResources)
        .map(SourceDirectorySet::getSrcDirs)
    from(resDirs) {
        include("META-INF/neoforge.mods.toml")
        expand(
            mapOf(
                "minecraft_version" to libs.versions.minecraft.mojang.version.get(),
                "minecraft_version_range" to listOf(libs.versions.minecraft.mojang.version.get())
                    .joinToString(","),
                "neo_version" to "neo_version",
                "neo_version_range" to "[${libs.versions.minecraft.neoforgeversion.get()},)",
                "mod_id" to requireProjectInfo.name.lowercase(),
                "mod_name" to requireProjectInfo.name,
                "mod_license" to "mod_license",
                "mod_version" to requireProjectInfo.versionString,
                "mod_authors" to requireProjectInfo.developersList
                    .map(Developer::id)
                    .joinToString(","),
                "mod_description" to requireProjectInfo.description
            )
        )
    }
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
        .resolve("mods")
        .takeIf(File::exists)
        ?: File(rootDir, "jars").also(File::mkdirs)
    dependencies {
        // deps
        include(dependency(libs.minecraft.astralibs.core.asProvider()))
        include(dependency(libs.minecraft.astralibs.core.neoforge))
        include(dependency(libs.minecraft.astralibs.command.asProvider()))
        include(dependency(libs.klibs.kstorage))
        include(dependency(libs.klibs.mikro.core))
        include(dependency(libs.klibs.mikro.extensions))
        // modules
        include(dependency(projects.modules.buildKonfig))
        include(dependency(projects.modules.commandNeoforge))
        include(dependency(projects.modules.core))
        include(dependency(projects.modules.dao))
        include(dependency(projects.modules.eventNeoforge))
        include(dependency(projects.modules.libLoader))
        include(dependency(projects.modules.service))
        include(dependency(projects.modules.serviceNeoforge))
        // core
        include(dependency("com.alessiodp.libby:libby-core:.*"))
        include(dependency("com.alessiodp.libby:libby-standalone:.*"))
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

java.toolchain.languageVersion = JavaLanguageVersion.of(requireJinfo.jtarget.majorVersion)

dependencies {
    compileOnly(libs.minecraft.neoforgeversion)
}

configurations.runtimeElements {
    setExtendsFrom(emptySet())
}
