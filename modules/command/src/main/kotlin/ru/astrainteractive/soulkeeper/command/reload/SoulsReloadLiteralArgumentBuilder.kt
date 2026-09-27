package ru.astrainteractive.soulkeeper.command.reload

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import ru.astrainteractive.astralibs.command.api.brigadier.command.MultiplatformCommand
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.getValue
import ru.astrainteractive.soulkeeper.command.exception.CommandExceptionHandler
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation

internal class SoulsReloadLiteralArgumentBuilder(
    private val lifecyclePlugin: Lifecycle,
    private val multiplatformCommand: MultiplatformCommand,
    private val commandExceptionHandler: CommandExceptionHandler,
    translationKrate: CachedKrate<PluginTranslation>,
) {
    private val translation by translationKrate

    fun create(): LiteralArgumentBuilder<*> {
        return with(multiplatformCommand) {
            command("skreload") {
                runs(onFailure = commandExceptionHandler::handle) { ctx ->
                    ctx.requirePermission(PluginPermission.Reload)
                    val audience = ctx.getSender()
                    audience.sendMessage(translation.reload.started)
                    lifecyclePlugin.onReload()
                    audience.sendMessage(translation.reload.completed)
                }
            }
        }
    }
}
