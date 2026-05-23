package ru.astrainteractive.soulkeeper.module.souls.renderer.api

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul

interface SoulEffectRenderer {
    suspend fun renderOnce(player: OnlineKPlayer, souls: List<DatabaseSoul>)
}
