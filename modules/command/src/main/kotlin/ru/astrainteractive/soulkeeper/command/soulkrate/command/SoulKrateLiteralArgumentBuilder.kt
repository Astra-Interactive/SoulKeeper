package ru.astrainteractive.soulkeeper.command.soulkrate.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.StringFormat
import ru.astrainteractive.astralibs.command.api.brigadier.command.MultiplatformCommand
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.getValue
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.command.error.CommandExceptionHandler
import ru.astrainteractive.soulkeeper.core.plugin.model.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.model.PluginTranslation
import ru.astrainteractive.soulkeeper.data.dao.soul.krate.PlayerSoulKrate
import ru.astrainteractive.soulkeeper.module.souls.soul.api.PlayerInventory
import java.io.File
import kotlin.time.Instant

@Suppress("LongParameterList")
internal class SoulKrateLiteralArgumentBuilder(
    private val multiplatformCommand: MultiplatformCommand,
    private val stringFormat: StringFormat,
    private val dataFolder: File,
    private val ioScope: CoroutineScope,
    private val playerInventory: PlayerInventory,
    private val commandExceptionHandler: CommandExceptionHandler,
    translationKrate: CachedKrate<PluginTranslation>
) : Logger by JUtiltLogger("SoulKrateLiteralArgumentBuilder") {
    private val translation by translationKrate

    private val instantArgumentType = LongArgumentType.longArg()

    fun create(): LiteralArgumentBuilder<*> {
        return with(multiplatformCommand) {
            command("soulkrate") {
                argument("uuid", StringArgumentType.string()) { uuidArg ->
                    argument("instant", instantArgumentType) { instantArg ->
                        argument("index", IntegerArgumentType.integer()) { indexArg ->
                            runs(commandExceptionHandler::onFailure) { ctx ->
                                ctx.requirePermission(PluginPermission.LoadSouls)
                                val player = ctx.requirePlayer()
                                val instant = ctx.requireArgument(instantArg).let(Instant::fromEpochSeconds)
                                val index = ctx.requireArgument(indexArg)
                                val uuid = ctx.requireArgument(uuidArg, UuidArgumentConverter)
                                ioScope.launch {
                                    val soul = PlayerSoulKrate(
                                        stringFormat = stringFormat,
                                        dataFolder = dataFolder,
                                        createdAt = instant,
                                        ownerUUID = uuid,
                                        readIndex = index
                                    ).getValue()
                                    if (soul == null) {
                                        player.sendMessage(translation.soul.notFound)
                                        return@launch
                                    }
                                    playerInventory.addItems(
                                        player = player,
                                        items = soul.items
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
