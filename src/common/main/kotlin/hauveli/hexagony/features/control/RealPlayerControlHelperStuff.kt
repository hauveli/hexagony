package hauveli.hexagony.features.control

import hauveli.hexagony.client.HexagonyClient.MINECRAFT
import hauveli.hexagony.features.fake_player.FakeServerPlayer
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
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

    fun localBreakBlock(player: Player, miningProgress: FakeServerPlayer.MiningProgress) {
        val gm = MINECRAFT!!.gameMode!!
        if (miningProgress.progress != 0f) {
            gm.continueDestroyBlock(miningProgress.pos, Direction.DOWN)
        } else {
            gm.startDestroyBlock(miningProgress.pos, Direction.DOWN)
        }

        /*
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

         */
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

    val localMiningProgress = FakeServerPlayer.MiningProgress()

    fun localAttack(player: Player) {
        if (!player.level().isClientSide) return
        val hitResult = getPlayerTarget(player)
        player.swing(player.usedItemHand) // swing no matter what
        when (hitResult.type) {
            HitResult.Type.MISS -> {return}
            HitResult.Type.ENTITY -> {
                player.attack((hitResult as EntityHitResult).entity)
                resetMiningProgress(player, localMiningProgress)
            }
            HitResult.Type.BLOCK -> {
                val targetPos = (hitResult as BlockHitResult).blockPos
                if (localMiningProgress.pos != targetPos) {
                    MINECRAFT!!.gameMode!!.stopDestroyBlock()
                    resetMiningProgress(player, localMiningProgress)
                    localMiningProgress.pos = targetPos
                }
                localBreakBlock(player, localMiningProgress)
            }
        }
    }
}