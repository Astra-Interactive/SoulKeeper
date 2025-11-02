package ru.astrainteractive.soulkeeper.command.reload

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import ru.astrainteractive.astralibs.command.api.registrar.PaperCommandRegistrarContext
import ru.astrainteractive.astralibs.command.api.util.command
import ru.astrainteractive.astralibs.command.api.util.requirePermission
import ru.astrainteractive.astralibs.command.api.util.runs
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.kyori.unwrap
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation

internal class SoulsReloadCommandRegistrar(
    private val plugin: LifecyclePlugin,
    translationKrate: CachedKrate<PluginTranslation>,
    kyoriKrate: CachedKrate<KyoriComponentSerializer>,
    private val registrarContext: PaperCommandRegistrarContext
) : KyoriComponentSerializer by kyoriKrate.unwrap() {
    private val translation by translationKrate

    private fun createNode(): LiteralCommandNode<CommandSourceStack> {
        return command("skreload") {
            runs { ctx ->
                ctx.requirePermission(PluginPermission.Reload)
                ctx.source.sender.sendMessage(translation.general.reload.component)
                plugin.onReload()
                ctx.source.sender.sendMessage(translation.general.reloadComplete.component)
            }
        }.build()
    }

    fun register() {
        registrarContext.registerWhenReady(createNode())
    }
}
