package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.bridge.MinecraftPlatformServer
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.domain.ForgeAddSoulItemsIntoInventoryUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.ForgePickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.StubShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.ForgeEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.ForgeExperienced
import ru.astrainteractive.soulkeeper.module.souls.platform.ForgeIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider

class ForgePlatformServiceModule(
    coreModule: CoreModule,
    soulsDaoModule: SoulsDaoModule,
) : PlatformServiceModule {
    override val platformServer: PlatformServer = MinecraftPlatformServer
    override val eventProvider: EventProvider = ForgeEventProvider
    override val isDeadPlayerProvider: IsDeadPlayerProvider = ForgeIsDeadPlayerProvider

    override val showArmorStandUseCase: ShowArmorStandUseCase = StubShowArmorStandUseCase
    override val onlineMinecraftPlayerExperiencedFactory = ForgeExperienced.OnlineMinecraftPlayerFactory
    override val pickUpItemsUseCase: PickUpItemsUseCase = ForgePickUpItemsUseCase(
        collectItemSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectItem },
        soulsDao = soulsDaoModule.soulsDao,
        effectEmitter = coreModule.effectEmitter,
        isDeadPlayerProvider = isDeadPlayerProvider,
        dispatchers = coreModule.dispatchers
    )
    override val addSoulItemsIntoInventoryUseCase = ForgeAddSoulItemsIntoInventoryUseCase(isDeadPlayerProvider)
}
