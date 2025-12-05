package ru.astrainteractive.soulkeeper.module.souls.di

import net.neoforged.neoforge.common.NeoForge
import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.Experienced
import ru.astrainteractive.soulkeeper.module.souls.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider

class NeoForgePlatformServiceModule(
    coreModule: CoreModule,
    soulsDaoModule: SoulsDaoModule,
) : PlatformServiceModule {
    override val platformServer: PlatformServer
        get() = TODO("Not yet implemented")
    override val effectEmitter: EffectEmitter
        get() = TODO("Not yet implemented")
    override val minecraftNativeBridge: MinecraftNativeBridge
        get() = TODO("Not yet implemented")
    override val eventProvider: EventProvider
        get() = TODO("Not yet implemented")
    override val isDeadPlayerProvider: IsDeadPlayerProvider
        get() = TODO("Not yet implemented")
    override val onlineMinecraftPlayerExperiencedFactory: Experienced.Factory<OnlineMinecraftPlayer>
        get() = TODO("Not yet implemented")
    override val showArmorStandUseCase: ShowArmorStandUseCase
        get() = TODO("Not yet implemented")
    override val pickUpItemsUseCase: PickUpItemsUseCase
        get() = TODO("Not yet implemented")

    init {
        println(NeoForge.EVENT_BUS)
    }
}
