package ru.astrainteractive.soulkeeper.core.di

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import org.bukkit.plugin.java.JavaPlugin
import ru.astrainteractive.astralibs.async.BukkitDispatchers
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.async.DefaultBukkitDispatchers
import ru.astrainteractive.astralibs.event.EventListener
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.astralibs.menu.event.DefaultInventoryClickEvent
import ru.astrainteractive.astralibs.serialization.YamlStringFormat
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.klibs.kstorage.api.impl.DefaultMutableKrate
import ru.astrainteractive.soulkeeper.core.di.factory.ConfigKrateFactory
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation

interface CoreModule {
    val lifecycle: Lifecycle

    val plugin: JavaPlugin
    val eventListener: EventListener

    val dispatchers: BukkitDispatchers
    val scope: CoroutineScope
    val translation: Krate<PluginTranslation>
    val yamlFormat: StringFormat

    val kyoriComponentSerializer: Krate<KyoriComponentSerializer>
    val inventoryClickEventListener: DefaultInventoryClickEvent

    val jsonStringFormat: StringFormat

    class Default(override val plugin: JavaPlugin) : CoreModule, Logger by JUtiltLogger("CoreModule") {
        // Core
        override val eventListener = EventListener.Default()

        override val dispatchers = DefaultBukkitDispatchers(plugin)

        override val scope = CoroutineFeature.Default(Dispatchers.IO)

        override val yamlFormat: StringFormat = YamlStringFormat(
            configuration = Yaml.default.configuration.copy(
                encodeDefaults = true,
                strictMode = false,
                polymorphismStyle = PolymorphismStyle.Property
            ),
        )

        override val translation = ConfigKrateFactory.create(
            fileNameWithoutExtension = "translations",
            stringFormat = yamlFormat,
            dataFolder = plugin.dataFolder,
            factory = ::PluginTranslation
        )

        override val kyoriComponentSerializer = DefaultMutableKrate<KyoriComponentSerializer>(
            loader = { null },
            factory = { KyoriComponentSerializer.Legacy }
        )

        override val inventoryClickEventListener = DefaultInventoryClickEvent()

        override val jsonStringFormat: StringFormat = Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = true
        }

        override val lifecycle: Lifecycle = Lifecycle.Lambda(
            onEnable = {
                inventoryClickEventListener.onEnable(plugin)
                eventListener.onEnable(plugin)
            },
            onReload = {
                translation.loadAndGet()
            },
            onDisable = {
                inventoryClickEventListener.onDisable()
                eventListener.onDisable()
                scope.cancel()
            }
        )
    }
}
