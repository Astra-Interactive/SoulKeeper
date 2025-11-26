package ru.astrainteractive.soulkeeper.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLPaths
import ru.astrainteractive.astralibs.async.CoroutineTimings
import ru.astrainteractive.astralibs.coroutine.ForgeMainDispatcher
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.util.tryCast
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.event.di.ForgeEventModule
import ru.astrainteractive.soulkeeper.module.souls.di.ForgePlatformServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.ServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import java.io.File
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class RootModule {

    private val dataFolder by lazy {
        FMLPaths.CONFIGDIR.get()
            .resolve("SoulKeeper")
            .toAbsolutePath()
            .toFile()
            .also(File::mkdirs)
    }

    class ForgeMainDispatcher : CoroutineDispatcher() {

        override fun isDispatchNeeded(context: CoroutineContext): Boolean {
            return true
        }

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            DistExecutor.safeRunWhenOn(FMLEnvironment.dist) {
                val key = context.tryCast<AbstractCoroutineContextElement>()?.key
                val timingsKey = key?.tryCast<CoroutineTimings.Key>()
                val timedRunnable = timingsKey?.let(context::get)

                if (timedRunnable == null) {
                    DistExecutor.SafeRunnable { block.run() }
                } else {
                    timedRunnable.queue.add(block)
                    DistExecutor.SafeRunnable { timedRunnable.run() }
                }
            }
        }
    }

    val coreModule: CoreModule by lazy {
        CoreModule(
            dispatchers = object : KotlinDispatchers {
                override val Main: CoroutineDispatcher = ForgeMainDispatcher()
                override val IO: CoroutineDispatcher = Dispatchers.IO
                override val Default: CoroutineDispatcher = Dispatchers.Default
                override val Unconfined: CoroutineDispatcher = Dispatchers.Unconfined
            },
            dataFolder = dataFolder
        )
    }

    private val soulsDaoModule by lazy {
        SoulsDaoModule.Default(
            dataFolder = coreModule.dataFolder,
            ioScope = coreModule.ioScope
        )
    }
    private val forgePlatformServiceModule by lazy {
        ForgePlatformServiceModule(
            coreModule = coreModule,
            soulsDaoModule = soulsDaoModule
        )
    }

    private val serviceModule by lazy {
        ServiceModule(
            coreModule = coreModule,
            soulsDaoModule = soulsDaoModule,
            platformServiceModule = forgePlatformServiceModule
        )
    }
    private val forgeEventModule by lazy {
        ForgeEventModule(
            coreModule = coreModule,
            soulsDaoModule = soulsDaoModule,
            effectEmitter = forgePlatformServiceModule.effectEmitter
        )
    }

    private val lifecycles: List<Lifecycle>
        get() = listOfNotNull(
            coreModule.lifecycle,
            soulsDaoModule.lifecycle,
            forgeEventModule.lifecycle,
            serviceModule.lifecycle,
        )

    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            lifecycles.forEach(Lifecycle::onEnable)
        },
        onDisable = {
            lifecycles.forEach(Lifecycle::onDisable)
        },
        onReload = {
            lifecycles.forEach(Lifecycle::onReload)
        }
    )
}
