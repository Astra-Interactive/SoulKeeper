import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import ru.astrainteractive.gradleplugin.model.Developer
import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireJinfo
import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireProjectInfo

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.neoforgegradle)
    alias(libs.plugins.klibs.minecraft.resource.processor)
    alias(libs.plugins.gradle.shadow)
}

dependencies {
    // Kotlin
    shadow(libs.kotlin.coroutines.core)
    // AstraLibs
    shadow(libs.minecraft.astralibs.core)
    shadow(libs.minecraft.astralibs.core.neoforge)
    shadow(libs.minecraft.astralibs.command)
    shadow(libs.kotlin.serialization.kaml)
    shadow(libs.klibs.mikro.core)
    shadow(libs.klibs.kstorage)
    shadow(libs.driver.h2)
    shadow(libs.driver.jdbc)
    shadow(libs.kyori.plain)
    shadow(libs.kyori.legacy)
    shadow(libs.kyori.gson)
    // Local
    shadow(projects.modules.core)
    shadow(projects.modules.commandNeoforge)
    shadow(projects.modules.dao)
    shadow(projects.modules.service)
    shadow(projects.modules.serviceNeoforge)
    shadow(projects.modules.eventNeoforge)
}

repositories {
    mavenLocal()
}

dependencies {
    compileOnly(libs.minecraft.neoforgeversion)
}

tasks.withType<JavaCompile> {
    javaCompiler.set(
        javaToolchains.compilerFor {
            requireJinfo.jtarget.majorVersion
                .let(JavaLanguageVersion::of)
                .let(languageVersion::set)
        }
    )
}

configurations.runtimeElements {
    setExtendsFrom(emptySet())
}

val destination = rootDir
    .resolve("build")
    .resolve("neoforge")
    .resolve("mods")
    .takeIf(File::exists)
    ?: File(rootDir, "jars")

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

val shadowJar by tasks.getting(ShadowJar::class) {
    mergeServiceFiles()
    dependsOn(tasks.named<ProcessResources>("processResources"))
    configurations = listOf(project.configurations.shadow.get())
    isReproducibleFileOrder = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier = null as String?
    archiveVersion = requireProjectInfo.versionString
    archiveBaseName = "${requireProjectInfo.name}-neoforge"
    destinationDirectory = destination
    dependencies {
        // deps
        exclude(dependency("org.jetbrains:annotations"))
        // deps paths
        // Without kotlin deps
        exclude("kotlin/**")

        exclude("co/touchlab/stately/**")
        exclude("club/minnced/opus/**")
        exclude("com/google/**")
        exclude("com/sun/**")
        exclude("google/protobuf/**")
        exclude("io/github/**")
        exclude("io/javalin/**")
        exclude("jakarta/servlet/**")
        exclude("javax/annotation/**")
        exclude("javax/servlet/**")
        exclude("natives/**")
        exclude("nl/altindag/**")
        exclude("org/eclipse/**")
        exclude("org/apache/commons/logging/**")
        exclude("org/bouncycastle/**")
        exclude("org/checkerframework/**")
        exclude("org/conscrypt/**")
        exclude("tomp2p/opuswrapper/**")
        exclude("DebugProbesKt.bin")
        exclude("_COROUTINE/**")
        // meta
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/com.android.tools/**")
        exclude("META-INF/gradle-plugins/**")
        exclude("META-INF/maven/**")
        exclude("META-INF/proguard/**")
        exclude("META-INF/native/**")
        exclude("META-INF/**LICENCE**")
    }
    // Be sure to relocate EXACT PACKAGES!!
    // For example, relocate org.some.package instead of org
    // Becuase relocation org will break other non-relocated dependencies such as org.minecraft
    listOf(
        "com.fasterxml",
        "net.kyori",
        "org.h2",
        "com.neovisionaries",
        "gnu.trove",
        "org.json",
        "org.apache",
        "org.telegram",
        "okhttp3",
        "net.dv8tion",
        "okio",
        "org.slf4j",
        "kotlinx",
        "it.krzeminski",
        "net.thauvin",
        "org.jetbrains.exposed.dao",
        "org.jetbrains.exposed.exceptions",
        "org.jetbrains.exposed.sql",
        "org.jetbrains.exposed.jdbc",
        "org.jetbrains.kotlin",
        "org.jetbrains.kotlinx",
        "ch.qos",
        "com.arkivanov",
        "com.ibm",
        "dev.icerock",
        "javax.xml",
        "org.w3c",
        "org.xml",
        "com.charleskorn.kaml",
        "ru.astrainteractive.klibs",
        "ru.astrainteractive.astralibs",
        "club.minnced.discord",
        "club.minnced.opus",
    ).forEach { pattern -> relocate(pattern, "${requireProjectInfo.group}.shade.$pattern") }
}
