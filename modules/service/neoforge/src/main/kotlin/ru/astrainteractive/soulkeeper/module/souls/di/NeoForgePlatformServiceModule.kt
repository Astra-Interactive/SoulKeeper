package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.bridge.MinecraftPlatformServer
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.soulkeeper.core.platform.NeoForgeExperienced
import ru.astrainteractive.soulkeeper.core.platform.NeoForgeIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.core.platform.api.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.armorstand.api.ArmorStandDisplay
import ru.astrainteractive.soulkeeper.module.souls.armorstand.stub.StubArmorStandDisplay
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.api.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.soul.NeoForgePlayerInventory
import ru.astrainteractive.soulkeeper.module.souls.soul.api.PlayerInventory

class NeoForgePlatformServiceModule : PlatformServiceModule {
    override val platformServer: PlatformServer = MinecraftPlatformServer
    override val eventProvider: EventProvider = NeoForgeEventProvider
    override val isDeadPlayerProvider: IsDeadPlayerProvider = NeoForgeIsDeadPlayerProvider

    override val armorStandDisplay: ArmorStandDisplay = StubArmorStandDisplay
    override val onlineMinecraftPlayerExperiencedFactory = NeoForgeExperienced.OnlineMinecraftPlayerFactory
    override val playerInventory: PlayerInventory = NeoForgePlayerInventory(isDeadPlayerProvider)
}
