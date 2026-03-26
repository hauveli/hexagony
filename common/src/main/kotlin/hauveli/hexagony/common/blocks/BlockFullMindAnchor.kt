package hauveli.hexagony.common.blocks

//import hauveli.hexagony.common.lib.BlockProperties
//import hauveli.hexagony.common.lib.BlockProperties.Companion.FILLED
import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus.TAG_MEDIA
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus.TAG_PIGMENT
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.utils.getOrCreateCompound
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockRedstoneImpetus
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.xplat.IXplatAbstractions
import hauveli.hexagony.common.blocks.anchors.MindAnchor.Companion.TAG_STORED_PLAYER
import hauveli.hexagony.common.blocks.anchors.MindAnchor.Companion.TAG_STORED_PLAYER_PROFILE
import hauveli.hexagony.mind_anchor.MindAnchorData
import hauveli.hexagony.mind_anchor.MindAnchorManager
import hauveli.hexagony.registry.HexagonyBlockEntities
import hauveli.hexagony.registry.HexagonyBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
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
import org.jetbrains.annotations.Nullable

// BlockAbstractImpetus happens to have a lot of useful features I want so that's convenient
// Also I get to avoid having to construct quite a few classes which is awesome
class BlockFullMindAnchor(properties: Properties) :
    BlockAbstractMindAnchor(properties) {

    // Hmm...
    // BlockRedstoneImpetus already has a BlockEntity, how do we a void conflicts?
    @Nullable
    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity {
        return BlockEntityFullMindAnchor (pPos, pState)
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
        }
        return InteractionResult.PASS
    }

    /*
    override fun playerWillDestroy(pLevel: Level, pPos: BlockPos, state: BlockState, player: Player) {
        if (pLevel !is ServerLevel
            && pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor) return
        // TODO: if player is in creative, OR if item has empty tag, break as usual?
        summonItem(pLevel as ServerLevel, pPos)
    }
     */

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        super.setPlacedBy(level, pos, state, placer, stack)
        // Only run on the server
        if (!level.isClientSide) {
            val mindAnchor = level.getBlockEntity(pos) as BlockEntityFullMindAnchor
            val mindUUID = mindAnchor.getPlayerUuid()
            val minecraftServer = level.server
            // Update the MindAnchorSavedData to track as block
            if (minecraftServer != null && mindUUID != null) {
                MindAnchorManager.trackBlock(
                    minecraftServer,
                    mindUUID,
                    mindAnchor
                )
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
        // I don't undertsand the purpose of the code below
        if (pLevel.getBlockEntity(pPos) is BlockEntityFullMindAnchor) {
            // val tile = pLevel.getBlockEntity(pPos) as BlockEntityFullMindAnchor
            // tile.updatePlayerProfile()
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
                }
            }
        }
    }

    companion object {
        val POWERED: BooleanProperty  = BooleanProperty.create("powered")
    }

    /*
        Need:
        Player UUID
        Player GameProfile
        DUST_AMOUNT
        TAG_PIGMENT
     */

    fun summonItem(level: ServerLevel, pos: BlockPos) {
        val itemStack = ItemStack(HexagonyBlocks.MIND_ANCHOR_FULL.value)
        val thisItemsNBT = itemStack.getOrCreateCompound("BlockEntityTag")
        // Create custom NBT
        val tile = level.getBlockEntity(pos) as BlockEntityFullMindAnchor
        tile.helperApplyNbt(thisItemsNBT)
        // TODO:
        // We probably want to escape if either of the two above were null, and say sommething like
        // "ooOooooOOOoo the dormant spirit could not be woken..."
        val itemEntity = ItemEntity(
            level,
            pos.x + 0.5,   // Center in the block
            pos.y + 0.5,
            pos.z + 0.5,
            itemStack
        )
        itemEntity.setNoPickUpDelay() // ticks before it can be picked up
        level.addFreshEntity(itemEntity)

        val mindUUID = thisItemsNBT.getUUID(TAG_STORED_PLAYER)
        // Update the MindAnchorSavedData to track as block
        if (mindUUID != null) {
            MindAnchorManager.trackItemEntity(
                level.server,
                mindUUID,
                itemEntity
            )
        }
        // OK. Now that it is an itemEntity, what now?
        // Update the player associated with this Anchor, and let them know that their soul is now
        // Splattered on the ground at that position?
        // Do I make ItemFullMindAnchor have a tick function
        // and continually update the reference?
    }

    /*
     No reason to bother killing all other instances if I code properly
     Only update player's reference if needed, in case server did not save nicely?
     Would need to know what to do when an ItemStack, when an ItemEntity AND when a BlockEntity...
     Possible solutions:
     Server (NOT player!) should store a list of mind anchored players
     each entry in the list should have these fields if possible
        dimension: ResourceLocation?
        position: Vec3
        type: ItemStack, ItemEntity, BlockEntity
        holder: ServerPlayer? // needs to account for offline players too... Just drop it when logging out instead? no need to manage offline players then...

        getMindAnchor() // Gets dimension, Position in Dimension of Mind Anchor (Without stating form or holder)

        setMindAnchor(dimension, position) // Places the block at that location as a BlockEntity, if possible, does nothing if not
     */
    fun onLoad() {

    }
}