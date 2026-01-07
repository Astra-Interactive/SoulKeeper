@file:Suppress("UnusedPrivateMember")

import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireProjectInfo

plugins {
    kotlin("jvm")
    id("ru.astrainteractive.gradleplugin.java.version")
    id("com.github.gmazzo.buildconfig")
}

buildConfig {
    className("BuildKonfig")
    packageName("${requireProjectInfo.group}.buildkonfig")
    useKotlinOutput { internalVisibility = false }
    buildConfigField("group", requireProjectInfo.group)
    buildConfigField("version", requireProjectInfo.versionString)

    buildConfigField(
        "DEPENDEPCIES",
        arrayOf(
            libs.kotlin.coroutines.core.get(),
            libs.kotlin.serialization.json.get(),
            libs.kotlin.serialization.kaml.get(),
            libs.kotlin.serialization.core.get(),
            libs.exposed.core.get(),
            libs.exposed.dao.get(),
            libs.exposed.jdbc.get(),
            libs.exposed.java.time.get(),
            libs.minecraft.packetevents.get(),
            libs.driver.h2.get(),
            libs.driver.jdbc.get(),
            libs.driver.mysql.get(),
        ).map(Any::toString)
    )
    buildConfigField(
        "EXCLUDED_TRANSITIVE_DEPENDENCIES",
        arrayOf(
            "org.bstats:bstats-base",
            "org.bstats:bstats-bukkit",
            "org.jetbrains.kotlin:kotlin-stdlib",
            "com.alessiodp.libby:libby-standalone",
            "com.alessiodp.libby:libby-core",
            "io.papermc.paper:paper-api",
            "io.papermc:paperlib",
            "org.slf4j:slf4j-api",
            "net.kyori:adventure-api",
            "net.kyori:adventure-key",
            "net.kyori:adventure-nbt",
            "net.kyori:examination-api",
            "net.kyori:examination-string",
        ).map(Any::toString)
    )
    buildConfigField(
        "REPOSITORIES",
        arrayOf(
            "https://repo1.maven.org/maven2/",
            "https://oss.sonatype.org/content/groups/public/",
            "https://repo.opencollab.dev/main/",
            "https://repo.codemc.io/repository/maven-releases/"
        ).map(Any::toString)
    )
}
