package hauveli.hexagony.features.control

import hauveli.hexagony.features.control.FakePlayerControlHelperStuff.unpackX
import hauveli.hexagony.features.control.FakePlayerControlHelperStuff.unpackY
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

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

    // Works
    fun look(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.xRot = unpackX(amplifier)
        livingEntity.yRot = unpackY(amplifier)
    }

    // entity portion works, mining blocks does NOT. no animation is played, either.
    //
    fun attack(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is Player) return // animation plays if I set this to LocalPlayer... hmmm...
        RealPlayerControlHelperStuff.localAttack(livingEntity)
    }

    // doesn't work
    fun use(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is LocalPlayer) return
        livingEntity.useItem.use(livingEntity.level(), livingEntity, livingEntity.usedItemHand)
    }

    // Works
    fun hotbarSlot(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is LocalPlayer) return
        livingEntity.inventory.selected = amplifier
    }

    // Works
    fun swapHands(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is LocalPlayer) return
        val tempItemStack = livingEntity.getItemInHand(InteractionHand.MAIN_HAND)
        livingEntity.setItemInHand(InteractionHand.MAIN_HAND, livingEntity.getItemInHand(InteractionHand.OFF_HAND))
        livingEntity.setItemInHand(InteractionHand.OFF_HAND, tempItemStack)
    }

    // Works
    fun drop(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is LocalPlayer) return
        val entireStack = amplifier > 0
        livingEntity.drop(entireStack)
    }
}