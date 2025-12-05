package ru.astrainteractive.soulkeeper.module.event.di

import net.neoforged.neoforge.common.NeoForge
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter

class ForgeEventModule(
    coreModule: CoreModule,
    soulsDaoModule: SoulsDaoModule,
    effectEmitter: EffectEmitter
) {
    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            println(NeoForge.EVENT_BUS)
        },
        onDisable = {
        }
    )
}
