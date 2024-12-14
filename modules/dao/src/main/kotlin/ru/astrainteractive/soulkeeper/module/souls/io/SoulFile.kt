package ru.astrainteractive.soulkeeper.module.souls.io

import ru.astrainteractive.soulkeeper.module.souls.io.model.BukkitSoul
import ru.astrainteractive.soulkeeper.module.souls.io.model.Soul

internal interface SoulFile {
    fun read(soul: Soul): Result<BukkitSoul>

    fun write(soul: BukkitSoul): Result<Unit>

    fun delete(soul: Soul)

    companion object {
        val Soul.fileNameWithoutExtension: String
            get() = "${ownerUUID}_${createdAt.toEpochMilli()}"
    }
}
