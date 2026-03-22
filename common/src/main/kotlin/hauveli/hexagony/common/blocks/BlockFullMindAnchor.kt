package hauveli.hexagony.common.blocks

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.BlockHitResult

class BlockFullMindAnchor(p_49795_: Properties) : BlockAbstractImpetus(p_49795_) {
    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity? {
        return BlockEntityFullMindAnchor(pPos, pState)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        super.createBlockStateDefinition(builder)
        builder.add(POWERED)
    }

    override fun use(
        pState: BlockState, pLevel: Level, pPos: BlockPos, pPlayer: Player, pHand: InteractionHand,
        pHit: BlockHitResult
    ): InteractionResult {
        if (pLevel is ServerLevel
            && pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor
        ) {
            val usedStack = pPlayer.getItemInHand(pHand)
            val tile = pLevel.getBlockEntity(pPos) as BlockEntityFullMindAnchor
            if (usedStack.isEmpty() && pPlayer.isDiscrete()) {
                tile.clearPlayer()
                tile.sync()
                pLevel.playSound(null, pPos, HexSounds.IMPETUS_REDSTONE_CLEAR, SoundSource.BLOCKS, 1f, 1f)
                return InteractionResult.sidedSuccess(pLevel.isClientSide)
            } else {
                val datumContainer = IXplatAbstractions.INSTANCE.findDataHolder(usedStack)
                if (datumContainer != null) {
                    val stored = datumContainer.readIota(pLevel)
                    if (stored is EntityIota) {
                        val entity = stored.getEntity()
                        if (entity is Player) {
                            // phew, we got something
                            tile.setPlayer(entity.getGameProfile(), entity.getUUID())
                            tile.sync()

                            pLevel.playSound(
                                null, pPos, HexSounds.IMPETUS_REDSTONE_DING,
                                SoundSource.BLOCKS, 1f, 1f
                            )
                            return InteractionResult.sidedSuccess(pLevel.isClientSide)
                        }
                    }
                }
            }
        }

        return InteractionResult.PASS
    }

    override fun tick(pState: BlockState, pLevel: ServerLevel, pPos: BlockPos, pRandom: RandomSource) {
        super.tick(pState, pLevel, pPos, pRandom)
        if (pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor) {
            val tile = pLevel.getBlockEntity(pPos) as BlockEntityFullMindAnchor
            tile.updatePlayerProfile()
        }
    }

    override fun neighborChanged(
        pState: BlockState, pLevel: Level, pPos: BlockPos, pBlock: Block, pFromPos: BlockPos,
        pIsMoving: Boolean
    ) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving)

        if (pLevel is ServerLevel) {
            val prevPowered = pState.getValue<Boolean?>(POWERED)
            val isPowered = pLevel.hasNeighborSignal(pPos)

            if (prevPowered != isPowered) {
                pLevel.setBlockAndUpdate(pPos, pState.setValue<Boolean?, Boolean?>(POWERED, isPowered))

                if (isPowered && pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor) {
                    val tile = pLevel.getBlockEntity(pPos) as BlockEntityFullMindAnchor
                    val player: ServerPlayer? = tile.getStoredPlayer()
                    tile.startExecution(player)
                }
            }
        }
    }

    companion object {
        val POWERED: BooleanProperty = BlockStateProperties.POWERED
    }
}