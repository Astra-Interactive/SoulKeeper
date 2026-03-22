package ru.astrainteractive.soulkeeper.command.reload

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import ru.astrainteractive.astralibs.command.api.brigadier.command.MultiplatformCommand
import ru.astrainteractive.astralibs.command.api.registrar.CommandRegistrarContext
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.kyori.unwrap
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation

internal class SoulsReloadCommandRegistrar(
    private val lifecyclePlugin: Lifecycle,
    private val registrarContext: CommandRegistrarContext,
    private val multiplatformCommand: MultiplatformCommand,
    translationKrate: CachedKrate<PluginTranslation>,
    kyoriKrate: CachedKrate<KyoriComponentSerializer>
) : KyoriComponentSerializer by kyoriKrate.unwrap() {
    private val translation by translationKrate

    private fun createNode(): LiteralArgumentBuilder<*> {
        return with(multiplatformCommand) {
            command("skreload") {
                runs { ctx ->
                    ctx.requirePermission(PluginPermission.Reload)
                    val audience = ctx.getSender()
                    audience.sendMessage(translation.general.reload.component)
                    lifecyclePlugin.onReload()
                    audience.sendMessage(translation.general.reloadComplete.component)
                }
            }
        }
    }

    fun register() {
        registrarContext.registerWhenReady(createNode())
    }
}
