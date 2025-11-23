package ru.astrainteractive.aspekt.di

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.fml.loading.FMLPaths
import ru.astrainteractive.aspekt.module.auth.api.di.AuthApiModule
import ru.astrainteractive.aspekt.module.auth.di.ForgeAuthModule
import ru.astrainteractive.aspekt.module.claims.di.ClaimModule
import ru.astrainteractive.aspekt.module.claims.di.ForgeClaimModule
import ru.astrainteractive.aspekt.module.rtp.di.RtpModule
import ru.astrainteractive.aspekt.module.sethome.di.HomesModule
import ru.astrainteractive.aspekt.module.tpa.di.TpaModule
import ru.astrainteractive.astralibs.command.registrar.ForgeCommandRegistrarContext
import ru.astrainteractive.astralibs.coroutine.ForgeMainDispatcher
import ru.astrainteractive.astralibs.event.flowEvent
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.server.ForgeMinecraftNativeBridge
import ru.astrainteractive.astralibs.server.ForgePlatformServer
import ru.astrainteractive.astralibs.util.YamlStringFormat
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import java.io.File

class RootModule : Logger by JUtiltLogger("AspeKt-RootModuleImpl") {
    private val dataFolder by lazy {
        FMLPaths.CONFIGDIR.get()
            .resolve("AspeKt")
            .toAbsolutePath()
            .toFile()
            .also(File::mkdirs)
    }

    val coreModule by lazy {
        CoreModule(
            dataFolder = dataFolder,
            dispatchers = object : KotlinDispatchers {
                override val Main: CoroutineDispatcher = ForgeMainDispatcher
                override val IO: CoroutineDispatcher = Dispatchers.IO
                override val Default: CoroutineDispatcher = Dispatchers.Default
                override val Unconfined: CoroutineDispatcher = Dispatchers.Unconfined
            },
            minecraftNativeBridge = ForgeMinecraftNativeBridge(),
            platformServer = ForgePlatformServer
        )
    }

    @Suppress("UnusedPrivateProperty")
    private val serverStateFlow = flowEvent<ServerStartedEvent>()
        .map { event -> event.server }
        .stateIn(coreModule.mainScope, SharingStarted.Eagerly, null)

    private val commandRegistrarContext = ForgeCommandRegistrarContext(coreModule.mainScope)

    val authApiModule = AuthApiModule(
        ioScope = coreModule.ioScope,
        dataFolder = dataFolder
            .resolve("auth")
            .also(File::mkdirs),
        stringFormat = YamlStringFormat(
            configuration = Yaml.default.configuration.copy(
                encodeDefaults = true,
                strictMode = false,
                polymorphismStyle = PolymorphismStyle.Property
            )
        )
    )
    val forgeAuthModule by lazy {
        ForgeAuthModule(
            authApiModule = authApiModule,
            coreModule = coreModule,
            commandRegistrarContext = commandRegistrarContext
        )
    }

    val claimModule by lazy {
        ClaimModule(
            stringFormat = coreModule.jsonStringFormat,
            dataFolder = dataFolder,
            ioScope = coreModule.ioScope,
            translationKrate = coreModule.translation
        )
    }

    val forgeClaimModule by lazy {
        ForgeClaimModule(
            commandRegistrarContext = commandRegistrarContext,
            coreModule = coreModule,
            claimModule = claimModule
        )
    }

    val homesModule by lazy {
        HomesModule(
            commandRegistrarContext = commandRegistrarContext,
            dataFolder = dataFolder,
            stringFormat = coreModule.jsonStringFormat,
            coreModule = coreModule
        )
    }

    val tpaModule by lazy {
        TpaModule(
            coreModule = coreModule,
            commandRegistrarContext = commandRegistrarContext,
        )
    }
    val rtpModule by lazy {
        RtpModule(
            coreModule = coreModule,
            commandRegistrarContext = commandRegistrarContext
        )
    }

    private val lifecycles: List<Lifecycle>
        get() = listOf(
            coreModule.lifecycle,
            forgeAuthModule.lifecycle,
            forgeClaimModule.lifecycle,
            homesModule.lifecycle,
            tpaModule.lifecycle,
            rtpModule.lifecycle
        )

    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            lifecycles.forEach(Lifecycle::onEnable)
        },
        onReload = {
            lifecycles.forEach(Lifecycle::onReload)
        },
        onDisable = {
            lifecycles.forEach(Lifecycle::onDisable)
        }
    )
}
