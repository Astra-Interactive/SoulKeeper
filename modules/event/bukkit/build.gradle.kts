plugins {
    id("org.jetbrains.kotlin.jvm")
    id("ru.astrainteractive.gradleplugin.linter")
    id("ru.astrainteractive.gradleplugin.java.version")
}

dependencies {
    compileOnly(libs.minecraft.paper.api)

    implementation(libs.klibs.mikro.core)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.bukkit)
    implementation(projects.modules.core.api)
    implementation(projects.modules.core.bukkit)
    implementation(projects.modules.data.dao)
    implementation(projects.modules.service.api)
}
