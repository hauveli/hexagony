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

    fun look(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.xRot = unpackX(amplifier)
        livingEntity.yRot = unpackY(amplifier)
    }

    fun attack(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is ServerPlayer) return
        // livingEntity.attack()
        // livingEntity.swing(livingEntity.usedItemHand)
        ControlHelperStuff.attack(livingEntity)
    }

    // I couldn't figure out a good way to do some of these without access to ServerPlayer...
    fun use(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is ServerPlayer) return
        //livingEntity.useItemRemainingTicks
        //livingEntity.useItem.useOnRelease()
        livingEntity.useItem.use(livingEntity.level(), livingEntity, livingEntity.usedItemHand)

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
        if (livingEntity !is ServerPlayer) return
        livingEntity.inventory.selected = amplifier
    }

    fun swapHands(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is ServerPlayer) return
        val tempItemStack = livingEntity.getItemInHand(InteractionHand.MAIN_HAND)
        livingEntity.setItemInHand(InteractionHand.MAIN_HAND, livingEntity.getItemInHand(InteractionHand.OFF_HAND))
        livingEntity.setItemInHand(InteractionHand.OFF_HAND, tempItemStack)
    }

    fun drop(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is ServerPlayer) return
        val entireStack = amplifier > 0
        livingEntity.drop(entireStack)
    }
}