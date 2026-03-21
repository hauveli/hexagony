package hauveli.hexagony.common.blocks

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent
import at.petrak.hexcasting.api.casting.circles.ICircleComponent
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import com.mojang.datafixers.util.Pair
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.DirectionProperty
import java.util.*
import java.util.List

// As it turns out, not actually an impetus
class BlockEmptyMindAnchor (p_49795_: Properties) : BlockCircleComponent(p_49795_) {
    init {
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue<Boolean?, Boolean?>(ENERGIZED, false)
                .setValue<Direction?, Direction?>(FACING, Direction.NORTH)
        )
    }

    override fun acceptControlFlow(
        imageIn: CastingImage?, env: CircleCastEnv?, enterDir: Direction?, pos: BlockPos?,
        bs: BlockState, world: ServerLevel?
    ): ICircleComponent.ControlFlow {
        return ICircleComponent.ControlFlow.Continue(
            imageIn, List.of<Pair<BlockPos?, Direction?>?>(
                this.exitPositionFromDirection(
                    pos, bs.getValue<Direction?>(
                        FACING
                    )
                )
            )
        )
    }

    override fun canEnterFromDirection(
        enterDir: Direction?,
        pos: BlockPos?,
        bs: BlockState,
        world: ServerLevel?
    ): Boolean {
        return enterDir != bs.getValue<Direction?>(FACING).getOpposite()
    }

    override fun possibleExitDirections(pos: BlockPos?, bs: BlockState, world: Level?): EnumSet<Direction?> {
        return EnumSet.of<Direction?>(bs.getValue<Direction?>(FACING))
    }

    override fun normalDir(pos: BlockPos, bs: BlockState, world: Level?, recursionLeft: Int): Direction? {
        return normalDirOfOther(pos.relative(bs.getValue<Direction?>(FACING)), world, recursionLeft)
    }

    override fun particleHeight(pos: BlockPos?, bs: BlockState?, world: Level?): Float {
        return 0.5f
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        super.createBlockStateDefinition(builder)
        builder.add(FACING)
    }

    override fun getStateForPlacement(pContext: BlockPlaceContext): BlockState? {
        return placeStateDirAndSneak(this.defaultBlockState(), pContext)
    }

    override fun rotate(pState: BlockState, pRot: Rotation): BlockState {
        return pState.setValue<Direction?, Direction?>(FACING, pRot.rotate(pState.getValue<Direction?>(FACING)))
    }

    override fun mirror(pState: BlockState, pMirror: Mirror): BlockState {
        return pState.rotate(pMirror.getRotation(pState.getValue<Direction?>(FACING)))
    }

    companion object {
        val FACING: DirectionProperty = BlockStateProperties.FACING
    }
}