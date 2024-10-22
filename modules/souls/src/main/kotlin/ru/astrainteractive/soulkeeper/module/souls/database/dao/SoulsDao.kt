package ru.astrainteractive.soulkeeper.module.souls.database.dao

import org.bukkit.Location
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul
import java.util.UUID

internal interface SoulsDao {
    suspend fun getSouls(): Result<List<DatabaseSoul>>

    suspend fun getPlayerSouls(uuid: UUID): Result<List<DatabaseSoul>>

    suspend fun insertSoul(soul: ItemStackSoul): Result<Unit>

    suspend fun getSoulsNear(location: Location, radius: Int): Result<List<DatabaseSoul>>

    suspend fun deleteSoul(soul: Soul): Result<Unit>

    suspend fun updateSoul(soul: Soul): Result<Unit>

    suspend fun updateSoul(soul: ItemStackSoul): Result<Unit>

    suspend fun toItemStackSoul(soul: Soul): Result<ItemStackSoul>
}
