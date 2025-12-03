package ru.astrainteractive.soulkeeper.module.event.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.neoforged.bus.api.EventPriority
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent
import ru.astrainteractive.astralibs.event.flowEvent
import ru.astrainteractive.astralibs.server.location.Location
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.astralibs.server.util.asLocatable
import ru.astrainteractive.astralibs.server.util.asOnlineMinecraftPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.klibs.mikro.core.util.tryCast
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.ItemStackSerializer
import java.time.Instant
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.orEmpty
import kotlin.time.Duration.Companion.seconds

internal class ForgeSoulEvents(
    private val soulsDao: SoulsDao,
    private val effectEmitter: EffectEmitter,
    private val dispatchers: KotlinDispatchers,
    mainScope: CoroutineScope,
    soulsConfigKrate: CachedKrate<SoulsConfig>
) : Logger by JUtiltLogger("SoulKeeper-ForgeSoulEvents") {
    private val soulsConfig by soulsConfigKrate
    private val mutex = Mutex()
    private suspend fun spawnSoulEffects(
        location: Location,
        onlineMinecraftPlayer: OnlineMinecraftPlayer
    ) {
        withContext(dispatchers.Main) {
            effectEmitter.spawnParticleForPlayer(
                location = location,
                player = onlineMinecraftPlayer,
                config = soulsConfig.particles.soulCreated,
            )
            effectEmitter.playSoundForPlayer(
                location = location,
                player = onlineMinecraftPlayer,
                sound = soulsConfig.sounds.soulDropped,
            )
        }
    }

    private suspend fun updateSoul(
        soul: ItemDatabaseSoul,
        droppedXp: Int?,
        soulItems: List<ItemStack>?
    ) {
        val updatedItems = soulItems
            ?.map(ItemStackSerializer::encodeToString)
            ?.map(::StringFormatObject)
            .orEmpty()
            .plus(soul.items)
        soulsDao.updateSoul(
            soul = soul.copy(
                exp = droppedXp ?: soul.exp,
                items = updatedItems,
                hasItems = updatedItems.isNotEmpty()
            )
        )
    }

    private suspend fun createSoul(
        onlineMinecraftPlayer: OnlineMinecraftPlayer,
        droppedXp: Int?,
        soulItems: List<ItemStack>?,
        location: Location,
        dimension: ResourceKey<Level>
    ) {
        val soul = DefaultSoul(
            exp = droppedXp ?: 0,
            ownerUUID = onlineMinecraftPlayer.uuid,
            ownerLastName = onlineMinecraftPlayer.name,
            createdAt = Instant.now(),
            isFree = soulsConfig.soulFreeAfter == 0.seconds,
            location = when (dimension) {
                Level.END -> {
                    location.copy(y = location.y.coerceAtLeast(soulsConfig.endLocationLimitY))
                }

                else -> location
            },
            items = soulItems
                .orEmpty()
                .map(ItemStackSerializer::encodeToString)
                .map(::StringFormatObject),
        )
        soulsDao.insertSoul(soul)
    }

    @Suppress("LongMethod")
    private suspend fun createOrUpdateSoul(
        onlineMinecraftPlayer: OnlineMinecraftPlayer,
        serverPlayer: ServerPlayer,
        droppedXp: Int?,
        soulItems: List<ItemStack>?
    ) {
        mutex.withLock {
            val location = onlineMinecraftPlayer
                .asLocatable()
                .getLocation()

            val existingSoul = soulsDao.getSoulsNear(location, 1)
                .getOrNull()
                .orEmpty()
                .firstOrNull { soul -> soul.ownerUUID == onlineMinecraftPlayer.uuid }
                ?.let { dbSoul -> soulsDao.toItemDatabaseSoul(dbSoul) }
                ?.getOrNull()

            if (existingSoul != null) {
                updateSoul(
                    soul = existingSoul,
                    droppedXp = droppedXp,
                    soulItems = soulItems
                )
            } else {
                createSoul(
                    location = location,
                    onlineMinecraftPlayer = onlineMinecraftPlayer,
                    droppedXp = droppedXp,
                    soulItems = soulItems,
                    dimension = serverPlayer.level().dimension()
                )
            }
            spawnSoulEffects(
                location = location,
                onlineMinecraftPlayer = onlineMinecraftPlayer
            )
        }
    }

    val expDropEvent = flowEvent<LivingExperienceDropEvent>(EventPriority.HIGHEST)
        .filter { !it.isCanceled }
        .onEach { event ->
            val serverPlayer = event.entity.tryCast<ServerPlayer>() ?: return@onEach
            val keepLevel = event.entity.level().gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)
            val onlineMinecraftPlayer = serverPlayer.asOnlineMinecraftPlayer()

            val droppedXp = when {
                keepLevel -> 0
                else -> {
                    event.droppedExperience
                        .times(soulsConfig.retainedXp)
                        .toInt()
                }
            }

            if (droppedXp <= 0) return@onEach
            event.droppedExperience = 0

            createOrUpdateSoul(
                onlineMinecraftPlayer = onlineMinecraftPlayer,
                serverPlayer = serverPlayer,
                droppedXp = droppedXp,
                soulItems = null
            )
        }
        .launchIn(mainScope)

    val livingDropsEvent = flowEvent<LivingDropsEvent>(EventPriority.HIGHEST)
        .filter { event -> !event.isCanceled }
        .onEach { event ->
            info { "#livingDropsEvent ${event.drops.size} ${event.drops}" }
            val serverPlayer = event.entity.tryCast<ServerPlayer>() ?: return@onEach
            val keepInventory = event.entity.level().gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)
            val onlineMinecraftPlayer = serverPlayer.asOnlineMinecraftPlayer()

            val soulItems = when {
                keepInventory -> emptyList()
                else -> event.drops.map(ItemEntity::getItem)
            }

            if (soulItems.isEmpty()) return@onEach
            event.drops.clear()

            createOrUpdateSoul(
                onlineMinecraftPlayer = onlineMinecraftPlayer,
                serverPlayer = serverPlayer,
                droppedXp = null,
                soulItems = soulItems
            )
        }
        .launchIn(mainScope)
}
