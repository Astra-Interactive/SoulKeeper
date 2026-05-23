package ru.astrainteractive.soulkeeper.module.souls.domain.di.factory

import org.bukkit.Bukkit
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.PacketEventsShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.StubShowArmorStandUseCase

internal class ShowArmorStandUseCaseFactory(
    private val coreModule: CoreModule
) {
    fun create(): ShowArmorStandUseCase {
        if (!Bukkit.getPluginManager().isPluginEnabled("packetevents")) {
            return StubShowArmorStandUseCase
        }
        return PacketEventsShowArmorStandUseCase(
            kyoriKrate = coreModule.kyoriComponentSerializer,
            translationKrate = coreModule.translation
        )
    }
}
