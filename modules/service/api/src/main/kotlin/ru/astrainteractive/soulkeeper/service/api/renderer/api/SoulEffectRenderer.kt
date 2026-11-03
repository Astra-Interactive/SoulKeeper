package ru.astrainteractive.soulkeeper.service.api.renderer.api

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul

interface SoulEffectRenderer {
    suspend fun renderOnce(player: OnlineKPlayer, souls: List<DatabaseSoul>)
}
