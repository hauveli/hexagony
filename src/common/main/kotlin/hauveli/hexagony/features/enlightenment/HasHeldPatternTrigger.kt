package hauveli.hexagony.features.enlightenment

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.common.casting.PatternRegistryManifest
import at.petrak.hexcasting.common.impl.HexAPIImpl
import at.petrak.hexcasting.common.items.storage.ItemScroll
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.lib.HexItems.SCROLL_LARGE
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.common.lib.hex.HexActions
import at.petrak.hexcasting.common.loot.AddPerWorldPatternToScrollFunc
import at.petrak.hexcasting.server.ScrungledPatternsSave
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import hauveli.hexagony.Hexagony
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.SimpleCriterionTrigger
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import java.util.*


class HasHeldPatternTrigger
    : SimpleCriterionTrigger<HasHeldPatternTrigger.Conditions>() {

    override fun codec(): Codec<Conditions> {
        return Conditions.CODEC
    }

    data class Conditions(
        val playerPredicate: Optional<ContextAwarePredicate>,
        val pattern: ResourceLocation
    ) : SimpleInstance {
        override fun player(): Optional<ContextAwarePredicate> {
            return this.playerPredicate
        }

        fun requirementsMet(itemStack: ItemStack, serverPlayer: ServerPlayer): Boolean {
            val scrollItemMaybe = itemStack.item
            // This will cause essentially no impact in all instances *except* when a player is holding a SCROLL_LARGE
            if (scrollItemMaybe != SCROLL_LARGE) return false
            if (scrollItemMaybe !is IotaHolderItem) return false
            val iota: Iota? = scrollItemMaybe.readIota(itemStack)
            if (iota !is PatternIota) return false
            // oh my gog this rots
            val resourceKey: ResourceKey<ActionRegistryEntry> =
                HexActions.REGISTRY.getHolder(pattern).get().key()
            val pat = PatternRegistryManifest.getCanonicalStrokesPerWorld(
                resourceKey,
                serverPlayer.serverLevel()
            )
            val matched = iota.pattern == pat
            if (matched) {
                // I could do this from the advancement, but I am lazy, this is more convenient and is still only called
                // when the advancement is granted.
                // todo: add custom sound? maybe unwrapping old scroll papery sound? or book opening in pathcouli sound
                serverPlayer.playNotifySound(HexSounds.READ_LORE_FRAGMENT, SoundSource.PLAYERS, 1f, 1f)
                // serverPlayer.playSound(HexSounds.READ_LORE_FRAGMENT, 1f, 1f) // some type of notification
            }
            return matched // erm I'm not super happy with this, will the compiler even know it can cache this value?

        }

        companion object {
            val CODEC: Codec<Conditions> =
                RecordCodecBuilder.create { instance ->
                    instance.group(
                        ContextAwarePredicate.CODEC
                            .optionalFieldOf("player")
                            .forGetter(Conditions::player),

                        ResourceLocation.CODEC
                            .fieldOf("pattern")
                            .forGetter(Conditions::pattern)
                    ).apply(instance, ::Conditions)
                }
        }
    }

    fun trigger(player: ServerPlayer, itemStack: ItemStack) {
        this.trigger(player) { conditions ->
            conditions.requirementsMet(itemStack, player)
        }
    }
}