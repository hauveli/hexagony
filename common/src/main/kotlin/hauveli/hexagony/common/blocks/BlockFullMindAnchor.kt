package hauveli.hexagony.common.blocks

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.utils.getOrCreateCompound
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.putUUID
import at.petrak.hexcasting.common.blocks.circles.BlockEmptyImpetus
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockRedstoneImpetus
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.xplat.IXplatAbstractions
import hauveli.hexagony.common.blocks.anchors.MindAnchor.Companion.TAG_STORED_PLAYER
import hauveli.hexagony.common.blocks.anchors.MindAnchor.Companion.TAG_STORED_PLAYER_PROFILE
//import hauveli.hexagony.common.lib.BlockProperties
//import hauveli.hexagony.common.lib.BlockProperties.Companion.FILLED
import hauveli.hexagony.registry.HexagonyBlockEntities
import hauveli.hexagony.registry.HexagonyBlocks
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
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
// Also I get to avoid having to construct quite a few classes which is awesome
class BlockFullMindAnchor(properties: BlockBehaviour.Properties) :
    BlockRedstoneImpetus(properties) {

    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity {
        return BlockEntityFullMindAnchor (pPos, pState)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        super.createBlockStateDefinition(builder)
        // builder.add(POWERED)
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
        if (pLevel !is ServerLevel
            && pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor) return
        summonItem(pLevel as ServerLevel, pPos)
    }

     override fun attack(pState: BlockState, pLevel: Level, pPos: BlockPos, pPlayer: Player) {
        if (pLevel is ServerLevel
            && pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor
        ) {
            // todo: Do something like hurting or notifying the player here
        }
    }

     override fun tick(pState: BlockState, pLevel: ServerLevel, pPos: BlockPos, pRandom: RandomSource) {
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

    fun getThisTile(level: ServerLevel, pos: BlockPos) : BlockEntityFullMindAnchor {
        val tile = level.getBlockEntity(pos) as BlockEntityFullMindAnchor
        return tile
    }

    fun summonItem(level: ServerLevel, pos: BlockPos) {
        val itemStack = ItemStack(HexagonyBlocks.MIND_ANCHOR_FULL.value)
        // Create custom NBT
        val nbt = itemStack.getOrCreateCompound("BlockEntityTag")
        val tile = getThisTile(level, pos)
        val playerOrNull: ServerPlayer? = tile.storedPlayer
        val playerProfileOrNull = tile.playerNameHelper
        if (playerOrNull != null) {
            nbt.putUUID(TAG_STORED_PLAYER, playerOrNull.uuid)
        }
        if (playerProfileOrNull != null) {
            nbt.putCompound(TAG_STORED_PLAYER_PROFILE, NbtUtils.writeGameProfile(CompoundTag(), playerProfileOrNull))
        }
        // TODO:
        // We probably want to escape if either of the two above were null, and say sommething like
        // "ooOooooOOOoo the dormant spirit could not be woken..."
        println("What the sigma??")
        println(getThisTile(level, pos))
        val itemEntity = ItemEntity(
            level,
            pos.x + 0.5,   // Center in the block
            pos.y + 0.5,
            pos.z + 0.5,
            itemStack
        )
        itemEntity.setNoPickUpDelay() // ticks before it can be picked up
        val test = level.addFreshEntity(itemEntity)
        // OK. Now that it is an itemEntity, what now?
        // Update the player associated with this Anchor, and let them know that their soul is now
        // Splattered on the ground at that position?
        // Do I make ItemFullMindAnchor have a tick function
        // and continually update the reference?
    }
}