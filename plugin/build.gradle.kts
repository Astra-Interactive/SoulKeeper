import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile
import ru.astrainteractive.gradleplugin.property.PropertyValue.Companion.secretProperty
import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireProjectInfo
import ru.astrainteractive.gradleplugin.property.extension.PrimitivePropertyValueExt.requireInt
import ru.astrainteractive.gradleplugin.property.extension.PrimitivePropertyValueExt.requireString

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.github.goooler.shadow")
    alias(libs.plugins.klibs.minecraft.shadow)
    alias(libs.plugins.klibs.minecraft.resource.processor)
}

dependencies {
    // Kotlin
    implementation(libs.bundles.kotlin)
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
    implementation(projects.modules.dao)
    implementation(projects.modules.worker)
}
val destination = File("/run/media/makeevrserg/WDGOLD2TB/MinecraftServers/Servers/conf.smp/smp/plugins/")
    .takeIf(File::exists)
    ?: File(rootDir, "jars")

minecraftProcessResource {
    bukkit()
}

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

tasks.register("uploadSsh") {
    val projectInfo = requireProjectInfo

    dependsOn(shadowJar)
    mustRunAfter(shadowJar)

    val sshClient = SSHClient().apply {
        this.addHostKeyVerifier(PromiscuousVerifier())
        this.connect(
            secretProperty("hostname").requireString,
            secretProperty("port").requireInt
        )
        this.authPassword(
            secretProperty("username").requireString,
            secretProperty("password").requireString
        )
    }
    val sftpClient = sshClient.newSFTPClient()
    val jarFile = rootDir
        .resolve("jars")
        .resolve("${projectInfo.name}-${projectInfo.versionString}.jar")
    try {
        runBlocking {
            withTimeout(5000) {
                sftpClient.use { client ->
                    val destination = secretProperty("destination").requireString
                    println("Deleting ${"$destination/${jarFile.name}"}")
                    runCatching { client.rm("$destination/${jarFile.name}") }
                    client.put(FileSystemFile(jarFile), destination)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        if (sshClient.isConnected) sshClient.disconnect()
    }
}
