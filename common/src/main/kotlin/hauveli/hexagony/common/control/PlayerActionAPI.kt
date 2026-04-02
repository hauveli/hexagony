package hauveli.hexagony.common.control

import hauveli.hexagony.common.bilocation.FreeCameraEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.min
import kotlin.math.sqrt


object PlayerActionAPI {
    // TODO: god there has to be a better way to get the local player's data without duplicating code...
    // I could just write local to a static UUID, or store it in a static variable somewhere....
    // then access would be PlayerControlData.myEntry
    // God so much of the code is duplicated at this point but whatever, I just want it to work...

    private val mc: Minecraft
        get() = Minecraft.getInstance()

    private val player: LocalPlayer?
        get() = mc.player

    private val level: Level?
        get() = mc.level

    // Ex. players who connect to the server
    object Client {

        val e = PlayerControlData.getSelf()

        fun detach(bool: Boolean) {
            FreeCameraEntity.detachCamera(mc)
        }

        fun reattach(bool: Boolean) {
            FreeCameraEntity.reattachCamera(mc)
        }

        fun stop(bool: Boolean) {
            e.shouldMoveForwardBackward = 0f
            e.shouldMoveLeftRight = 0f
            e.shouldLookUpDown = 0f
            e.shouldLookLeftRight = 0f
            e.shouldLookRoll = 0f
            e.shouldLook = false
            e.shouldJump = false
            e.shouldSprint = false
            e.shouldSneak = false
            e.shouldAttack = false
            e.shouldAttackPeriod = -1
            e.shouldUse = false
            e.shouldUsePeriod = -1
            e.shouldSwapHands = false
            e.shouldHotbarSlot = -1
            e.shouldDrop = false
            e.shouldDropStack = false
        }

        fun moveForwardBackward(float: Float) {
            e.shouldMoveForwardBackward = float
        }

        fun moveLeftRight(float: Float) {
            e.shouldMoveLeftRight = float
        }

        fun lookUpDown(float: Float) {
            e.shouldLookUpDown = float
        }

        fun lookLeftRight(float: Float) {
            e.shouldLookLeftRight = float
        }

        fun lookRoll(float: Float) {
            e.shouldLookRoll = float
        }

        fun look(bool: Boolean) {
            e.shouldLook = bool
        }

        fun attackPeriodic(integer: Int) {
            println("Wow! ${integer}")
            e.shouldAttackPeriod = integer
        }

        fun hotbarSlot(integer: Int) {
            e.shouldHotbarSlot = integer
        }

        fun usePeriodic(integer: Int) {
            e.shouldUsePeriod = integer
        }

        fun jump(bool: Boolean) {
            e.shouldJump = bool
        }

        fun sprint(bool: Boolean) {
            e.shouldSprint = bool
        }

        fun sneak(bool: Boolean) {
            e.shouldSneak = bool
        }

        fun attack(bool: Boolean) {
            println("Wow! ${bool}")
            e.shouldAttack = bool
        }

        fun use(bool: Boolean) {
            e.shouldUse = bool
        }

        fun swapHands(bool: Boolean) {
            e.shouldSwapHands = bool
        }

        fun drop(bool: Boolean) {
            e.shouldDrop = bool
        }

        fun dropStack(bool: Boolean) {
            e.shouldDropStack = bool
        }
    }

    // Ex. players who live on the server (and do not have clients)
    object Server {

    }

    /*
    private val runtime = ConcurrentHashMap<UUID, MindAnchorRuntime>()

    private fun runtime(uuid: UUID) =
        runtime.computeIfAbsent(uuid) { MindAnchorRuntime() }
*/

    fun getInputVector(
        relative: Vec3,
        motionScaler: Float,
        facing: Float
    ): Vec3 {

        var input = relative

        val lengthSqr = input.lengthSqr()
        if (lengthSqr > 1.0) {
            input = input.normalize()
        }

        val scaled = input.scale(motionScaler.toDouble())

        val radians = facing * (Math.PI.toFloat() / 180f)

        val sin = Mth.sin(radians)
        val cos = Mth.cos(radians)

        return Vec3(
            scaled.x * cos - scaled.z * sin,
            0.0, //scaled.y,
            scaled.z * cos + scaled.x * sin
        )
    }

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

    val SPEED_MULT = 1

    private fun customMovement(entity: LivingEntity, input: Vec3) {
        val speed: Float = entity.speed //.getAttributeValue(Attributes.MOVEMENT_SPEED)
        // Normalize input
        val moveInput = if (input.lengthSqr() > 1.0)
            input.normalize()
        else
            input

        // Convert input relative to rotation
        val rotated = getInputVector(
            moveInput,
            speed, // entity.speed is taken into account here, but I don't know which things this includes...
            entity.yRot
        )

        // todo: make else if in game versions where the bug is fixed
        val sneakMult = if (entity.isShiftKeyDown) movementCalculation.SNEAKING else 1.0
        val sprintMult = if (entity.isSprinting) movementCalculation.SPRINTING else 1.0
        val viscosityMult = if (entity.isInWater || entity.isInLava) 0.3 else 1.0
        val groundAccel = if (entity.onGround()) {
            SPEED_MULT * (entity.level().getBlockState(entity.blockPosition().below()).block.friction * 0.91)
        } else {
            0.15
        }

        // Maximum length of rotated must be 4 or 5 ish?
        val finalMult = min(sneakMult * sprintMult * groundAccel * viscosityMult, 3.0)
        println(finalMult)
        entity.addDeltaMovement(rotated.multiply(finalMult,1.0,finalMult))
    }

