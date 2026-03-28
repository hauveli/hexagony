package hauveli.hexagony.registry

import hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
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
        ::BlockEntityFullMindAnchor,
        { HexagonyBlocks.MIND_ANCHOR_FULL.value } //, HexagonyBlocks.MIND_ANCHOR_EMPTY.value
    )

    fun <T : BlockEntity> make(
        name: String,
        factory: BiFunction<BlockPos, BlockState, T>,
        vararg blocks: () -> Block
    ): HexagonyRegistrar<BlockEntityType<*>>.Entry<BlockEntityType<T>> {
        return register(name) {
            val resolved = blocks.map { it() }.toTypedArray()
            IXplatAbstractions.INSTANCE!!.createBlockEntityType(factory, *resolved)
        }
    }
}