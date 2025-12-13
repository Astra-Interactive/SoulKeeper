package ru.astrainteractive.soulkeeper.module.souls.renderer

import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.StubShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.renderer.api.SoulEffectRenderer

internal class ArmorStandRenderer(
    soulsConfigKrate: CachedKrate<SoulsConfig>,
    private val showArmorStandUseCase: ShowArmorStandUseCase,
    private val platformServer: PlatformServer
) : SoulEffectRenderer {
    private val soulsConfig by soulsConfigKrate

    private val soulByArmorStandId = HashMap<Long, Int>()

    private fun rememberSoulArmorStandId(soul: DatabaseSoul) {
        if (showArmorStandUseCase is StubShowArmorStandUseCase) return
        if (soulsConfig.displaySoulTitles) return
        soulByArmorStandId.getOrPut(soul.id) { showArmorStandUseCase.generateEntityId() }
    }

    override suspend fun renderOnce(player: OnlineMinecraftPlayer, souls: List<DatabaseSoul>) {
        showArmorStandUseCase.destroy(player, soulByArmorStandId.values)
        souls
            .onEach(::rememberSoulArmorStandId)
            .forEach { soul ->
                val armorStandId = soulByArmorStandId[soul.id] ?: return@forEach
                showArmorStandUseCase.show(armorStandId, player, soul)
            }
    }

    fun onDisable() {
        platformServer.getOnlinePlayers().forEach { player ->
            showArmorStandUseCase.destroy(player, soulByArmorStandId.values)
        }
        soulByArmorStandId.clear()
    }
}
