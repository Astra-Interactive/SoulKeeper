package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.bridge.MinecraftPlatformServer
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.soulkeeper.core.platform.ForgeExperienced
import ru.astrainteractive.soulkeeper.core.platform.ForgeIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.core.platform.api.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.armorstand.api.ArmorStandDisplay
import ru.astrainteractive.soulkeeper.module.souls.armorstand.stub.StubArmorStandDisplay
import ru.astrainteractive.soulkeeper.module.souls.platform.ForgeEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.api.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.soul.ForgePlayerInventory
import ru.astrainteractive.soulkeeper.module.souls.soul.api.PlayerInventory

class ForgePlatformServiceModule : PlatformServiceModule {
    override val platformServer: PlatformServer = MinecraftPlatformServer
    override val eventProvider: EventProvider = ForgeEventProvider
    override val isDeadPlayerProvider: IsDeadPlayerProvider = ForgeIsDeadPlayerProvider

    override val armorStandDisplay: ArmorStandDisplay = StubArmorStandDisplay
    override val onlineMinecraftPlayerExperiencedFactory = ForgeExperienced.OnlineMinecraftPlayerFactory
    override val playerInventory: PlayerInventory = ForgePlayerInventory(isDeadPlayerProvider)
}
