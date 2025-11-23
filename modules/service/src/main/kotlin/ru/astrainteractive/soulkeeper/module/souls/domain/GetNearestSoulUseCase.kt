package ru.astrainteractive.soulkeeper.module.souls.domain

import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.util.toDatabaseLocation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul

/**
 * Get most near soul and available by current player
 */
internal class GetNearestSoulUseCase(
    private val soulsDao: SoulsDao,
    private val minecraftNativeBridge: MinecraftNativeBridge,
) : Logger by JUtiltLogger("AspeKt-GetNearestSoulUseCase"),
    MinecraftNativeBridge by minecraftNativeBridge {
    suspend fun invoke(player: OnlineMinecraftPlayer): DatabaseSoul? {
        return soulsDao.getSoulsNear(player.asLocatable().getLocation().toDatabaseLocation(), 2)
            .getOrNull()
            .orEmpty()
            .firstOrNull { it.isFree || it.ownerUUID == player.uuid }
    }
}
