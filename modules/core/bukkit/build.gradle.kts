plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("ru.astrainteractive.klibs.rules.gradle.linter")
    id("ru.astrainteractive.gradleplugin.java.version")
}

linter {
    unit(ru.astrainteractive.klibs.rules.core.unit.model.UnitRole.CorePlatform())
    rootPackage.set("ru.astrainteractive.soulkeeper.core")
}

dependencies {
    implementation(libs.klibs.kstorage)

    compileOnly(libs.minecraft.bstats)
    compileOnly(libs.minecraft.paper.api)

    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.core)
    implementation(libs.klibs.mikro.extensions)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.bukkit)
    implementation(libs.minecraft.astralibs.menu.bukkit)
    implementation(projects.modules.core.api)
}
