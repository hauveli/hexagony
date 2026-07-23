package hauveli.hexagony.features.control

import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.control.ControlHelperStuff.unpackX
import hauveli.hexagony.features.control.ControlHelperStuff.unpackY
import hauveli.hexagony.features.control.ControlledMobEffects.makeControlEffect
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import kotlin.math.PI
import kotlin.math.sin

object RealPlayerActions {

    // walks dont work
    fun stopWalkingForwardsBackwards(livingEntity: LivingEntity) {
        livingEntity.zza = 0f
    }

    fun walkForward(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.zza = 1f
    }

    fun walkBackward(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.zza = -1f
    }

    fun stopWalkingLeftRight(livingEntity: LivingEntity) {
        livingEntity.xxa = 0f
    }

    fun walkLeft(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.xxa = 1f
    }

    fun walkRight(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.xxa = -1f
    }

    // I mean, it works, but it also feels a little jank. low priority.
    fun stopSprinting(livingEntity: LivingEntity) {
        livingEntity.isSprinting = false
    }

    fun sprint(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.isSprinting = livingEntity.canSprint()
    }

    // sneak doesn't work properly, sneaks once then stops instantly? jank
    fun stopSneaking(livingEntity: LivingEntity) {
        livingEntity.isShiftKeyDown = false
    }

    fun sneak(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.isShiftKeyDown = true
    }

    // jump works ok, but not in water
    fun stopJumping(livingEntity: LivingEntity) {
        livingEntity.setJumping(false)
    }

    fun jump(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity.onGround()) {
            livingEntity.jumpFromGround()
            // setjumping doesn't seem to work for underwater stuff, need to fix
        } else if (livingEntity.isInWater || livingEntity.isInLava) {
            livingEntity.setJumping(true)
        } else {
            livingEntity.setJumping(false)
        }
    }

    // look works fine
    fun look(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.xRot = unpackX(amplifier)
        livingEntity.yRot = unpackY(amplifier)
    }

    // entity portion works, mining blocks does NOT. no animation is played, either.
    fun attack(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is ServerPlayer) return
        ControlHelperStuff.attack(livingEntity)
    }

    // doesn't work
    fun use(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is ServerPlayer) return
        livingEntity.useItem.use(livingEntity.level(), livingEntity, livingEntity.usedItemHand)
    }

    // doesn't work
    fun hotbarSlot(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is ServerPlayer) return
        livingEntity.inventory.selected = amplifier
    }

    //  seems to work
    fun swapHands(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is ServerPlayer) return
        val tempItemStack = livingEntity.getItemInHand(InteractionHand.MAIN_HAND)
        livingEntity.setItemInHand(InteractionHand.MAIN_HAND, livingEntity.getItemInHand(InteractionHand.OFF_HAND))
        livingEntity.setItemInHand(InteractionHand.OFF_HAND, tempItemStack)
    }

    // works visually, but doesn't actually
    fun drop(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is ServerPlayer) return
        val entireStack = amplifier > 0
        livingEntity.drop(entireStack)
    }
}