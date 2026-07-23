package hauveli.hexagony.features.control

import hauveli.hexagony.features.fake_player.FakeServerPlayer
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import kotlin.math.max

object ControlHelperStuff {
    // how it feels to do some nonsense :ridingmybikey:
    private const val DEG_TO_INDEX = (65536.0 / 360.0).toFloat()
    private const val INDEX_TO_DEG = (360.0 / 65536.0).toFloat()

    fun pack(yRot: Float, xRot: Float): Int {
        val x = (xRot * DEG_TO_INDEX).toInt() and 0xFFFF
        val y = (yRot * DEG_TO_INDEX).toInt() and 0xFFFF

        return (x shl 16) or y
    }

    fun unpackX(packed: Int): Float {
        val xIndex = (packed ushr 16) and 0xFFFF
        return xIndex * INDEX_TO_DEG
    }

    fun unpackY(packed: Int): Float {
        val yIndex = packed and 0xFFFF
        return yIndex * INDEX_TO_DEG
    }

    fun getPlayerTarget(player: ServerPlayer): HitResult {
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

    fun doMiningThing(player: FakeServerPlayer) {
        val job = player.miningProgress
        val state = player.serverLevel().getBlockState(job.pos)
        if (state.isAir) return
        val destroySpeed = state.getDestroyProgress(player, player.serverLevel(), job.pos)
        job.progress += destroySpeed

        val stage = (job.progress * 10).toInt().coerceAtMost(9)
        player.serverLevel().destroyBlockProgress(player.id, job.pos, stage)

        if (job.progress >= 1.0f) {
            player.gameMode.destroyBlock(job.pos)
            job.progress = 0f
        }
    }

    fun resetMiningProgress(player: FakeServerPlayer) {
        player.serverLevel().destroyBlockProgress(player.id, player.miningProgress.pos, 0)
        player.miningProgress.progress = 0f
    }

    fun attack(player: ServerPlayer) {
        val hitResult = getPlayerTarget(player)
        player.swing(player.usedItemHand) // swing no matter what
        when (hitResult.type) {
            HitResult.Type.MISS -> {return}
            HitResult.Type.ENTITY -> {
                player.attack((hitResult as EntityHitResult).entity)
            }
            HitResult.Type.BLOCK -> {
                if (player !is FakeServerPlayer) return
                val targetPos = (hitResult as BlockHitResult).blockPos
                if (player.miningProgress.pos != targetPos) {
                    resetMiningProgress(player)
                    player.miningProgress.pos = targetPos
                }
                doMiningThing(player)
            }
        }

    }
}