package ru.astrainteractive.soulkeeper.service.forge.di

import ru.astrainteractive.astralibs.server.bridge.MinecraftPlatformServer
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.soulkeeper.core.platform.ForgeExperienced
import ru.astrainteractive.soulkeeper.core.platform.ForgeIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.core.platform.api.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.service.api.armorstand.api.ArmorStandDisplay
import ru.astrainteractive.soulkeeper.service.api.armorstand.stub.StubArmorStandDisplay
import ru.astrainteractive.soulkeeper.service.api.di.PlatformSoulsModule
import ru.astrainteractive.soulkeeper.service.api.platform.api.EventProvider
import ru.astrainteractive.soulkeeper.service.api.soul.api.PlayerInventory
import ru.astrainteractive.soulkeeper.service.forge.platform.ForgeEventProvider
import ru.astrainteractive.soulkeeper.service.forge.soul.ForgePlayerInventory

class ForgePlatformSoulsModule : PlatformSoulsModule {
    override val platformServer: PlatformServer = MinecraftPlatformServer
    override val eventProvider: EventProvider = ForgeEventProvider
    override val isDeadPlayerProvider: IsDeadPlayerProvider = ForgeIsDeadPlayerProvider

    override val armorStandDisplay: ArmorStandDisplay = StubArmorStandDisplay
    override val onlineMinecraftPlayerExperiencedFactory = ForgeExperienced.OnlineMinecraftPlayerResolver
    override val playerInventory: PlayerInventory = ForgePlayerInventory(isDeadPlayerProvider)
}
