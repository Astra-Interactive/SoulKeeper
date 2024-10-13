package ru.astrainteractive.aspekt.module.souls.database.dao

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.aspekt.module.souls.database.model.Soul
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import java.io.File

internal interface SoulReader {
    fun read(soul: Soul): Result<ItemStackSoul>
}

internal interface SoulWriter {
    fun write(soul: ItemStackSoul): Result<Unit>

    fun delete(soul: Soul)
}

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
        configuration.save(soul.soul.file)
    }.onFailure { "#read ${it.message}. ${it.cause}" }

    override fun read(soul: Soul) = kotlin.runCatching {
        ItemStackSoul(
            items = soul.configuration.getList("items") as List<ItemStack>,
            soul = soul
        )
    }.onFailure { "#read ${it.message}. ${it.cause}" }

    override fun delete(soul: Soul) {
        soul.file.delete()
    }
}
