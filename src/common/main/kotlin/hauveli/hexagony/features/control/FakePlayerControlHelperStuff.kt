package hauveli.hexagony.features.control

import at.petrak.hexcasting.ktxt.UseOnContext
import hauveli.hexagony.Hexagony
import hauveli.hexagony.client.HexagonyClient.MINECRAFT
import hauveli.hexagony.features.fake_player.FakeServerPlayer
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import kotlin.math.max

object FakePlayerControlHelperStuff {
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

    fun serverBreakBlock(player: Player, miningProgress: FakeServerPlayer.MiningProgress) {
        val job = miningProgress
        val state = player.level().getBlockState(job.pos)
        if (state.isAir) return
        val destroySpeed = state.getDestroyProgress(player, player.level(), job.pos)
        job.progress += destroySpeed

        val stage = (job.progress * 10).toInt().coerceAtMost(9)
        player.level().destroyBlockProgress(player.id, job.pos, stage)

        if (job.progress >= 1.0f) {
            if (player is ServerPlayer) {
                player.gameMode.destroyBlock(job.pos)
            }
            job.progress = 0f
        }
    }

    fun resetMiningProgress(player: Player, miningProgress: FakeServerPlayer.MiningProgress) {
        player.level().destroyBlockProgress(player.id, miningProgress.pos, 0)
        miningProgress.progress = 0f
    }

    fun attack(player: Player) {
        val hitResult = getPlayerTarget(player)
        player.swing(player.usedItemHand) // swing no matter what
        when (hitResult.type) {
            HitResult.Type.MISS -> {return}
            HitResult.Type.ENTITY -> {
                player.attack((hitResult as EntityHitResult).entity)
                if (player !is FakeServerPlayer) return
                resetMiningProgress(player, player.miningProgress)
            }
            HitResult.Type.BLOCK -> {
                if (player !is FakeServerPlayer) return
                val targetPos = (hitResult as BlockHitResult).blockPos
                if (player.miningProgress.pos != targetPos) {
                    resetMiningProgress(player, player.miningProgress)
                    player.miningProgress.pos = targetPos
                }
                serverBreakBlock(player, player.miningProgress)
            }
        }
    }

    // todo: AHHHHHHHHHHHHHH
    fun placeBlock(player: ServerPlayer, hit: BlockHitResult): Boolean {
        val result = player.gameMode.useItemOn(
            player,
            player.serverLevel(),
            player.useItem,
            player.usedItemHand,
            hit
        )

        // player.gameMode.useItem()

        // player.level().setBlock()

        /*
        // what the fuck why is this the exact same but with flipped ABCD BADC ????
        val result = player.useItem.useOn(
            UseOnContext(
                player.level(),
                player,
                player.usedItemHand,
                player.useItem,
                hit
            )
        )
        */

        return result.consumesAction()
    }

    fun use(player: ServerPlayer) {
        // todo: how the fuck do I check this in a sane way?

        val hitResult = getPlayerTarget(player)
        when (hitResult.type) {
            HitResult.Type.MISS -> {
                player.useItem.use(player.level(), player, player.usedItemHand)
            }
            HitResult.Type.ENTITY -> {
                player.interactOn((hitResult as EntityHitResult).entity, InteractionHand.MAIN_HAND)
                // player.interactAt(player, hitResult.location, InteractionHand.MAIN_HAND)
            }
            HitResult.Type.BLOCK -> {
                val interacted = placeBlock(player, hitResult as BlockHitResult)
                if (!interacted && player.useItem.item !is BlockItem) {
                    player.useItem.use(player.level(), player, player.usedItemHand)
                }
            }
        }
    }
}