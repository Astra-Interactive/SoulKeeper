package ru.astrainteractive.aspekt.module.souls.database.dao

import org.bukkit.Location
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.aspekt.module.souls.database.model.Soul
import java.util.UUID

internal interface SoulsDao {
    suspend fun getSouls(): Result<List<Soul>>

    suspend fun getPlayerSouls(uuid: UUID): Result<List<Soul>>

    suspend fun insertSoul(itemStackSoul: ItemStackSoul): Result<Unit>

    suspend fun getSoulsNear(location: Location, radius: Int): Result<List<Soul>>

    suspend fun deleteSoul(soul: Soul): Result<Unit>

    suspend fun updateSoul(soul: Soul): Result<Unit>

    suspend fun updateSoul(itemStackSoul: ItemStackSoul): Result<Unit>

    suspend fun toItemStackSoul(soul: Soul): Result<ItemStackSoul>
}
