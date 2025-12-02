package ru.astrainteractive.soulkeeper

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.neoforged.bus.api.EventPriority
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import ru.astrainteractive.astralibs.event.flowEvent
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.server.util.NeoForgeUtil
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.di.RootModule
import javax.annotation.ParametersAreNonnullByDefault

@Mod("soulkeeper")
@ParametersAreNonnullByDefault
class ForgeEntryPoint :
    Logger by JUtiltLogger("SoulKeeper-ForgeEntryPoint"),
    Lifecycle {
    private val rootModule = RootModule(this)

    override fun onEnable() {
        info { "#onEnable" }
        rootModule.lifecycle.onEnable()
    }

    override fun onDisable() {
        info { "#onDisable" }
        rootModule.lifecycle.onDisable()
    }

    override fun onReload() {
        info { "#onReload" }
        rootModule.lifecycle.onReload()
    }

    val serverStartedEvent = flowEvent<ServerStartedEvent>(EventPriority.HIGHEST)
        .onEach {
            info { "#serverStartedEvent" }
            onEnable()
        }.launchIn(rootModule.coreModule.unconfinedScope)

    val serverStoppingEvent = flowEvent<ServerStoppingEvent>(EventPriority.HIGHEST)
        .onEach {
            info { "#serverStoppingEvent" }
            onDisable()
        }.launchIn(rootModule.coreModule.unconfinedScope)

    init {
        NeoForgeUtil.bootstrap()
    }
}
