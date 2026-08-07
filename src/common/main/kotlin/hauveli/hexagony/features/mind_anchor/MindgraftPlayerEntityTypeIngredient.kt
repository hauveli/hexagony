package hauveli.hexagony.features.mind_anchor

import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredientType
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.EntityTypeIngredient
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import hauveli.hexagony.Hexagony
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import java.util.function.Function


// todo: decide if it's worth going down this route, I think the answer is probably no...
class MindgraftPlayerEntityTypeIngredient : EntityTypeIngredient(EntityType.PLAYER) {

    override fun test(entity: Entity, level: ServerLevel?): Boolean {
        Hexagony.LOGGER.info("WOAOWOOWOWOWOWOWHH HOOLLY FUCK ")
        return entity.type === this.entityType
                && entity is ServerPlayer
                && entity.health == 0f
                && entity.inventory.items
            .asSequence()
            .filterNot(ItemStack::isEmpty)
            .any { stack ->
                val mediaHolderMaybe = IXplatAbstractions.INSTANCE.findMediaHolder(stack)
                if (mediaHolderMaybe != null) {
                    return@any mediaHolderMaybe.media == 0L
                } else {
                    return true
                }
            }
    }

    override fun equals(o: Any?): Boolean {
        Hexagony.LOGGER.info("WOAOWOOWOWOWOWOWHH HOOLLY FUCK 2")
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        Hexagony.LOGGER.info("WOAOWOOWOWOWOWOWHH HOOLLY FUCK 3")
        val that = o as MindgraftPlayerEntityTypeIngredient
        return entityType == that.entityType
    }

    class Type : BrainsweepeeIngredientType<MindgraftPlayerEntityTypeIngredient?> {
        override fun codec(): MapCodec<MindgraftPlayerEntityTypeIngredient?>? {
            return CODEC
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf?, MindgraftPlayerEntityTypeIngredient?> {
            return STREAM_CODEC
        }

        companion object {
            val CODEC: MapCodec<MindgraftPlayerEntityTypeIngredient?>? =
                RecordCodecBuilder.mapCodec<MindgraftPlayerEntityTypeIngredient?>(
                    Function { instance: RecordCodecBuilder.Instance<MindgraftPlayerEntityTypeIngredient?>? ->
                        instance!!.group(
                            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entityType")
                                .forGetter<MindgraftPlayerEntityTypeIngredient?>(
                                    { obj: MindgraftPlayerEntityTypeIngredient? -> obj!!.getEntityType() })
                        ).apply<MindgraftPlayerEntityTypeIngredient?>(
                            instance,
                            { MindgraftPlayerEntityTypeIngredient() })
                    })
            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf?, MindgraftPlayerEntityTypeIngredient?> =
                StreamCodec.composite<RegistryFriendlyByteBuf?, MindgraftPlayerEntityTypeIngredient?, EntityType<*>?>(
                    ByteBufCodecs.registry(Registries.ENTITY_TYPE),
                    { obj: MindgraftPlayerEntityTypeIngredient? -> obj!!.getEntityType() },
                    { MindgraftPlayerEntityTypeIngredient() }
                )
        }
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}