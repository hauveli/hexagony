package hauveli.hexagony.common.blocks

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.casting.circles.ICircleComponent
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.DirectionProperty
import java.util.*


// Facing dir is the direction it starts searching for slates in to start
abstract class BlockAbstractMindAnchor(properties: Properties) : BlockCircleComponent(properties), EntityBlock {
    init {
        this.registerDefaultState(
            this.stateDefinition.any().setValue(ENERGIZED, false).setValue(
                FACING, Direction.NORTH
            )
        )
    }

    override fun acceptControlFlow(
        imageIn: CastingImage?, env: CircleCastEnv?, enterDir: Direction?, pos: BlockPos?,
        bs: BlockState?, world: ServerLevel?
    ): ICircleComponent.ControlFlow {
        return ICircleComponent.ControlFlow.Stop()
    }

    override fun canEnterFromDirection(
        enterDir: Direction?,
        pos: BlockPos?,
        bs: BlockState,
        world: ServerLevel?
    ): Boolean {
        // FACING is the direction media EXITS from, so we can't have media entering in that direction
        // so, flip it
        return enterDir != bs.getValue(FACING).opposite
    }

    override fun possibleExitDirections(pos: BlockPos?, bs: BlockState, world: Level?): EnumSet<Direction?> {
        return EnumSet.of(bs.getValue(FACING))
    }

    override fun normalDir(pos: BlockPos, bs: BlockState, world: Level?, recursionLeft: Int): Direction? {
        return normalDirOfOther(pos.relative(bs.getValue(FACING)), world, recursionLeft)
    }

    override fun particleHeight(pos: BlockPos?, bs: BlockState?, world: Level?): Float {
        return 0.5f
    }

    @Deprecated("Deprecated in Java")
    override fun tick(pState: BlockState, pLevel: ServerLevel, pPos: BlockPos, pRandom: RandomSource) {
        if (pLevel.getBlockEntity(pPos) is BlockEntityAbstractImpetus && pState.getValue(ENERGIZED)) {
            // hmm... where is tile even coming from? I've checked the code and can not find it...
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRemove(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pNewState: BlockState,
        pIsMoving: Boolean
    ) {
        if (!pNewState.`is`(pState.block)
            && pLevel.getBlockEntity(pPos) is BlockEntityAbstractImpetus
        ) {
            // do something else?
            // impetus.endExecution()
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        super.createBlockStateDefinition(builder)
        builder.add(FACING)
    }

    override fun getStateForPlacement(pContext: BlockPlaceContext): BlockState? {
        return placeStateDirAndSneak(this.defaultBlockState(), pContext)
    }

    @Deprecated("Deprecated in Java")
    override fun rotate(pState: BlockState, pRot: Rotation): BlockState {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)))
    }

    @Deprecated("Deprecated in Java")
    override fun mirror(pState: BlockState, pMirror: Mirror): BlockState {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)))
    }

    companion object {
        val FACING: DirectionProperty = BlockStateProperties.FACING
    }
}