package ru.astrainteractive.soulkeeper.command.soulkrate

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
import ru.astrainteractive.soulkeeper.core.command.CommandExceptionHandler
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.module.souls.domain.AddSoulItemsIntoInventoryUseCase
import ru.astrainteractive.soulkeeper.module.souls.krate.PlayerSoulKrate
import java.io.File
import java.time.Instant

@Suppress("LongParameterList")
internal class SoulKrateLiteralArgumentBuilder(
    private val multiplatformCommand: MultiplatformCommand,
    private val stringFormat: StringFormat,
    private val dataFolder: File,
    private val ioScope: CoroutineScope,
    private val addSoulItemsIntoInventoryUseCase: AddSoulItemsIntoInventoryUseCase,
    private val commandExceptionHandler: CommandExceptionHandler,
    translationKrate: CachedKrate<PluginTranslation>
) : Logger by JUtiltLogger("SoulKrateLiteralArgumentBuilder") {
    private val translation by translationKrate

    /** Seconds outside this range make [Instant.ofEpochSecond] throw, so Brigadier rejects them while parsing. */
    private val instantArgumentType = LongArgumentType.longArg(Instant.MIN.epochSecond, Instant.MAX.epochSecond)

    fun create(): LiteralArgumentBuilder<*> {
        return with(multiplatformCommand) {
            command("soulkrate") {
                argument("uuid", StringArgumentType.string()) { uuidArg ->
                    argument("instant", instantArgumentType) { instantArg ->
                        argument("index", IntegerArgumentType.integer()) { indexArg ->
                            runs(commandExceptionHandler::handle) { ctx ->
                                ctx.requirePermission(PluginPermission.LoadSouls)
                                val player = ctx.requirePlayer()
                                val instant = ctx.requireArgument(instantArg).let(Instant::ofEpochSecond)
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
                                    addSoulItemsIntoInventoryUseCase.invoke(
                                        player = player,
                                        soul = soul,
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
