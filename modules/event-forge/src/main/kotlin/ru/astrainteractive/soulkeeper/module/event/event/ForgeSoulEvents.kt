package ru.astrainteractive.soulkeeper.module.event.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.eventbus.api.EventPriority
import ru.astrainteractive.astralibs.async.withTimings
import ru.astrainteractive.astralibs.event.flowEvent
import ru.astrainteractive.astralibs.server.util.asLocatable
import ru.astrainteractive.astralibs.server.util.asOnlineMinecraftPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.coroutines.CoroutineFeature
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
    mainScope: CoroutineScope,
    ioScope: CoroutineScope,
    soulsConfigKrate: CachedKrate<SoulsConfig>
) {
    private val soulsConfig by soulsConfigKrate

    val playerDeathEvent = flowEvent<LivingDropsEvent>(EventPriority.HIGH)
        .filter { event -> !event.isCanceled }
        .onEach { event ->
            val serverPlayer = event.entity.tryCast<ServerPlayer>() ?: return@onEach
            val onlineMinecraftPlayer = serverPlayer.asOnlineMinecraftPlayer()
            val keepInventory = event.entity.level().gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)
            val keepLevel = keepInventory // event.entity.level().gameRules.getBoolean(GameRules.RULE_KEEPLEVEL)

            val soulItems = when {
                keepInventory -> emptyList()

                else -> event.drops.map(ItemEntity::getItem)
            }

            val droppedXp = when {
                keepLevel -> 0

                else ->
                    event.drops
                        .filterIsInstance<ExperienceOrb>()
                        .sumOf { it.value }
                        .times(soulsConfig.retainedXp)
                        .toInt()
            }
            event.drops.clear()
            if (soulItems.isEmpty() && droppedXp <= 0) return@onEach

            val bukkitSoul = DefaultSoul(
                exp = droppedXp,
                ownerUUID = onlineMinecraftPlayer.uuid,
                ownerLastName = onlineMinecraftPlayer.name,
                createdAt = Instant.now(),
                isFree = soulsConfig.soulFreeAfter == 0.seconds,
                location = when (serverPlayer.level().dimension()) {
                    Level.END -> {
                        val location = onlineMinecraftPlayer.asLocatable().getLocation()
                        location.copy(y = location.y.coerceAtLeast(soulsConfig.endLocationLimitY))
                    }

                    else -> onlineMinecraftPlayer.asLocatable().getLocation()
                }.toDatabaseLocation(),
                hasItems = soulItems.isNotEmpty(),
                items = soulItems
                    .map(ItemStackSerializer::encodeToString)
                    .map(::StringFormatObject),
            )
            effectEmitter.spawnParticleForPlayer(
                location = bukkitSoul.location,
                player = onlineMinecraftPlayer,
                config = soulsConfig.particles.soulCreated,
            )
            effectEmitter.playSoundForPlayer(
                location = bukkitSoul.location,
                player = onlineMinecraftPlayer,
                sound = soulsConfig.sounds.soulDropped,
            )
            ioScope.launch {
                soulsDao.insertSoul(bukkitSoul)
            }
        }.launchIn(mainScope)
}
