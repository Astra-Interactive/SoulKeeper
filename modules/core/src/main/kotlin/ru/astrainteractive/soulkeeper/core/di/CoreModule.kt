package ru.astrainteractive.soulkeeper.core.di

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.cancel
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import ru.astrainteractive.astralibs.async.withTimings
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.util.YamlStringFormat
import ru.astrainteractive.astralibs.util.parseOrWriteIntoDefault
import ru.astrainteractive.klibs.kstorage.api.impl.DefaultMutableKrate
import ru.astrainteractive.klibs.kstorage.util.asCachedKrate
import ru.astrainteractive.klibs.mikro.core.coroutines.CoroutineFeature
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import java.io.File

class CoreModule(
    val dispatchers: KotlinDispatchers,
    val dataFolder: File
) : Logger by JUtiltLogger("CoreModule").withoutParentHandlers() {

    val ioScope = CoroutineFeature.IO.withTimings()
    val mainScope = CoroutineFeature
        .Default(dispatchers.Main)
        .withTimings()

    val yamlFormat: StringFormat = YamlStringFormat(
        configuration = Yaml.default.configuration.copy(
            encodeDefaults = true,
            strictMode = false,
            polymorphismStyle = PolymorphismStyle.Property
        ),
    )

    val translation = DefaultMutableKrate(
        factory = ::PluginTranslation,
        loader = {
            yamlFormat.parseOrWriteIntoDefault(
                file = dataFolder.resolve("translations.yml"),
                default = ::PluginTranslation
            )
        }
    ).asCachedKrate()

    val soulsConfigKrate = DefaultMutableKrate(
        factory = ::SoulsConfig,
        loader = {
            yamlFormat.parseOrWriteIntoDefault(
                file = dataFolder.resolve("souls_config.yml"),
                default = ::SoulsConfig
            )
        }
    ).asCachedKrate()

    val kyoriComponentSerializer = DefaultMutableKrate<KyoriComponentSerializer>(
        loader = { null },
        factory = { KyoriComponentSerializer.Legacy }
    ).asCachedKrate()

    val jsonStringFormat: StringFormat = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    val lifecycle: Lifecycle = Lifecycle.Lambda(
        onEnable = {},
        onReload = {
            soulsConfigKrate.getValue()
            translation.getValue()
        },
        onDisable = {
            ioScope.cancel()
        }
    )
}
