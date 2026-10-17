package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.bridge.BukkitPlatformServer
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.platform.BukkitExperienced
import ru.astrainteractive.soulkeeper.core.platform.BukkitIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.core.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.di.factory.ShowArmorStandUseCaseFactory
import ru.astrainteractive.soulkeeper.module.souls.platform.BukkitEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.soul.BukkitPlayerInventory
import ru.astrainteractive.soulkeeper.module.souls.soul.api.PlayerInventory

class BukkitPlatformServiceModule(
    coreModule: CoreModule,
    bukkitCoreModule: BukkitCoreModule
) : PlatformServiceModule {
    override val platformServer: PlatformServer = BukkitPlatformServer()
    override val eventProvider: EventProvider = BukkitEventProvider(
        plugin = bukkitCoreModule.plugin
    )
    override val isDeadPlayerProvider: IsDeadPlayerProvider = BukkitIsDeadPlayerProvider
    override val onlineMinecraftPlayerExperiencedFactory = BukkitExperienced.OnlineMinecraftPlayerFactory

    override val showArmorStandUseCase: ShowArmorStandUseCase = ShowArmorStandUseCaseFactory(coreModule).create()
    override val playerInventory: PlayerInventory = BukkitPlayerInventory()
}
