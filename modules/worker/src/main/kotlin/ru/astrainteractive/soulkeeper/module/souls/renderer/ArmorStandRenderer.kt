package ru.astrainteractive.soulkeeper.module.souls.renderer

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandStubUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.renderer.api.SoulEffectRenderer

internal class ArmorStandRenderer(
    soulsConfigKrate: Krate<SoulsConfig>,
    private val showArmorStandUseCase: ShowArmorStandUseCase,
) : SoulEffectRenderer {
    private val soulsConfig by soulsConfigKrate

    private val soulByArmorStandId = HashMap<Long, Int>()

    private fun rememberSoulArmorStandId(soul: DatabaseSoul) {
        if (showArmorStandUseCase is ShowArmorStandStubUseCase) return
        if (soulsConfig.displaySoulTitles) return
        soulByArmorStandId.getOrPut(soul.id) { showArmorStandUseCase.generateEntityId() }
    }

    override suspend fun renderOnce(player: Player, souls: List<DatabaseSoul>) {
        showArmorStandUseCase.destroy(player, soulByArmorStandId.values)
        souls
            .onEach(::rememberSoulArmorStandId)
            .forEach { soul ->
                val armorStandId = soulByArmorStandId[soul.id] ?: return@forEach
                showArmorStandUseCase.show(armorStandId, player, soul)
            }
    }

    fun onDisable() {
        Bukkit.getOnlinePlayers().forEach { player ->
            showArmorStandUseCase.destroy(player, soulByArmorStandId.values)
        }
        soulByArmorStandId.clear()
    }
}
