package ru.astrainteractive.soulkeeper

import kotlinx.coroutines.cancel
import net.neoforged.fml.common.Mod
import ru.astrainteractive.astralibs.lifecycle.ForgeLifecycleServer
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.di.RootModule
import javax.annotation.ParametersAreNonnullByDefault

@Mod("soulkeeper")
@ParametersAreNonnullByDefault
class ForgeEntryPoint :
    ForgeLifecycleServer(),
    Logger by JUtiltLogger("SoulKeeper-ForgeEntryPoint"),
    Lifecycle {
    private val rootModule = RootModule(this)

    override fun onEnable() {
        rootModule.lifecycle.onEnable()
    }

    override fun onDisable() {
        rootModule.lifecycle.onDisable()
        rootModule.coreModule.unconfinedScope.cancel()
    }

    override fun onReload() {
        rootModule.lifecycle.onReload()
    }
}
