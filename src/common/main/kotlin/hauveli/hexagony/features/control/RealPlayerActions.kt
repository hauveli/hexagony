package hauveli.hexagony.features.control

import at.petrak.hexcasting.api.HexAPI
import hauveli.hexagony.Hexagony
import hauveli.hexagony.client.HexagonyClient.MINECRAFT
import hauveli.hexagony.features.control.FakePlayerControlHelperStuff.unpackX
import hauveli.hexagony.features.control.FakePlayerControlHelperStuff.unpackY
import hauveli.hexagony.features.freecam.FreeCameraEntity
import hauveli.hexagony.features.freecam.FreeCameraServerData
import hauveli.hexagony.mixin.control.IsCrouchingLocalPLayerAccessorMixin
import net.minecraft.client.KeyMapping
import net.minecraft.client.player.LocalPlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object RealPlayerActions {


    // These are from the parkour wiki
    object movementCalculation {
        val SPRINTING = 1.3
        val WALKING = 1.0
        val SNEAKING = 0.3
        val STOPPING = 0.0
        val ANGLE_DEFAULT = 0.98
        val ANGLE_STRAFE = 1.0
        val ANGLE_STRAFE_SNEAK = 0.98 * sqrt(2.0)
        val SLIPPERINESS_DEFAULT = 0.6
        val SLIPPERINESS_SLIME = 0.8
        val SLIPPERINESS_ICE = 0.98
        val SLIPPERINESS_AIRBORNE = 0.0
        val SPEED_INCREASE_PER_LEVEL = 0.2
        val SLOWNESS_DECREASE_PER_LEVEL = 0.15

        var velocity_old = 0.0
    }

    val SPEED_MULT: Double = 0.36

    fun getSpeedMultiplier(entity: LivingEntity): Double {

        // todo: what the fuck was I referring to here?
        // TODO: 1.21.1+? make else if in game versions where the bug is fixed
        // i just guesstimated the unlabelled values here based on how it felt to play
        val sneakMult = if (entity.isShiftKeyDown) movementCalculation.SNEAKING else 1.0
        val sprintMult = if (entity.isSprinting) movementCalculation.SPRINTING else 1.0
        val viscosityMult = if (entity.isInWater || entity.isInLava) 0.3 else 1.0
        val groundAccel = if (entity.onGround()) {
            SPEED_MULT * (entity.level().getBlockState(entity.blockPosition().below()).block.friction * 0.91)
        } else if (entity.isSprinting) {
            0.075
        } else {
            0.045 // decelerate a lot in the air if not sprintjumping
        }
        val finalMult = min(sneakMult * sprintMult * groundAccel * viscosityMult, 3.0)
        return finalMult
    }

    // walks dont work how I want, no sprinting really...
    fun stopWalkingForwardsBackwards(livingEntity: LivingEntity) {
        // livingEntity.zza = 0f
    }

    fun walkForward(livingEntity: LivingEntity, amplifier: Int) {
        // livingEntity.zza = 1f
        // with how I'm doing it:
        // check if freecam and if so, don't worry about inputs
        // if not in free-cam, don't run this if forward is being held
        // if (freecamera || !holdingForward)
        // if (!freecamera && holdingForward) return
        if (MINECRAFT!!.options.keyUp.isDown && !FreeCameraEntity.active) return
        // do I need to check if swimming/flying/whatever for this to make sense? hmm...
        if (livingEntity !is LocalPlayer) return
        val headAngle = Vec3.directionFromRotation(0f, livingEntity.yHeadRot)
        val speedMult = getSpeedMultiplier(livingEntity)
        livingEntity.addDeltaMovement(headAngle.scale(speedMult))
        // player.input.forwardImpulse = 1f
        // player.input.up = true
        // player.zza = -1f
        // livingEntity.zza = -1f
        //livingEntity.input.up = true
        //livingEntity.input.forwardImpulse = 1f
    }

    fun walkBackward(livingEntity: LivingEntity, amplifier: Int) {
        if (MINECRAFT!!.options.keyDown.isDown && !FreeCameraEntity.active) return
        // do I need to check if swimming/flying/whatever for this to make sense? hmm...
        if (livingEntity !is LocalPlayer) return
        val headAngle = Vec3.directionFromRotation(0f, livingEntity.yHeadRot)
        val speedMult = getSpeedMultiplier(livingEntity)
        livingEntity.addDeltaMovement(headAngle.scale(-speedMult))
    }

    fun stopWalkingLeftRight(livingEntity: LivingEntity) {
        // livingEntity.xxa = 0f
    }

    fun walkLeft(livingEntity: LivingEntity, amplifier: Int) {
        if (MINECRAFT!!.options.keyLeft.isDown && !FreeCameraEntity.active) return
        // do I need to check if swimming/flying/whatever for this to make sense? hmm...
        if (livingEntity !is LocalPlayer) return
        val headAngle = Vec3.directionFromRotation(0f, livingEntity.yHeadRot + 90f)
        val speedMult = getSpeedMultiplier(livingEntity)
        livingEntity.addDeltaMovement(headAngle.scale(-speedMult))
    }

    fun walkRight(livingEntity: LivingEntity, amplifier: Int) {
        if (MINECRAFT!!.options.keyRight.isDown && !FreeCameraEntity.active) return
        // do I need to check if swimming/flying/whatever for this to make sense? hmm...
        if (livingEntity !is LocalPlayer) return
        val headAngle = Vec3.directionFromRotation(0f, livingEntity.yHeadRot + 90f)
        val speedMult = getSpeedMultiplier(livingEntity)
        livingEntity.addDeltaMovement(headAngle.scale(speedMult))
    }

    // Works
    fun stopSprinting(livingEntity: LivingEntity) {
        livingEntity.isSprinting = false
    }

    fun sprint(livingEntity: LivingEntity, amplifier: Int) {
        // using canSprint causes it to tweak which is annoying, hmm...§
        // livingEntity.canSprint())
        val localPlayer = MINECRAFT!!.player
        if (localPlayer != null
            && localPlayer.canSprint()
            && (localPlayer.getFoodData().foodLevel > 6
                    || localPlayer.abilities.instabuild) // todo: figure out if there's a less jank way to check...
            && localPlayer.input != null
            && localPlayer.input.hasForwardImpulse()) {
            /*
            && livingEntity.deltaMovement.lengthSqr() > 0.00001
            && livingEntity.forward.dot(livingEntity.deltaMovement) > 0) {
             */
            livingEntity.isSprinting = true
            // MINECRAFT!!.player!!.isSprinting = true
        } else {
            stopSprinting(livingEntity)
        }
    }

    // todo: AHHHHH I CANT EVEN BEGIN TO FIGURE IT OUT
    // sneak doesn't work properly, sneaks once then stops instantly? jank
    fun stopSneaking(livingEntity: LivingEntity) {
        livingEntity.isShiftKeyDown = false
        if (livingEntity !is LocalPlayer) return
        // livingEntity.input.shiftKeyDown = true
        // livingEntity as IsCrouchingLocalPLayerAccessorMixin
        // livingEntity.isCrouching = false
    }

    val sneakPoses = listOf(
        Pose.CROUCHING
        //Pose.SWIMMING
    )

    fun sneak(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.isShiftKeyDown = true
        livingEntity.pose = Pose.CROUCHING
        if (livingEntity !is LocalPlayer) return
        // livingEntity.input.shiftKeyDown = true
        /*
        livingEntity as IsCrouchingLocalPLayerAccessorMixin
        livingEntity.isCrouching = true
        livingEntity.crouching = true
        livingEntity.setWasShiftKeyDown(true)
        livingEntity.wasShiftKeyDown = true

         */
        // livingEntity.input.shiftKeyDown = true
        // Hexagony.LOGGER.info("teeest")
    }

    // jump works ok, but not in water
    fun stopJumping(livingEntity: LivingEntity) {
        livingEntity.setJumping(false)
    }

    // todo: not happy with solution for behavior in water.
    fun jump(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity.onGround()) {
            livingEntity.jumpFromGround()
            // setjumping doesn't seem to work for underwater stuff, need to fix
        } else if (livingEntity.isInWater || livingEntity.isInLava) {
            livingEntity.setJumping(true)
            livingEntity.addDeltaMovement(Vec3(0.0,0.05,0.0))
        } else {
            livingEntity.setJumping(false)
        }
    }

    // Works
    fun look(livingEntity: LivingEntity, amplifier: Int) {
        livingEntity.xRot = unpackX(amplifier)
        livingEntity.yRot = unpackY(amplifier)
    }


    fun stopAttack(livingEntity: LivingEntity) {
        MINECRAFT!!.options.keyAttack.isDown = false
    }

    // Works
    fun attack(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is Player) return // animation plays if I set this to LocalPlayer... hmmm...
        RealPlayerControlHelperStuff.attack(livingEntity)
    }

    fun stopUse(livingEntity: LivingEntity) {
        MINECRAFT!!.options.keyUse.isDown = false
    }

    // eating no worky, place block and interact work
    fun use(livingEntity: LivingEntity, amplifier: Int) {
        if (livingEntity !is LocalPlayer) return
        RealPlayerControlHelperStuff.use(livingEntity)
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