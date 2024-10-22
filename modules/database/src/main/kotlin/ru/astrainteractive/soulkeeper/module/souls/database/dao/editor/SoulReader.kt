package ru.astrainteractive.soulkeeper.module.souls.database.dao.editor

import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul

internal interface SoulReader {
    fun read(soul: Soul): Result<ItemStackSoul>
}
