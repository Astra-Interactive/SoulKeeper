package ru.astrainteractive.soulkeeper.core.di

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import ru.astrainteractive.astralibs.coroutines.withTimings
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.util.YamlStringFormat
import ru.astrainteractive.astralibs.util.parseOrWriteIntoDefault
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.impl.DefaultMutableKrate
import ru.astrainteractive.klibs.kstorage.util.asCachedKrate
import ru.astrainteractive.klibs.mikro.core.coroutines.CoroutineFeature
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.di.qualifier.CoreLifecycle
import ru.astrainteractive.soulkeeper.core.di.qualifier.DataFolder
import ru.astrainteractive.soulkeeper.core.di.qualifier.IoScope
import ru.astrainteractive.soulkeeper.core.di.qualifier.JsonFormat
import ru.astrainteractive.soulkeeper.core.di.qualifier.MainScope
import ru.astrainteractive.soulkeeper.core.di.qualifier.UnconfinedScope
import ru.astrainteractive.soulkeeper.core.di.qualifier.YamlFormat
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import java.io.File

object CoreScope

@DependencyGraph(CoreScope::class)
interface CoreModule {

    val dispatchers: KotlinDispatchers

    @get:DataFolder
    val dataFolder: File

    @get:IoScope
    val ioScope: CoroutineScope

    @get:UnconfinedScope
    val unconfinedScope: CoroutineScope

    @get:MainScope
    val mainScope: CoroutineScope

    @get:YamlFormat
    val yamlFormat: StringFormat

    @get:JsonFormat
    val jsonStringFormat: StringFormat

    val translation: CachedKrate<PluginTranslation>

    val soulsConfigKrate: CachedKrate<SoulsConfig>

    val kyoriComponentSerializer: CachedKrate<KyoriComponentSerializer>

    @get:CoreLifecycle
    val lifecycle: Lifecycle

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides dispatchers: KotlinDispatchers,
            @Provides @DataFolder dataFolder: File
        ): CoreModule
    }

    @SingleIn(CoreScope::class)
    @IoScope
    @Provides
    fun provideIoScope(): CoroutineScope = CoroutineFeature.IO.withTimings()

    @SingleIn(CoreScope::class)
    @UnconfinedScope
    @Provides
    fun provideUnconfinedScope(): CoroutineScope = CoroutineFeature.Unconfined.withTimings()

    @SingleIn(CoreScope::class)
    @MainScope
    @Provides
    fun provideMainScope(dispatchers: KotlinDispatchers): CoroutineScope =
        CoroutineFeature.Default(dispatchers.Main).withTimings()

    @SingleIn(CoreScope::class)
    @YamlFormat
    @Provides
    fun provideYamlFormat(): StringFormat = YamlStringFormat(
        configuration = Yaml.default.configuration.copy(
            encodeDefaults = true,
            strictMode = false,
            polymorphismStyle = PolymorphismStyle.Property
        ),
    )

    @SingleIn(CoreScope::class)
    @JsonFormat
    @Provides
    fun provideJsonFormat(): StringFormat = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @SingleIn(CoreScope::class)
    @Provides
    fun provideTranslation(
        @YamlFormat yamlFormat: StringFormat,
        @DataFolder dataFolder: File
    ): CachedKrate<PluginTranslation> = DefaultMutableKrate(
        factory = ::PluginTranslation,
        loader = {
            yamlFormat.parseOrWriteIntoDefault(
                file = dataFolder.resolve("translations.yml"),
                default = ::PluginTranslation
            )
        }
    ).asCachedKrate()

    @SingleIn(CoreScope::class)
    @Provides
    fun provideSoulsConfig(
        @YamlFormat yamlFormat: StringFormat,
        @DataFolder dataFolder: File
    ): CachedKrate<SoulsConfig> = DefaultMutableKrate(
        factory = ::SoulsConfig,
        loader = {
            yamlFormat.parseOrWriteIntoDefault(
                file = dataFolder.resolve("souls_config.yml"),
                default = ::SoulsConfig
            )
        }
    ).asCachedKrate()

    @SingleIn(CoreScope::class)
    @Provides
    fun provideKyoriComponentSerializer(): CachedKrate<KyoriComponentSerializer> =
        DefaultMutableKrate<KyoriComponentSerializer>(
            loader = { null },
            factory = { KyoriComponentSerializer.Legacy }
        ).asCachedKrate()

    @SingleIn(CoreScope::class)
    @CoreLifecycle
    @Provides
    fun provideLifecycle(
        soulsConfigKrate: CachedKrate<SoulsConfig>,
        translation: CachedKrate<PluginTranslation>,
        @IoScope ioScope: CoroutineScope,
        @MainScope mainScope: CoroutineScope
    ): Lifecycle = Lifecycle.Lambda(
        onEnable = {},
        onReload = {
            soulsConfigKrate.getValue()
            translation.getValue()
        },
        onDisable = {
            ioScope.cancel()
            mainScope.cancel()
        }
    )
}
