package ru.astrainteractive.aspekt.module.souls.domain.di.factory

import org.bukkit.Bukkit
import ru.astrainteractive.aspekt.di.CoreModule
import ru.astrainteractive.aspekt.module.souls.domain.armorstand.ShowArmorStandStubUseCase
import ru.astrainteractive.aspekt.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.aspekt.module.souls.domain.armorstand.ShowArmorStandUseCaseImpl

internal class ShowArmorStandUseCaseFactory(
    private val coreModule: CoreModule
) {
    fun create(): ShowArmorStandUseCase {
        if (!Bukkit.getPluginManager().isPluginEnabled("PacketEvents")) {
            return ShowArmorStandStubUseCase
        }
        return ShowArmorStandUseCaseImpl(
            kyoriKrate = coreModule.kyoriComponentSerializer,
            translationKrate = coreModule.translation
        )
    }
}
