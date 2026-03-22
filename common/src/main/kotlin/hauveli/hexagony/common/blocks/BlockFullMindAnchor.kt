package hauveli.hexagony.common.blocks

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockRedstoneImpetus
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.xplat.IXplatAbstractions
//import hauveli.hexagony.common.lib.BlockProperties
//import hauveli.hexagony.common.lib.BlockProperties.Companion.FILLED
import hauveli.hexagony.registry.HexagonyBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.BlockHitResult

// BlockAbstractImpetus happens to have a lot of useful features I want so that's convenient
class BlockFullMindAnchor(properties: BlockBehaviour.Properties) :
    BlockEntityFullMindAnchor() {

    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity? {
        return BlockEntityFullMindAnchor(pPos, pState)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        // super.createBlockStateDefinition(builder)
        builder.add(POWERED)
    }

    override fun use(
        pState: BlockState, pLevel: Level, pPos: BlockPos, pPlayer: Player, pHand: InteractionHand,
        pHit: BlockHitResult
    ): InteractionResult {
        if (pLevel is ServerLevel
            && pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor
        ) {
            val tile = pLevel.getBlockEntity(pPos) as BlockEntityFullMindAnchor
            when (tile.getStoredPlayer()) {
                is ServerPlayer -> {}
                else -> {}
            }
        }
        return InteractionResult.PASS
    }

    override fun playerWillDestroy(pLevel: Level, pPos: BlockPos, state: BlockState, player: Player) {
        //super.playerWillDestroy(pLevel, pPos, state, player)
        if (pLevel is ServerLevel
            && pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor
        ) {
            val tile = pLevel.getBlockEntity(pPos) as BlockEntityFullMindAnchor
            when (tile.getStoredPlayer()) {
                is ServerPlayer -> {

                }
                else -> {
                    pLevel.addWithUUID(tile.getStoredPlayer())
                } // Player not connected, don't break it? I would like it if I could anyway...
            }
        }
    }

    override fun attack(pState: BlockState, pLevel: Level, pPos: BlockPos, pPlayer: Player) {
        if (pLevel is ServerLevel
            && pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor
        ) {

        }
    }

    override fun tick(pState: BlockState, pLevel: ServerLevel, pPos: BlockPos, pRandom: RandomSource) {
        super.tick(pState, pLevel, pPos, pRandom)
        // I don't undertsand the purpose of the code below
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

        // TODO: make powering the block affect the bound player
        if (pLevel is ServerLevel) {
            val prevPowered = pState.getValue(POWERED)
            val isPowered = pLevel.hasNeighborSignal(pPos)

            if (prevPowered != isPowered) {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(POWERED, isPowered))

                if (isPowered && pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor) {
                    val tile = pLevel.getBlockEntity(pPos) as BlockEntityFullMindAnchor
                    val player: ServerPlayer? = tile.getStoredPlayer()
                    // todo: in here!
                    // Also todo: it might be fun to leave this behaviour here...
                    tile.startExecution(player)
                }
            }
        }
    }


    companion object {
        val POWERED: BooleanProperty  = BooleanProperty.create("powered")
    }

    fun summonItem(level: ServerLevel, pos: BlockPos, stack: ItemStack ) {
        val itemEntity = ItemEntity(
            level,
            pos.x + 0.5,   // Center in the block
            pos.y + 0.5,
            pos.z + 0.5,
            stack
        )

        // Create custom NBT
        val nbt = CompoundTag()
        nbt.putString("UUID", this.UUID)
        nbt.putInt("PowerLevel", 9001)
        // Optional: prevent despawn
        itemEntity.setNoPickUpDelay() // ticks before it can be picked up
        level.spawn (itemEntity)
    }
}