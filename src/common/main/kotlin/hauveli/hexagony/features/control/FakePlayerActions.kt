package hauveli.hexagony.features.control

import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.control.ControlledMobEffects.makeControlEffect
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import kotlin.math.PI
import kotlin.math.sin

object FakePlayerActions {

    fun walkForward(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.zza = 1f
    }

    fun walkBackward(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.zza = -1f
    }

    fun walkLeft(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.xxa = 1f
    }

    fun walkRight(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.xxa = -1f
    }

    fun sprint(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.isSprinting = livingEntity.canSprint()
    }

    fun sneak(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.isShiftKeyDown = true
    }

    fun jump(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity.onGround()) {
            livingEntity.jumpFromGround()
        } else if (livingEntity.isInWater || livingEntity.isInLava) {
            livingEntity.setJumping(true)
        } else {
            livingEntity.setJumping(false)
        }
    }

    private const val DEG_TO_INDEX = (65536.0 / 360.0).toFloat()
    private const val INDEX_TO_DEG = (360.0 / 65536.0).toFloat()

    fun pack(yRot: Float, xRot: Float): Int {
        val x = (xRot * DEG_TO_INDEX).toInt() and 0xFFFF
        val y = (yRot * DEG_TO_INDEX).toInt() and 0xFFFF

        return (x shl 16) or y
    }

    fun look(livingEntity: LivingEntity, amplifier: Int) {
        // how it feels to do some nonsense :ridingmybikey:
        val xIndex = (amplifier ushr 16) and 0xFFFF
        val yIndex = amplifier and 0xFFFF

        livingEntity.xRot = xIndex * INDEX_TO_DEG
        livingEntity.yRot = yIndex * INDEX_TO_DEG
    }

    fun attack(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.swing(livingEntity.usedItemHand)
    }

    fun use(livingEntity: LivingEntity, amplifier: Int) {

        // p.lookAngle
        /*
        val hit = p.gameMode. .hitResult ?: return
        if (hit.type != HitResult.Type.BLOCK) return
        hit as BlockHitResult
        val useContext = UseOnContext(
            p,
            InteractionHand.MAIN_HAND,
            hit
        )
        p.useItem.useOn(
            useContext
        )
         */
        //p.swing(player!!.usedItemHand) // Periodic
    }

    // reads entity data to get slot value
    fun hotbarSlot(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is Player) return
        // livingEntity.entityData.get<>()
        livingEntity.inventory.selected = amplifier
    }

    fun swapHands(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is Player) return
        val tempItemStack = livingEntity.getItemInHand(InteractionHand.MAIN_HAND)
        livingEntity.setItemInHand(InteractionHand.MAIN_HAND, livingEntity.getItemInHand(InteractionHand.OFF_HAND))
        livingEntity.setItemInHand(InteractionHand.OFF_HAND, tempItemStack)
    }

    fun drop(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is Player) return
        val tempItemStack = livingEntity.getItemInHand(InteractionHand.MAIN_HAND)
        val entireStack = amplifier > 0
        if (!tempItemStack.isEmpty) {
            livingEntity.drop(tempItemStack, entireStack)
        } else {
            val otherItemStack = livingEntity.getItemInHand(InteractionHand.OFF_HAND)
            if (!otherItemStack.isEmpty) {
                livingEntity.drop(otherItemStack, entireStack)
            }
        }
    }
}