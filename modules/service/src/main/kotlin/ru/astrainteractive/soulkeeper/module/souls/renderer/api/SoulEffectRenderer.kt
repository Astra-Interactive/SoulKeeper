package ru.astrainteractive.soulkeeper.module.souls.renderer.api

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul

interface SoulEffectRenderer {
    suspend fun renderOnce(player: OnlineMinecraftPlayer, souls: List<DatabaseSoul>)
}
