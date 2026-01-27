package ru.astrainteractive.soulkeeper.module.event.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.StringFormat
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
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
import ru.astrainteractive.soulkeeper.module.souls.krate.PlayerSoulKrate
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.ItemStackSerializer
import java.io.File
import java.time.Instant
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.orEmpty
import kotlin.time.Duration.Companion.seconds

@Suppress("LongParameterList")
internal class ForgeSoulEvents(
    private val soulsDao: SoulsDao,
    private val effectEmitter: EffectEmitter,
    private val dispatchers: KotlinDispatchers,
    private val dataFolder: File,
    private val stringFormat: StringFormat,
    private val ioScope: CoroutineScope,
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

    private suspend fun updateSoulUnsafe(
        soul: ItemDatabaseSoul,
        droppedXp: Int?,
        soulItems: List<StringFormatObject>?
    ) {
        val updatedItems = soulItems
            .orEmpty()
            .plus(soul.items)
        val updatedSoul = soul.copy(
            exp = droppedXp ?: soul.exp,
            items = updatedItems,
            hasItems = updatedItems.isNotEmpty()
        )
        soulsDao.updateSoul(soul = updatedSoul)
        ioScope.launch {
            val defaultSoul = DefaultSoul(
                ownerUUID = updatedSoul.ownerUUID,
                ownerLastName = updatedSoul.ownerLastName,
                createdAt = updatedSoul.createdAt,
                isFree = updatedSoul.isFree,
                location = updatedSoul.location,
                exp = updatedSoul.exp,
                items = updatedSoul.items
            )
            PlayerSoulKrate(
                stringFormat = stringFormat,
                dataFolder = dataFolder,
                createdAt = soul.createdAt,
                ownerUUID = soul.ownerUUID
            ).save(defaultSoul)
        }
    }

    private suspend fun createSoulUnsafe(
        serverPlayer: OnlineMinecraftPlayer,
        droppedXp: Int?,
        soulItems: List<StringFormatObject>?,
        location: Location,
        dimension: ResourceKey<Level>
    ) {
        val soul = DefaultSoul(
            exp = droppedXp ?: 0,
            ownerUUID = serverPlayer.uuid,
            ownerLastName = serverPlayer.name,
            createdAt = Instant.now(),
            isFree = soulsConfig.soulFreeAfter == 0.seconds,
            location = when (dimension) {
                Level.END -> {
                    location.copy(y = location.y.coerceAtLeast(soulsConfig.endLocationLimitY))
                }

                else -> location
            },
            items = soulItems.orEmpty(),
        )
        soulsDao.insertSoul(soul)
        ioScope.launch {
            PlayerSoulKrate(
                stringFormat = stringFormat,
                dataFolder = dataFolder,
                createdAt = soul.createdAt,
                ownerUUID = soul.ownerUUID
            ).save(soul)
        }
    }

    @Suppress("LongMethod")
    private suspend fun createOrUpdateSoul(
        serverPlayer: ServerPlayer,
        droppedXp: Int?,
        soulItems: List<StringFormatObject>?
    ) {
        mutex.withLock {
            val location = serverPlayer
                .asLocatable()
                .getLocation()

            val existingSoul = soulsDao.getSoulsNear(location, 1)
                .getOrNull()
                .orEmpty()
                .firstOrNull { soul -> soul.ownerUUID == serverPlayer.uuid }
                ?.let { dbSoul -> soulsDao.toItemDatabaseSoul(dbSoul) }
                ?.getOrNull()

            if (existingSoul != null) {
                updateSoulUnsafe(
                    soul = existingSoul,
                    droppedXp = droppedXp,
                    soulItems = soulItems
                )
            } else {
                createSoulUnsafe(
                    location = location,
                    serverPlayer = serverPlayer.asOnlineMinecraftPlayer(),
                    droppedXp = droppedXp,
                    soulItems = soulItems,
                    dimension = serverPlayer.level().dimension()
                )
            }
            spawnSoulEffects(
                location = location,
                onlineMinecraftPlayer = serverPlayer.asOnlineMinecraftPlayer()
            )
        }
    }

    private fun getAndClearDroppedXp(event: LivingExperienceDropEvent): Int {
        val keepLevel = event.entity.level().gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)
        return when {
            keepLevel -> 0

            else -> {
                event.originalExperience
                    .times(soulsConfig.retainedXp)
                    .toInt()
                    .also { event.droppedExperience = 0 }
            }
        }
    }

    private fun getAndClearItems(event: LivingDropsEvent): List<StringFormatObject> {
        val keepInventory = event.entity.level()
            .gameRules
            .getBoolean(GameRules.RULE_KEEPINVENTORY)
        return when {
            keepInventory -> emptyList()
            else -> {
                event.drops
                    .map(ItemEntity::getItem)
                    .map(ItemStackSerializer::encodeToString)
                    .map(::StringFormatObject)
                    .also { event.drops.clear() }
            }
        }
    }

    val expDropEvent = flowEvent<LivingExperienceDropEvent>(EventPriority.HIGHEST)
        .filter { !it.isCanceled }
        .onEach { event ->
            val serverPlayer = event.entity.tryCast<ServerPlayer>() ?: return@onEach

            val droppedXp = getAndClearDroppedXp(event)
            if (droppedXp <= 0) return@onEach

            createOrUpdateSoul(
                serverPlayer = serverPlayer,
                droppedXp = droppedXp,
                soulItems = null
            )
        }
        .launchIn(mainScope)

    val livingDropsEvent = flowEvent<LivingDropsEvent>(EventPriority.HIGHEST)
        .filter { event -> !event.isCanceled }
        .onEach { event ->
            val serverPlayer = event.entity.tryCast<ServerPlayer>() ?: return@onEach

            val soulItems = getAndClearItems(event)
            if (soulItems.isEmpty()) return@onEach
            createOrUpdateSoul(
                serverPlayer = serverPlayer,
                droppedXp = null,
                soulItems = soulItems
            )
        }
        .launchIn(mainScope)
}
