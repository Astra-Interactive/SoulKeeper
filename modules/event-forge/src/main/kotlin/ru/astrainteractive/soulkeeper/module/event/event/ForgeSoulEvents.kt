package ru.astrainteractive.soulkeeper.module.event.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.eventbus.api.EventPriority
import ru.astrainteractive.astralibs.event.flowEvent
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.astralibs.server.util.asLocatable
import ru.astrainteractive.astralibs.server.util.asOnlineMinecraftPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.coroutines.launch
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.klibs.mikro.core.util.tryCast
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.toDatabaseLocation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.ItemStackSerializer
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

internal class ForgeSoulEvents(
    private val soulsDao: SoulsDao,
    private val effectEmitter: EffectEmitter,
    private val ioScope: CoroutineScope,
    mainScope: CoroutineScope,
    soulsConfigKrate: CachedKrate<SoulsConfig>
) : Logger by JUtiltLogger("SoulKeeper-ForgeSoulEvents") {
    private val soulsConfig by soulsConfigKrate
    private val mutex = Mutex()

    @Suppress("LongMethod")
    private fun createOrUpdateSoul(
        onlineMinecraftPlayer: OnlineMinecraftPlayer,
        serverPlayer: ServerPlayer,
        droppedXp: Int?,
        soulItems: List<ItemStack>?
    ) {
        val location = onlineMinecraftPlayer
            .asLocatable()
            .getLocation()
        val dbLocation = location.toDatabaseLocation()
        val dimension = serverPlayer.level().dimension()
        ioScope.launch(mutex) {
            val existingSoul = soulsDao.getSoulsNear(dbLocation, 1)
                .getOrNull()
                .orEmpty()
                .firstOrNull { soul -> soul.ownerUUID == onlineMinecraftPlayer.uuid }
                ?.let { dbSoul -> soulsDao.toItemDatabaseSoul(dbSoul) }
                ?.getOrNull()
            if (existingSoul != null) {
                val updatedItems = soulItems
                    .orEmpty()
                    .map(ItemStackSerializer::encodeToString)
                    .map(::StringFormatObject)
                    .takeIf { items -> items.isNotEmpty() }
                    ?: existingSoul.items
                soulsDao.updateSoul(
                    soul = existingSoul.copy(
                        exp = droppedXp ?: existingSoul.exp,
                        items = updatedItems,
                        hasItems = updatedItems.isNotEmpty()
                    )
                )
            } else {
                val soul = existingSoul ?: DefaultSoul(
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
                    }.toDatabaseLocation(),
                    hasItems = soulItems.orEmpty().isNotEmpty(),
                    items = soulItems
                        .orEmpty()
                        .map(ItemStackSerializer::encodeToString)
                        .map(::StringFormatObject),
                )
                soulsDao.insertSoul(soul)
            }
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

    val expDropEvent = flowEvent<LivingExperienceDropEvent>(EventPriority.HIGH)
        .filter { !it.isCanceled }
        .onEach { event ->
            val serverPlayer = event.entity.tryCast<ServerPlayer>() ?: return@onEach
            val onlineMinecraftPlayer = serverPlayer.asOnlineMinecraftPlayer()
            val keepLevel = event.entity.level().gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)

            val droppedXp = when {
                keepLevel -> 0
                else ->
                    event.droppedExperience
                        .times(soulsConfig.retainedXp)
                        .toInt()
            }

            if (droppedXp <= 0) return@onEach
            event.droppedExperience = 0

            createOrUpdateSoul(
                onlineMinecraftPlayer = onlineMinecraftPlayer,
                serverPlayer = serverPlayer,
                droppedXp = droppedXp,
                soulItems = null
            )
        }.launchIn(mainScope)

    val playerDeathEvent = flowEvent<LivingDropsEvent>(EventPriority.HIGH)
        .filter { event -> !event.isCanceled }
        .onEach { event ->
            val serverPlayer = event.entity.tryCast<ServerPlayer>() ?: return@onEach
            val onlineMinecraftPlayer = serverPlayer.asOnlineMinecraftPlayer()
            val keepInventory = event.entity.level().gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)

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
        }.launchIn(mainScope)
}
