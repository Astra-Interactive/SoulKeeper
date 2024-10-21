package ru.astrainteractive.aspekt.module.souls.database.dao.editor

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.aspekt.module.souls.database.model.Soul
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import java.io.File

internal class SoulFileEditor(
    private val folder: File
) : SoulReader, SoulWriter, Logger by JUtiltLogger("AspeKt-SoulFileEditor") {
    private val Soul.file
        get() = folder.resolve("${ownerUUID}_${createdAt.toEpochMilli()}.yml")

    private val Soul.configuration
        get() = YamlConfiguration.loadConfiguration(file)

    override fun write(soul: ItemStackSoul) = runCatching {
        val configuration = soul.soul.configuration
        configuration.set("items", soul.items)
        configuration.set("exp", soul.exp)
        configuration.save(soul.soul.file)
    }.onFailure { "#read ${it.message}. ${it.cause}" }

    override fun delete(soul: Soul) {
        soul.file.delete()
    }

    override fun read(soul: Soul) = kotlin.runCatching {
        ItemStackSoul(
            items = soul.configuration.getList("items") as List<ItemStack>,
            soul = soul,
            exp = soul.configuration.getInt("exp")
        )
    }.onFailure { "#read ${it.message}. ${it.cause}" }
}
