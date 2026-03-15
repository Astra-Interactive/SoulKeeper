package ru.astrainteractive.soulkeeper.command.soulkrate

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.StringFormat
import ru.astrainteractive.astralibs.command.api.brigadier.command.MultiplatformCommand
import ru.astrainteractive.astralibs.command.api.registrar.CommandRegistrarContext
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.kyori.unwrap
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.module.souls.domain.AddSoulItemsIntoInventoryUseCase
import ru.astrainteractive.soulkeeper.module.souls.krate.PlayerSoulKrate
import java.io.File
import java.time.Instant
import java.util.*

@Suppress("LongParameterList")
internal class SoulKrateCommandRegistrar(
    private val registrarContext: CommandRegistrarContext,
    private val multiplatformCommand: MultiplatformCommand<*>,
    private val stringFormat: StringFormat,
    private val dataFolder: File,
    private val ioScope: CoroutineScope,
    private val addSoulItemsIntoInventoryUseCase: AddSoulItemsIntoInventoryUseCase,
    translationKrate: CachedKrate<PluginTranslation>,
    kyoriKrate: CachedKrate<KyoriComponentSerializer>
) : Logger by JUtiltLogger("SoulKrateCommandRegistrar"),
    KyoriComponentSerializer by kyoriKrate.unwrap() {
    private val translation by translationKrate
    private fun createNode(): LiteralArgumentBuilder<*> {
        return with(multiplatformCommand) {
            command("soulkrate") {
                argument("uuid", StringArgumentType.string()) { uuidArg ->
                    argument("instant", LongArgumentType.longArg()) { instantArg ->
                        argument("index", IntegerArgumentType.integer()) { indexArg ->
                            runs { ctx ->
                                ctx.requirePermission(PluginPermission.LoadSouls)
                                val player = ctx.requirePlayer()
                                val instant = ctx.requireArgument(instantArg).let(Instant::ofEpochSecond)
                                val index = ctx.requireArgument(indexArg)
                                val uuid = ctx.requireArgument(uuidArg).let(UUID::fromString)
                                ioScope.launch {
                                    val soul = PlayerSoulKrate(
                                        stringFormat = stringFormat,
                                        dataFolder = dataFolder,
                                        createdAt = instant,
                                        ownerUUID = uuid,
                                        readIndex = index
                                    ).getValue()
                                    if (soul == null) {
                                        player.sendMessage(translation.souls.soulNotFound.component)
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

    fun register() {
        registrarContext.registerWhenReady(createNode())
    }
}
