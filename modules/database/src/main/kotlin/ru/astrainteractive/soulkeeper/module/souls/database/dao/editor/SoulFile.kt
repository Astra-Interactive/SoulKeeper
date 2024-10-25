package ru.astrainteractive.soulkeeper.module.souls.database.dao.editor

import ru.astrainteractive.soulkeeper.module.souls.database.model.BukkitSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul

internal interface SoulFile {
    fun read(soul: Soul): Result<BukkitSoul>

    fun write(soul: BukkitSoul): Result<Unit>

    fun delete(soul: Soul)

    companion object {
        val Soul.fileNameWithoutExtension: String
            get() = "${ownerUUID}_${createdAt.toEpochMilli()}"
    }
}
