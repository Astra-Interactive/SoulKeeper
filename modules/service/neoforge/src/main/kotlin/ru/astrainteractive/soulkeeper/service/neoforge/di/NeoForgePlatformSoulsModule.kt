package ru.astrainteractive.soulkeeper.service.neoforge.di

import ru.astrainteractive.astralibs.server.bridge.MinecraftPlatformServer
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.soulkeeper.core.platform.NeoForgeExperienced
import ru.astrainteractive.soulkeeper.core.platform.NeoForgeIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.core.platform.api.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.service.api.armorstand.api.ArmorStandDisplay
import ru.astrainteractive.soulkeeper.service.api.armorstand.stub.StubArmorStandDisplay
import ru.astrainteractive.soulkeeper.service.api.di.PlatformSoulsModule
import ru.astrainteractive.soulkeeper.service.api.platform.api.EventProvider
import ru.astrainteractive.soulkeeper.service.api.soul.api.PlayerInventory
import ru.astrainteractive.soulkeeper.service.neoforge.platform.NeoForgeEventProvider
import ru.astrainteractive.soulkeeper.service.neoforge.soul.NeoForgePlayerInventory

class NeoForgePlatformSoulsModule : PlatformSoulsModule {
    override val platformServer: PlatformServer = MinecraftPlatformServer
    override val eventProvider: EventProvider = NeoForgeEventProvider
    override val isDeadPlayerProvider: IsDeadPlayerProvider = NeoForgeIsDeadPlayerProvider

    override val armorStandDisplay: ArmorStandDisplay = StubArmorStandDisplay
    override val onlineMinecraftPlayerExperiencedFactory = NeoForgeExperienced.OnlineMinecraftPlayerResolver
    override val playerInventory: PlayerInventory = NeoForgePlayerInventory(isDeadPlayerProvider)
}
