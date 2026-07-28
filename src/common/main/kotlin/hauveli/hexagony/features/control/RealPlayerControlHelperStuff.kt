package hauveli.hexagony.features.control

import hauveli.hexagony.Hexagony
import hauveli.hexagony.client.HexagonyClient.MINECRAFT
import hauveli.hexagony.features.control.FakePlayerControlHelperStuff.placeBlockOrInteract
import hauveli.hexagony.features.fake_player.FakeServerPlayer
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import org.apache.logging.log4j.core.jmx.Server
import kotlin.math.max

object RealPlayerControlHelperStuff {
    // referenced this a little: https://www.mcpk.wiki/wiki/Angles
    // how it feels to do some nonsense :ridingmybikey:
    // Originally was going to use a big (65536) table for it but this is a bit easier to understand
    // I think this may be lossy on some floating point values (which ones?)
    // todo: determine if edge cases on some angles or if I'm ok to not change this at all
    private const val DEG_TO_PACKED = (65536.0 / 360.0).toFloat()
    private const val PACKED_TO_DEG = (360.0 / 65536.0).toFloat()

    fun pack(yRot: Float, xRot: Float): Int {
        val x = (xRot * DEG_TO_PACKED).toInt() and 0xFFFF
        val y = (yRot * DEG_TO_PACKED).toInt() and 0xFFFF

        return (x shl 16) or y
    }

    fun unpackX(packed: Int): Float {
        val xIndex = (packed ushr 16) and 0xFFFF
        return xIndex * PACKED_TO_DEG
    }

    fun unpackY(packed: Int): Float {
        val yIndex = packed and 0xFFFF
        return yIndex * PACKED_TO_DEG
    }

    fun getPlayerTarget(player: Player): HitResult {
        val start = player.eyePosition
        val reach = max(
            player.attributes.getValue(Attributes.ENTITY_INTERACTION_RANGE),
            player.attributes.getValue(Attributes.BLOCK_INTERACTION_RANGE)
        )
        val scaledLookAngle = player.lookAngle.scale(reach)
        val end = start.add(scaledLookAngle)

        val blockHit = player.level().clip(
            ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
            )
        )
        val entityHit = ProjectileUtil.getEntityHitResult(
            player,
            start,
            end,
            player.boundingBox.expandTowards(scaledLookAngle).inflate(1.0),
            { entity -> entity != player && entity.isPickable },
            reach * reach
        )

        if (entityHit == null) return blockHit
        if (blockHit.type == HitResult.Type.MISS) return entityHit
        return if (player.position().subtract(entityHit.location).lengthSqr()
            <= player.position().subtract(blockHit.location).lengthSqr()) {
            entityHit
        } else {
            blockHit
        }
    }

    fun localBreakBlock(player: Player, hitResult: BlockHitResult, miningProgress: FakeServerPlayer.MiningProgress) {
        val gm = MINECRAFT!!.gameMode!!

        // gm.isDestroying is true when I open chat.
        // this means gm.continueDestroyBlock is called at that time.
        // I should track this perhaps using miningProgress instead?
        if (miningProgress.progress > 0f) {
            gm.continueDestroyBlock(miningProgress.pos, hitResult.direction)
        } else {
            gm.startDestroyBlock(miningProgress.pos, hitResult.direction)
            miningProgress.progress = 1f
        }
    }

    fun resetMiningProgress(player: Player, miningProgress: FakeServerPlayer.MiningProgress) {
        player.level().destroyBlockProgress(player.id, miningProgress.pos, 0)
        MINECRAFT!!.gameMode!!.stopDestroyBlock()
        miningProgress.progress = 0f
    }

    val localMiningProgress = FakeServerPlayer.MiningProgress()

    fun attack(player: Player) {
        if (!player.level().isClientSide) return
        val hitResult = getPlayerTarget(player)
        player.swing(player.usedItemHand) // swing no matter what
        val key = MINECRAFT!!.options.keyAttack
        key.isDown = true
        when (hitResult.type) {
            HitResult.Type.MISS -> {
                // does this even help the behavior? hmmm....
                // MINECRAFT.options.keyAttack.isDown = false
            }
            HitResult.Type.ENTITY -> {
                MINECRAFT.gameMode!!.attack(player, (hitResult as EntityHitResult).entity)
                resetMiningProgress(player, localMiningProgress)
            }
            HitResult.Type.BLOCK -> {
                val targetPos = (hitResult as BlockHitResult).blockPos
                if (localMiningProgress.pos != targetPos) {
                    resetMiningProgress(player, localMiningProgress)
                    localMiningProgress.pos = targetPos
                }
                localBreakBlock(player, hitResult, localMiningProgress)
                // MINECRAFT.gameMode!!.destroyBlock(hitResult.blockPos) // does it instantly so nuh uh
            }
        }
        key.consumeClick()
    }

    fun placeBlockOrInteract(player: LocalPlayer, hit: BlockHitResult): Boolean {
        val result = MINECRAFT!!.gameMode!!.useItemOn(
            player,
            player.usedItemHand,
            hit
        )

        return result.consumesAction()
    }

    fun use(player: LocalPlayer) {
        // todo: how the fuck do I check this in a sane way?

        val hitResult = FakePlayerControlHelperStuff.getPlayerTarget(player)
        val key = MINECRAFT!!.options.keyUse
        key.isDown = true
        when (hitResult.type) {
            HitResult.Type.MISS -> {
                // this works if I open chat? hhmmmmm.....
                // MINECRAFT!!.gameMode!!.useItem(player, player.usedItemHand)
                // MINECRAFT.gameMode!!.useItem(player, player.usedItemHand)
                // player.getItemInHand(player.usedItemHand).use(player.level(), player, player.usedItemHand)
                // how can I unfuck this when in freecam....
                MINECRAFT.gameMode!!.useItem(player, player.usedItemHand)
                //MINECRAFT.gameMode!!.useItem(MINECRAFT.player!!, MINECRAFT.player!!.usedItemHand)
                //Hexagony.LOGGER.info(player.usedItemHand)
                //Hexagony.LOGGER.info(player.getItemInHand(player.usedItemHand))
                //val a = player.getItemInHand(player.usedItemHand).use(player.level(), player, player.usedItemHand)
                //Hexagony.LOGGER.info(a.result)
                // player.useItem.use(player.level(), player, player.usedItemHand)
            }
            HitResult.Type.ENTITY -> {
                player.interactOn((hitResult as EntityHitResult).entity, InteractionHand.MAIN_HAND)
                // player.interactAt(player, hitResult.location, InteractionHand.MAIN_HAND)
            }
            HitResult.Type.BLOCK -> {
                val interacted = placeBlockOrInteract(player, hitResult as BlockHitResult)
                if (!interacted && player.useItem.item !is BlockItem) {
                    MINECRAFT.gameMode!!.useItem(player, player.usedItemHand)
                    //MINECRAFT.gameMode!!.useItem(MINECRAFT.player!!, MINECRAFT.player!!.usedItemHand)
                    //Hexagony.LOGGER.info(player.usedItemHand)
                    //Hexagony.LOGGER.info(player.getItemInHand(player.usedItemHand))
                    //val a = player.getItemInHand(player.usedItemHand).use(player.level(), player, player.usedItemHand)
                    // Hexagony.LOGGER.info(a.result)
                    // player.useItem.use(player.level(), player, player.usedItemHand)
                }
            }
        }
        key.consumeClick()
    }
}