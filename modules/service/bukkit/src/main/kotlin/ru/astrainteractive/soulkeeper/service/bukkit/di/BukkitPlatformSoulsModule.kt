package ru.astrainteractive.soulkeeper.service.bukkit.di

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.bridge.BukkitPlatformServer
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.platform.BukkitExperienced
import ru.astrainteractive.soulkeeper.core.platform.BukkitIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.core.platform.api.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.service.api.armorstand.api.ArmorStandDisplay
import ru.astrainteractive.soulkeeper.service.api.armorstand.stub.StubArmorStandDisplay
import ru.astrainteractive.soulkeeper.service.api.di.PlatformSoulsModule
import ru.astrainteractive.soulkeeper.service.api.platform.api.EventProvider
import ru.astrainteractive.soulkeeper.service.api.soul.api.PlayerInventory
import ru.astrainteractive.soulkeeper.service.bukkit.armorstand.packetevent.PacketEventsArmorStandDisplay
import ru.astrainteractive.soulkeeper.service.bukkit.platform.BukkitEventProvider
import ru.astrainteractive.soulkeeper.service.bukkit.soul.BukkitPlayerInventory

class BukkitPlatformSoulsModule(
    coreModule: CoreModule,
    bukkitCoreModule: BukkitCoreModule
) : PlatformSoulsModule {
    override val platformServer: PlatformServer = BukkitPlatformServer()
    override val eventProvider: EventProvider = BukkitEventProvider(
        plugin = bukkitCoreModule.plugin
    )
    override val isDeadPlayerProvider: IsDeadPlayerProvider = BukkitIsDeadPlayerProvider
    override val onlineMinecraftPlayerExperiencedFactory = BukkitExperienced.OnlineMinecraftPlayerResolver

    override val armorStandDisplay: ArmorStandDisplay = when {
        Bukkit.getPluginManager().isPluginEnabled("packetevents") -> PacketEventsArmorStandDisplay(
            translationKrate = coreModule.translation
        )

        else -> StubArmorStandDisplay
    }
    override val playerInventory: PlayerInventory = BukkitPlayerInventory()
}
