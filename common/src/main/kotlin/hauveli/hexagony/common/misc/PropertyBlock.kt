package hauveli.hexagony.common.misc

import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.Property

// Attempt to abstrack block property registration, I'll just use BlockEmptyMindAnchor etc.
class PropertyBlock<T : Comparable<T>>(
    val property: Property<T>,
    val defaultValue: T,
    properties: BlockBehaviour.Properties
) : Block(properties) {

    init {
        require(property != null) { "Property cannot be null!" }
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(property)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(property, defaultValue)
    }

    fun defaultPropertyState(): BlockState = defaultBlockState().setValue(property, defaultValue)
}