    private var breaking = false
    // Do I want to implement this for server-only players as well? I think I do but I'm tired...
    fun emulateLeftClick() {
        val p = player ?: return
        val gameMode = mc.gameMode ?: return
        val hit = mc.hitResult ?: return

        when (hit.type) {
            HitResult.Type.ENTITY -> {
                val entity = (hit as EntityHitResult).entity
                gameMode.attack(p, entity)
                p.swing(InteractionHand.MAIN_HAND)
            }

            HitResult.Type.BLOCK -> {
                val blockHit = hit as BlockHitResult
                if (breaking) {
                    if (gameMode.destroyStage != 10) {
                        gameMode.continueDestroyBlock(blockHit.blockPos, blockHit.direction)
                    }
                    /*
                    else {
                        gameMode.destroyBlock(blockHit.blockPos)
                        breaking = false
                    }
                    */
                } else {
                    gameMode.startDestroyBlock(blockHit.blockPos, blockHit.direction)
                    breaking = true
                }
                p.swing(InteractionHand.MAIN_HAND)
            }

            HitResult.Type.MISS -> {
                if (breaking) {
                    gameMode.stopDestroyBlock()
                    breaking = false
                }
                p.swing(InteractionHand.MAIN_HAND)
            }
        }
    }

    private var using = false
    fun emulateRightClick() {
        val p = player ?: return
        val gameMode = mc.gameMode ?: return
        val hit = mc.hitResult ?: return

        val stack = p.getItemInHand(InteractionHand.MAIN_HAND)

        when (hit.type) {
            HitResult.Type.ENTITY -> {
                val entity = (hit as EntityHitResult).entity

                // Interact with entity
                // Main hand first
                //gameMode.useItem(p, InteractionHand.MAIN_HAND)
                gameMode.interact(p, entity, InteractionHand.MAIN_HAND)

                // Swing hand for animation
                p.swing(InteractionHand.MAIN_HAND)
            }

            HitResult.Type.BLOCK -> {
                val blockHit = hit as BlockHitResult
                val pos = blockHit.blockPos
                val direction = blockHit.direction

                // Place or use item on block
                gameMode.useItemOn(p, InteractionHand.MAIN_HAND, blockHit)

                // Optional: for continuous use (like eating or charging bow)
                if (p.isUsingItem) {
                    using = true
                } else if (using) {
                    gameMode.releaseUsingItem(p)
                    using = false
                }

                p.swing(InteractionHand.MAIN_HAND)
            }

            HitResult.Type.MISS -> {
                // Use item in air
                gameMode.useItem(p, InteractionHand.MAIN_HAND)
                p.swing(InteractionHand.MAIN_HAND)
            }
        }
    }

    fun onClientTick() {
        val p = player ?: return
        val e = PlayerControlData.myEntry
        // If shouldMoveForwardBackward is 0 and we set p.zza it may conflict, check needed, I think...
        if (e.shouldMoveForwardBackward != 0f || e.shouldMoveLeftRight != 0f) {
            // p.input.forwardImpulse = e.shouldMoveForwardBackward
            customMovement(p,
                Vec3(
                    e.shouldMoveLeftRight.toDouble(),
                    0.0,
                    e.shouldMoveForwardBackward.toDouble()))
        }
        if (e.shouldLook) {
            p.yRot = e.shouldLookLeftRight
            p.xRot = e.shouldLookUpDown
        }
        if (e.shouldJump && p.onGround()) {
            player?.jumpFromGround()
        }
        if (e.shouldSprint && p.canSprint()) {
            if (!p.isSprinting)
                p.isSprinting = true
        }
        if (e.shouldSneak) {
            // p.isCrouching
            if (!p.input.shiftKeyDown)
                p.input.shiftKeyDown = true
            if (!p.isShiftKeyDown) {
                p.isShiftKeyDown = true
            }
            p.pose = Pose.CROUCHING
        }

        if (e.shouldAttack) {
            // p.swing is local
            if (e.shouldAttackPeriod == -1) {
                // p.swinging = true
                emulateLeftClick() // Momentary
                e.shouldAttack = false
            } else if (e.shouldAttackPeriod == 0) {
                // p.swinging = true
                emulateLeftClick() // Momentary
            } else if ((p.level().gameTime % e.shouldAttackPeriod) == 0L) {
                // p.swinging = true
                emulateLeftClick() // Momentary
            }
        }

        if (e.shouldUse) {
            if (e.shouldUsePeriod == -1) {
                // TODO: do I really want to do it this way?
                // Hmm...
                emulateRightClick() // Hmm.... I don't think I can use .isDown because it would conflict...
                e.shouldUse = false
            } else if (e.shouldUsePeriod == 0) {
                emulateRightClick() // Continuous
            } else if ((p.level().gameTime % e.shouldUsePeriod) == 0L) {
                emulateRightClick() // Periodic
            }
        }


        if (e.shouldHotbarSlot != -1) {
            p.inventory.selected = e.shouldHotbarSlot - 1
            e.shouldHotbarSlot = -1 // reset, no reason to be persistent?
        }

        if (e.shouldSwapHands) {
            // PlayerActionPacketAPI.swapHands(p)
            val tempItemStack = p.getItemInHand(InteractionHand.MAIN_HAND)
            p.setItemInHand(InteractionHand.MAIN_HAND, p.getItemInHand(InteractionHand.OFF_HAND))
            p.setItemInHand(InteractionHand.OFF_HAND, tempItemStack)
            e.shouldSwapHands = false
            //data.setDirty()
        }

        if (e.shouldDrop) {
            p.drop(e.shouldDropStack)
            // p.updateOptions()
            e.shouldDrop = false
            //data.setDirty()
        }
        // Todo: smarter way to mark dirty?
        // data.setDirty()
    }

