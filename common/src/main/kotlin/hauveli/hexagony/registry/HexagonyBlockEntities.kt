package hauveli.hexagony.registry

import at.petrak.hexcasting.common.blocks.entity.BlockEntityConjured
import hauveli.hexagony.Hexagony
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.BlockPos
import hauveli.hexagony.xplat.IXplatAbstractions
import net.minecraft.resources.ResourceKey
import java.util.function.BiFunction

object HexagonyBlockEntities : HexagonyRegistrar<BlockEntityType<*>>(
    BuiltInRegistries.BLOCK_ENTITY_TYPE.key() as ResourceKey<Registry<BlockEntityType<*>>>,
    { BuiltInRegistries.BLOCK_ENTITY_TYPE }
) {

    // testing
    val MIND_ANCHOR = make(
        "mind_anchor/full",
        ::BlockEntityConjured ,
        HexagonyBlocks.MIND_ANCHOR_FULL.value //, HexagonyBlocks.MIND_ANCHOR_EMPTY.value
    )

    fun <T : BlockEntity> make(
        name: String,
        factory: BiFunction<BlockPos, BlockState, T>,
        vararg blocks: Block
    ): HexagonyRegistrar<BlockEntityType<*>>.Entry<BlockEntityType<T>> {
        return register(name) {
            IXplatAbstractions.INSTANCE!!.createBlockEntityType(factory, *blocks)
        }
    }
}