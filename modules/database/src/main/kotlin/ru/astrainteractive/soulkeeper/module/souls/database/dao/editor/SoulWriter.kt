package ru.astrainteractive.soulkeeper.module.souls.database.dao.editor

import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul

internal interface SoulWriter {
    fun write(soul: ItemStackSoul): Result<Unit>

    fun delete(soul: Soul)
}
