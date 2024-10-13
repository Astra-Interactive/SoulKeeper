import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireProjectInfo
import ru.astrainteractive.gradleplugin.setupSpigotProcessor

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.github.goooler.shadow")
}

dependencies {
    // Kotlin
    implementation(libs.bundles.kotlin)
    // Spigot dependencies
    compileOnly(libs.minecraft.paper.api)
    implementation(libs.minecraft.bstats)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.orm)
    implementation(libs.klibs.mikro.core)
    implementation(libs.minecraft.astralibs.menu.bukkit)
    implementation(libs.minecraft.astralibs.core.bukkit)
    compileOnly(libs.minecraft.vaultapi)
    compileOnly(libs.driver.h2)
    compileOnly(libs.driver.jdbc)
    compileOnly(libs.driver.mysql)
    // Spigot
    compileOnly(libs.minecraft.luckperms)
    compileOnly(libs.minecraft.discordsrv)
    compileOnly(libs.minecraft.essentialsx)
    // Test
    testImplementation(libs.bundles.testing.kotlin)
    testImplementation(libs.tests.kotlin.test)
    testImplementation(libs.minecraft.mockbukkit)
    implementation(projects.modules.core)
    implementation(projects.modules.menu)
    implementation(projects.modules.broadcast)
    implementation(projects.modules.adminprivate)
    implementation(projects.modules.discordlink)
    implementation(projects.modules.townyDiscord)
    implementation(projects.modules.moneydrop)
    implementation(projects.modules.moneyAdvancements)
    implementation(projects.modules.autocrop)
    implementation(projects.modules.newbee)
    implementation(projects.modules.antiswear)
    implementation(projects.modules.chatgame)
    implementation(projects.modules.economy)
    implementation(projects.modules.souls)
}
val destination = File(")/home/makeevrserg/Desktop/server/data/plugins")
    .takeIf(File::exists)
    ?: File(rootDir, "jars")

setupSpigotProcessor()

val shadowJar = tasks.named<ShadowJar>("shadowJar")
shadowJar.configure {
    if (!destination.exists()) destination.mkdirs()

    val projectInfo = requireProjectInfo
    isReproducibleFileOrder = true
    mergeServiceFiles()
    dependsOn(configurations)
    archiveClassifier.set(null as String?)
    relocate("org.bstats", projectInfo.group)

    minimize {
        exclude(dependency(libs.exposed.jdbc.get()))
        exclude(dependency(libs.exposed.dao.get()))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.version.get()}"))
    }
    archiveVersion.set(projectInfo.versionString)
    archiveBaseName.set(projectInfo.name)
    destination.also(destinationDirectory::set)
}
