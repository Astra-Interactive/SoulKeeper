package ru.astrainteractive.soulkeeper.command.exception

import com.mojang.brigadier.context.CommandContext
import ru.astrainteractive.astralibs.command.api.brigadier.command.MultiplatformCommand
import ru.astrainteractive.astralibs.command.api.exception.NoPermissionException
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.kyori.unwrap
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.getValue
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation

class CommandExceptionHandler(
    private val multiplatformCommand: MultiplatformCommand,
    translationKrate: CachedKrate<PluginTranslation>,
    kyoriKrate: CachedKrate<KyoriComponentSerializer>
) : KyoriComponentSerializer by kyoriKrate.unwrap(), Logger by JUtiltLogger("CommandExceptionHandler") {
    private val translation by translationKrate

    fun handle(ctx: CommandContext<Any>, t: Throwable) {
        with(multiplatformCommand) {
            when (t) {
                is NoPermissionException -> ctx.getSender()?.sendMessage(translation.general.noPermission.component)
                else -> ctx.getSender()?.sendMessage(translation.general.wrongUsage.component)
            }
        }
    }
}
