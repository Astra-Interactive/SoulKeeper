package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.bridge.MinecraftPlatformServer
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.soulkeeper.core.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.StubShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeExperienced
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.soul.NeoForgePlayerInventory
import ru.astrainteractive.soulkeeper.module.souls.soul.api.PlayerInventory

class NeoForgePlatformServiceModule : PlatformServiceModule {
    override val platformServer: PlatformServer = MinecraftPlatformServer
    override val eventProvider: EventProvider = NeoForgeEventProvider
    override val isDeadPlayerProvider: IsDeadPlayerProvider = NeoForgeIsDeadPlayerProvider

    override val showArmorStandUseCase: ShowArmorStandUseCase = StubShowArmorStandUseCase
    override val onlineMinecraftPlayerExperiencedFactory = NeoForgeExperienced.OnlineMinecraftPlayerFactory
    override val playerInventory: PlayerInventory = NeoForgePlayerInventory(isDeadPlayerProvider)
}
