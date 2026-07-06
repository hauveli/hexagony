package hauveli.hexagony.features.enlightenment

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.common.impl.HexAPIImpl
import at.petrak.hexcasting.common.items.storage.ItemScroll
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.lib.HexItems.SCROLL_LARGE
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import hauveli.hexagony.Hexagony
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.SimpleCriterionTrigger
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
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

        fun requirementsMet(itemStack: ItemStack): Boolean {
            val scrollItemMaybe = itemStack.item

            val whyDoesThisNotWork: HexItems.SCROLL_LARGE


            if (scrollItemMaybe !is HexItems.SCROLL_LARGE) return false
            val iota: Iota? = scrollItemMaybe.readIota(itemStack)
            return if (iota is PatternIota) {
                Hexagony.LOGGER.info("_______")
                Hexagony.LOGGER.info(pattern)
                Hexagony.LOGGER.info("_______")
                iota.pattern == HexActions.REGISTRY.get(pattern) // erm I'm not super happy with this, will the compiler even know it can cache this value?
            } else {
                false
            }
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
            conditions.requirementsMet(itemStack)
        }
    }
}