package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.bridge.MinecraftPlatformServer
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.soulkeeper.core.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.StubShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.ForgeEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.ForgeExperienced
import ru.astrainteractive.soulkeeper.module.souls.platform.ForgeIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.soul.ForgePlayerInventory
import ru.astrainteractive.soulkeeper.module.souls.soul.api.PlayerInventory

class ForgePlatformServiceModule : PlatformServiceModule {
    override val platformServer: PlatformServer = MinecraftPlatformServer
    override val eventProvider: EventProvider = ForgeEventProvider
    override val isDeadPlayerProvider: IsDeadPlayerProvider = ForgeIsDeadPlayerProvider

    override val showArmorStandUseCase: ShowArmorStandUseCase = StubShowArmorStandUseCase
    override val onlineMinecraftPlayerExperiencedFactory = ForgeExperienced.OnlineMinecraftPlayerFactory
    override val playerInventory: PlayerInventory = ForgePlayerInventory(isDeadPlayerProvider)
}
