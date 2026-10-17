package ru.astrainteractive.soulkeeper.module.souls.renderer.armorstand

import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.getValue
import ru.astrainteractive.soulkeeper.core.plugin.model.SoulsConfig
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.armorstand.api.ArmorStandDisplay
import ru.astrainteractive.soulkeeper.module.souls.armorstand.stub.StubArmorStandDisplay
import ru.astrainteractive.soulkeeper.module.souls.renderer.api.SoulEffectRenderer

internal class ArmorStandRenderer(
    soulsConfigKrate: CachedKrate<SoulsConfig>,
    private val armorStandDisplay: ArmorStandDisplay,
    private val platformServer: PlatformServer
) : SoulEffectRenderer {
    private val soulsConfig by soulsConfigKrate

    private val soulByArmorStandId = HashMap<Long, Int>()

    private fun rememberSoulArmorStandId(soul: DatabaseSoul) {
        if (armorStandDisplay is StubArmorStandDisplay) return
        if (soulsConfig.displaySoulTitles) return
        soulByArmorStandId.getOrPut(soul.id) { armorStandDisplay.generateEntityId() }
    }

    override suspend fun renderOnce(player: OnlineKPlayer, souls: List<DatabaseSoul>) {
        armorStandDisplay.destroy(player, soulByArmorStandId.values)
        souls
            .onEach(::rememberSoulArmorStandId)
            .forEach { soul ->
                val armorStandId = soulByArmorStandId[soul.id] ?: return@forEach
                armorStandDisplay.show(armorStandId, player, soul)
            }
    }

    fun onDisable() {
        platformServer.getOnlinePlayers().forEach { player ->
            armorStandDisplay.destroy(player, soulByArmorStandId.values)
        }
        soulByArmorStandId.clear()
    }
}
