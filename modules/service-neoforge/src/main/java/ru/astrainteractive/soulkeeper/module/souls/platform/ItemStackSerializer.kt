package ru.astrainteractive.soulkeeper.module.souls.platform

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.TagParser
import net.minecraft.world.item.ItemStack
import ru.astrainteractive.astralibs.server.util.NeoForgeUtil

object ItemStackSerializer : KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "AstraLibs.Forge.ItemStack.Json",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: ItemStack) {
        encoder.encodeString(encodeToString(value))
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        return decodeFromString(decoder.decodeString()).getOrThrow()
    }

    private fun requireHolderLookup(): RegistryAccess.Frozen {
        return NeoForgeUtil.serverOrNull
            ?.registryAccess()
            ?: error("Server is not running")
    }

    fun encodeToString(itemStack: ItemStack): String {
        return itemStack.save(requireHolderLookup()).toString()
    }

    fun decodeFromString(string: String): Result<ItemStack> = runCatching {
        val nbt = CompoundTag()
        nbt.merge(parseNbt(string))
        ItemStack.parse(requireHolderLookup(), nbt).get()
    }

    private fun parseNbt(string: String): CompoundTag {
        return try {
            TagParser.parseTag(string)
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not parse ItemStack string: $string", e)
        }
    }
}