    fun onServerTick(server: MinecraftServer) {
        val data = PlayerControlData.get(server)

        data.players.forEach { (uuid, e) ->
            val p = server.playerList.getPlayer(uuid)
            if (p != null) {
                // If shouldMoveForwardBackward is 0 and we set p.zza it may conflict, check needed, I think...
                if (e.shouldMoveForwardBackward != 0f) {
                    p.zza = e.shouldMoveForwardBackward   // forwar hehe pizza
                }
                if (e.shouldMoveLeftRight != 0f) {
                    p.xxa = e.shouldMoveLeftRight   // side
                }
                if (e.shouldLookUpDown != 0f) {
                    p.yRot = e.shouldLookUpDown
                }
                if (e.shouldLookLeftRight != 0f) {
                    p.xRot = e.shouldLookLeftRight
                }
                if (e.shouldJump) {
                    // If player is of type REAL and not a bot, we send a packet, otherwise we manipulate directly
                    /*
                    p.jumpFromGround()
                    p.addDeltaMovement(
                        Vec3(
                            0.0, 0.42 * p.jumpBoostPower, 0.0
                        )
                    )
                    */
                    // p.jumpBoostPower
                    // shouldJump = false // do I want it to be continuous if no other inputs?
                }
                if (e.shouldSprint && p.canSprint()) {
                    p.isSprinting = true
                }
                if (e.shouldSneak) {
                    p.isShiftKeyDown = true
                }

                if (e.shouldAttack) {
                    // p.swing is local
                    if (e.shouldAttackPeriod == -1) {

                        // TODO: do I really want to do it this way?
                        // Hmm...
                        p.swing(p.usedItemHand) // Momentary
                        e.shouldAttack = false
                    } else if (e.shouldAttackPeriod == 0) {
                        p.swing(p.usedItemHand) // Continuous
                    } else if ((p.level().gameTime % e.shouldAttackPeriod) == 0L) {
                        p.swing(p.usedItemHand) // Periodic
                    }
                }

                if (e.shouldUse) {
                    if (e.shouldUsePeriod == -1) {
                        // TODO: do I really want to do it this way?
                        // Hmm...
                        //mc.options.keyUse.isDown = true // Hmm.... I don't think I can use .isDown because it would conflict...
                        e.shouldUse = false
                    } else if (e.shouldUsePeriod == 0) {
                        //p.swing(player!!.usedItemHand) // Continuous
                    } else if ((level!!.gameTime % e.shouldUsePeriod) == 0L) {
                        //p.swing(player!!.usedItemHand) // Periodic
                    }
                }


                if (e.shouldHotbarSlot != -1) {
                    p.inventory.selected = e.shouldHotbarSlot + 1
                    e.shouldHotbarSlot = -1 // reset, no reason to be persistent?
                }

                if (e.shouldSwapHands) {
                    // PlayerActionPacketAPI.swapHands(p)
                    val tempItemStack = p.getItemInHand(InteractionHand.MAIN_HAND)
                    p.setItemInHand(InteractionHand.MAIN_HAND, p.getItemInHand(InteractionHand.OFF_HAND))
                    p.setItemInHand(InteractionHand.OFF_HAND, tempItemStack)
                    e.shouldSwapHands = false
                    //data.setDirty()
                }

                if (e.shouldDrop) {
                    p.drop(e.shouldDropStack)
                    // p.updateOptions()
                    e.shouldDrop = false
                    //data.setDirty()
                }
            }
        }
        data.setDirty()
        // Todo: smarter way to mark dirty?
        // data.setDirty()
    }
}