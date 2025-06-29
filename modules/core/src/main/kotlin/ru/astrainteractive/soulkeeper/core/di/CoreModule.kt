package ru.astrainteractive.soulkeeper.core.di

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.astralibs.serialization.YamlStringFormat
import ru.astrainteractive.astralibs.util.fileConfigKrate
import ru.astrainteractive.klibs.kstorage.api.impl.DefaultMutableKrate
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import java.io.File

class CoreModule(
    val dispatchers: KotlinDispatchers,
    val dataFolder: File
) : Logger by JUtiltLogger("CoreModule") {

    val scope = CoroutineFeature.Default(Dispatchers.IO)

    val yamlFormat: StringFormat = YamlStringFormat(
        configuration = Yaml.default.configuration.copy(
            encodeDefaults = true,
            strictMode = false,
            polymorphismStyle = PolymorphismStyle.Property
        ),
    )

    val translation = fileConfigKrate(
        file = dataFolder.resolve("translations.yml"),
        stringFormat = yamlFormat,
        factory = ::PluginTranslation
    )

    val soulsConfigKrate = fileConfigKrate<SoulsConfig>(
        file = dataFolder.resolve("souls_config.yml"),
        stringFormat = yamlFormat,
        factory = ::SoulsConfig
    )

    val kyoriComponentSerializer = DefaultMutableKrate<KyoriComponentSerializer>(
        loader = { null },
        factory = { KyoriComponentSerializer.Legacy }
    )


    val jsonStringFormat: StringFormat = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    val lifecycle: Lifecycle = Lifecycle.Lambda(
        onEnable = {},
        onReload = {
            soulsConfigKrate.loadAndGet()
            translation.loadAndGet()
        },
        onDisable = {
            scope.cancel()
        }
    )
}
