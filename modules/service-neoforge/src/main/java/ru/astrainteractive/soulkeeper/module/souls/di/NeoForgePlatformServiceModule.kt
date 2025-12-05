package ru.astrainteractive.soulkeeper.module.souls.di

import net.neoforged.neoforge.common.NeoForge
import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer

class NeoForgePlatformServiceModule(
)  {

    init {
        println(NeoForge.EVENT_BUS)
    }
}
