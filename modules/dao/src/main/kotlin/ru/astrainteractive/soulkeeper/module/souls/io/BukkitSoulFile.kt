package ru.astrainteractive.soulkeeper.module.souls.io

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.io.SoulFile.Companion.fileNameWithoutExtension
import ru.astrainteractive.soulkeeper.module.souls.io.model.BukkitSoul
import ru.astrainteractive.soulkeeper.module.souls.io.model.Soul
import java.io.File

internal class BukkitSoulFile(
    private val folder: File
) : SoulFile,
    Logger by JUtiltLogger("AspeKt-SoulFileEditor") {
    private val Soul.file: File
        get() {
            if (!folder.exists()) folder.mkdirs()
            val file = folder.resolve("$fileNameWithoutExtension.yml")
            if (!file.exists()) file.createNewFile()
            return file
        }

    private val Soul.configuration
        get() = YamlConfiguration.loadConfiguration(file)

    override fun read(soul: Soul) = kotlin.runCatching {
        BukkitSoul(
            items = (soul.configuration.getList("items") as? List<ItemStack>?).orEmpty(),
            exp = soul.configuration.getInt("exp"),
            ownerUUID = soul.ownerUUID,
            ownerLastName = soul.ownerLastName,
            createdAt = soul.createdAt,
            isFree = soul.isFree,
            location = soul.location,
        )
    }.onFailure { error(it) { "#read ${it.message}. ${it.cause}" } }

    override fun write(soul: BukkitSoul) = runCatching {
        val configuration = soul.configuration
        configuration.set("items", soul.items)
        configuration.set("exp", soul.exp)
        configuration.save(soul.file)
    }.onFailure { error(it) { "#write ${it.message}. ${it.cause}" } }

    override fun delete(soul: Soul) {
        soul.file.delete()
    }
}
