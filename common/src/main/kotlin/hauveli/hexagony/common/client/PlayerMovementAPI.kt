package hauveli.hexagony.common.client

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.world.level.Level

object PlayerMovementAPI {

    private val mc: Minecraft
        get() = Minecraft.getInstance()

    private val player: LocalPlayer?
        get() = mc.player

    private val level: Level?
        get() = mc.level

    fun onClientTick() {
        val p = player ?: return

        // If shouldMoveForwardBackward is 0 and we set p.zza it may conflict, check needed, I think...
        if (shouldMoveForwardBackward != 0f) {
            p.zza = shouldMoveForwardBackward   // forwar
        }
        if (shouldMoveLeftRight != 0f) {
            p.xxa = shouldMoveLeftRight   // side
        }
        if (shouldLookUpDown != 0f) {
            p.yRot = shouldLookUpDown
        }
        if (shouldLookLeftRight != 0f) {
            p.xRot = shouldLookLeftRight
        }
        if (shouldJump && p.onGround()) {
            p.jumpFromGround()
            // shouldJump = false // do I want it to be continuous if no other inputs?
        }
        if (shouldSprint && p.canSprint()) {
            p.isSprinting = true
        }
        if (shouldSneak) {
            p.isShiftKeyDown = true
        }

        if (shouldAttack) {
            // p.swing is local
            if (shouldAttackPeriod == -1) {

                // TODO: do I really want to do it this way?
                // Hmm...
                p.swing(player!!.usedItemHand) // Momentary
                shouldAttack = false
            } else if (shouldAttackPeriod == 0) {
                p.swing(player!!.usedItemHand) // Continuous
            } else if ((level!!.gameTime % shouldAttackPeriod) == 0L) {
                p.swing(player!!.usedItemHand) // Periodic
            }
        }

        if (shouldUse) {
            if (shouldUsePeriod == -1) {
                // TODO: do I really want to do it this way?
                // Hmm...
                mc.options.keyUse.isDown = true // Hmm.... I don't think I can use .isDown because it would conflict...
                shouldUse = false
            } else if (shouldUsePeriod == 0) {
                p.swing(player!!.usedItemHand) // Continuous
            } else if ((level!!.gameTime % shouldUsePeriod) == 0L) {
                p.swing(player!!.usedItemHand) // Periodic
            }
        }


        if (shouldHotbarSlot != 0) {
            p.inventory.selected = shouldHotbarSlot
            shouldHotbarSlot = 0 // reset, no reason to be persistent?
        }

        if (shouldSwapHands) {
            p.connection.send(
                ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                    BlockPos.ZERO,
                    Direction.DOWN
                )
            )
            shouldSwapHands = false
        }

    }

    fun jump() {
        shouldJump = true
    }

    fun stop() {
        shouldJump = false
        shouldMoveForwardBackward = 0f
        shouldMoveLeftRight = 0f
    }

    fun forward() {
        shouldMoveForwardBackward = 1.0f
    }

    fun backward() {
        shouldMoveForwardBackward = -1.0f
    }

    fun left() {
        shouldMoveLeftRight = 1.0f
    }

    fun right() {
        shouldMoveLeftRight = -1.0f
    }

    fun sprint(sprint: Boolean) {
        shouldSprint = sprint
    }

    fun sneak() {
        shouldSneak = !shouldSneak
    }

    fun attack() {
        shouldAttack = true
    }

    fun attackOnce() {
        shouldAttack = true
    }

    fun attackContinuous() {
        shouldAttack = true
        shouldAttackPeriod = 1
    }

    fun attackPeriodic(period: Int) {
        shouldAttack = true
        shouldAttackPeriod = period
    }

    fun use() {
        shouldUse = true
    }

    fun useOnce() {
        shouldUse = true
    }

    fun useContinuous() {
        shouldUse = true
        shouldUsePeriod = 1
    }

    fun usePeriodic(period: Int) {
        shouldUse = true
        shouldUsePeriod = period
    }


    // can implement this, but realistically, if shouldUse or shouldAttack are bound, we have hands, and should be allowed to do this
    // only needed if shouldAttack or shouldUse are NOT bound.
    fun hotbar(number: Int) {
        shouldHotbarSlot = number
    }

    fun swapHands() {
        shouldSwapHands = true
    }


    fun moveLongitudinal(walking: Float) {
        shouldMoveForwardBackward = walking
    }

    fun moveLatitudinal(walking: Float) {
        shouldMoveLeftRight = walking
    }

    var shouldMoveForwardBackward = 0f // ws
    var shouldMoveLeftRight = 0f // ad
    var shouldLookUpDown = 0f // pitch
    var shouldLookLeftRight = 0f // yaw
    var shouldLookRoll = 0f // roll if I can find a use?
    var shouldJump = false // space
    var shouldSprint = false // ctrl
    var shouldSneak = false // shift

    var shouldAttack = false // lmb
    var shouldAttackPeriod = 0

    var shouldUse = false // rmb
    var shouldUsePeriod = 0

    var shouldSwapHands = false
    var shouldHotbarSlot = 0
}