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
        "DEPENDEPCIES_ADDITIONAL_MINECRAFT_BOT",
        arrayOf()
    )
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
            libs.okio.get()
        ).map(Any::toString)
    )
}
