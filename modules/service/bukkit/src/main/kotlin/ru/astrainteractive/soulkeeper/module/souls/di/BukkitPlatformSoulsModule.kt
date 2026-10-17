package ru.astrainteractive.soulkeeper.module.souls.di

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.bridge.BukkitPlatformServer
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.platform.BukkitExperienced
import ru.astrainteractive.soulkeeper.core.platform.BukkitIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.core.platform.api.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.armorstand.api.ArmorStandDisplay
import ru.astrainteractive.soulkeeper.module.souls.armorstand.packetevents.PacketEventsArmorStandDisplay
import ru.astrainteractive.soulkeeper.module.souls.armorstand.stub.StubArmorStandDisplay
import ru.astrainteractive.soulkeeper.module.souls.platform.BukkitEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.api.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.soul.BukkitPlayerInventory
import ru.astrainteractive.soulkeeper.module.souls.soul.api.PlayerInventory

class BukkitPlatformSoulsModule(
    coreModule: CoreModule,
    bukkitCoreModule: BukkitCoreModule
) : PlatformSoulsModule {
    override val platformServer: PlatformServer = BukkitPlatformServer()
    override val eventProvider: EventProvider = BukkitEventProvider(
        plugin = bukkitCoreModule.plugin
    )
    override val isDeadPlayerProvider: IsDeadPlayerProvider = BukkitIsDeadPlayerProvider
    override val onlineMinecraftPlayerExperiencedFactory = BukkitExperienced.OnlineMinecraftPlayerFactory

    override val armorStandDisplay: ArmorStandDisplay = when {
        Bukkit.getPluginManager().isPluginEnabled("packetevents") -> PacketEventsArmorStandDisplay(
            translationKrate = coreModule.translation
        )

        else -> StubArmorStandDisplay
    }
    override val playerInventory: PlayerInventory = BukkitPlayerInventory()
}
