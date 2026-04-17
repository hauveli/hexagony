package hauveli.hexagony.common.control

// import at.petrak.hexcasting.ktxt.UseOnContext
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.events.common.TickEvent
import hauveli.hexagony.common.bilocation.FreeCameraEntity
import hauveli.hexagony.common.mind_anchor.MindAnchorData
import hauveli.hexagony.common.mind_anchor.MindAnchorManager
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random


object ControlLocalPlayerAPI {
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
            e.isDetached = true
            FreeCameraEntity.detachCamera(mc)
        }

        fun reattach(bool: Boolean) {
            e.isDetached = false
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

    val SPEED_MULT = 0.45

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

        // Maximum length of rotated must be 4 or 5 ish? 3 works well enough for the feel of it
        val finalMult = min(sneakMult * sprintMult * groundAccel * viscosityMult, 3.0)
        val finalVec = rotated.multiply(finalMult,1.0,finalMult)
        entity.addDeltaMovement(finalVec)
        // entity.move(MoverType.SELF, finalVec)
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

    // TODO: specify which hand to use in argument
    private var using = false
    fun emulateRightClick(hand: InteractionHand) {
        val p = player ?: return
        val lp = mc.player ?: return
        val gameMode = mc.gameMode ?: return
        val hit = mc.hitResult ?: return
        val server = p.server
        // val serverPlayer = server?.playerList?.getPlayer(p.uuid)

        // Todo: determine which hand I am using?
        // TODO: actually eat food, currently not working, and p.eat() consumes the stack instantly....

        // TODO: check how an item I'm holding is used?
        when (hit.type) {
            HitResult.Type.ENTITY -> {
                val entity = (hit as EntityHitResult).entity

                // Main hand first
                //gameMode.useItem(p, InteractionHand.MAIN_HAND)
                gameMode.interact(p, entity, hand)

                val stack = p.getItemInHand(hand)
                if (stack.isEdible) {
                    val l = level
                    if (l != null) {
                        lp.eat(l, stack) // when I figure it out, I think letting the player be forced to eat is ok
                    }
                } else {
                    p.swing(hand)
                }
            }

            HitResult.Type.BLOCK -> {
                val blockHit = hit as BlockHitResult
                val pos = blockHit.blockPos
                val direction = blockHit.direction

                // Place or use item on block
                gameMode.useItemOn(p, hand, blockHit)

                // Ok so this is needed for sure, I don't get how to make food work yet though....
                if (p.isUsingItem) {
                    using = true
                } else if (using) {
                    gameMode.releaseUsingItem(p)
                    using = false
                }

                val stack = p.getItemInHand(hand)
                if (stack.isEdible) {
                    val l = level
                    if (l != null) {
                        lp.eat(l, stack) // when I figure it out, I think letting the player be forced to eat is ok
                    }
                } else {
                    p.swing(hand)
                }
            }

            HitResult.Type.MISS -> {
                // Use item in air
                gameMode.useItem(p, hand)
                val stack = p.getItemInHand(hand)
                if (stack.isEdible) {
                    val l = level
                    if (l != null) {
                        lp.eat(l, stack) // when I figure it out, I think letting the player be forced to eat is ok
                    }
                } else {
                    p.swing(hand)
                }
            }
        }

    }

    fun onClientTick() {
        val p = player ?: return
        val e = PlayerControlData.getSelf()
        // This seems to have done it, finally...
        // Oh well, two additional boolean comparisons at most per tick shouldn't be too bad....
        if (e.isDetached) {
            if (!FreeCameraEntity.active) {
                FreeCameraEntity.Companion.detachCamera(Minecraft.getInstance())
            }

            // Fixes for detached player behaviour????
            // TODO: actually try to fix the freecamera to use an armor stand? I strongly suspect the issue is there...
            if (p.fallDistance > 3) {
                p.hurt(p.damageSources().fall(), (p.fallDistance-3) / 2)
            }
        }
        // no attributes in this version, TODO: 1.21.1 use generic.gravity, generic.safe_fall_distance, generic.fall_damage_multiplier and player.block_break_speed attributes.
        // If shouldMoveForwardBackward is 0 and we set p.zza it may conflict, check needed, I think...
        if (e.shouldMoveForwardBackward != 0f || e.shouldMoveLeftRight != 0f) {
            // p.input.forwardImpulse = e.shouldMoveForwardBackward
            // If the player is detached, p.zza and p.xxa work great...
            // But still only do things client-side....
            //p.zza = e.shouldMoveForwardBackward
            //p.xxa = e.shouldMoveLeftRight
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
            p.jumpFromGround()
        }
        if (e.shouldSprint && p.canSprint()) {
            if (!p.isSprinting) {
                p.isSprinting = true
            }
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
                emulateLeftClick() // Momentary
                e.shouldAttack = false
            } else if (e.shouldAttackPeriod == 0) {
                emulateLeftClick() // Momentary
            } else if ((p.level().gameTime % e.shouldAttackPeriod) == 0L) {
                emulateLeftClick() // Momentary
            }
        }

        if (e.shouldUse) {
            val hand = InteractionHand.MAIN_HAND
            if (e.shouldUsePeriod == -1) {
                // TODO: do I really want to do it this way?
                // Hmm...
                emulateRightClick(hand) // Hmm.... I don't think I can use .isDown because it would conflict...
                e.shouldUse = false
            } else if (e.shouldUsePeriod == 0) {
                emulateRightClick(hand) // Continuous
            } else if ((p.level().gameTime % e.shouldUsePeriod) == 0L) {
                emulateRightClick(hand) // Periodic
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
        // Todo: smarter way to mark dirty?
        // data.setDirty()
    }

    // Most of this, don't have to do every tick...

    // Todo: get PlayerControlData.get(server) and store is somewhere so it's more convenient and better
    // todo: store actual player in some sort of field? I don't want to call getPlayer multiple times each tick...

    // todo: check graftUUID to see if the player has a valid mind anchor, and if the player is grafted but has no valid anchor,
    // begin sapping the most recently stored media value from the anchor, and if that hits 0 before the anchor is found,
    // kill the player.

    // todo: when swapping player and homunculus, also swap active control settings like jumping, attacking etc
    // don't forget the hotbar slot either

    val connectedPlayers: MutableMap<UUID, Pair<ServerPlayer, PlayerControlEntry>> = mutableMapOf()

    var counter = 0
    var changed = false
    fun onServerTick(server: MinecraftServer) {
        counter++
        val currentTick = server.tickCount
        connectedPlayers.forEach { (uuid, pair) ->
            val p = pair.component1()
            val e = pair.component2()
            if (currentTick % 20 == 19) {
                println(p.toString())
                // This returns right away if no player matched
                // (if no player is in the MindAnchorManager.runtime list
                println("Subtracting!!")
                MindAnchorManager.perSecond(server,p)
                e.durationSeconds -= 1
                if (e.isDetached) {
                    // update position at least once a second...?
                    MindAnchorManager.getPosition(p)?.let {
                        MindAnchorManager.forwardPosToPlayer(p, it)
                    }
                }
                if (e.durationSeconds < 10L) {
                    if (e.durationSeconds <= 0L ) {
                        if (e.isFakePlayer) {
                            PlayerControlData.get(server).getOrCreate(p.uuid).detach(p)
                            changed = true // Only bother writing when duration has expired?
                        } else if (!e.isDetached
                            && MindAnchorData.get(server).getOrCreate(uuid).graftUUID
                            == PlayerControlData.get(server).getOrCreate(uuid).graftUUID) {
                            PlayerControlData.get(server).getOrCreate(p.uuid).detach(p)
                            changed = true // Only bother writing when duration has expired?
                        }
                        // TODO: figure out the anchor timing mechanics, then uncomment this
                    } else if (e.durationSeconds >= 2L) {
                        // I'm assuming player.position() is at feet...
                        p.serverLevel().addParticle(
                            ParticleTypes.DRAGON_BREATH,
                            p.position().x, p.position().y + p.bbHeight / 2, p.position().z,
                            (0.5 - Random.nextDouble()) * 3, (0.5 - Random.nextDouble()) * 3, (0.5 - Random.nextDouble()) * 3
                        )
                    }
                }
            }

            if (!p.tags.contains("FakePlayer")) return@forEach
            // If shouldMoveForwardBackward is 0 and we set p.zza it may conflict, check needed, I think...
            if (e.shouldSprint && p.canSprint()) {
                p.isSprinting = true
            } else { p.isSprinting = false }
            if (e.shouldSneak) {
                p.isShiftKeyDown = true
            } else { p.isShiftKeyDown = false }
            if (e.shouldMoveForwardBackward != 0f) {
                p.zza = e.shouldMoveForwardBackward
            }
            if (e.shouldMoveLeftRight != 0f) {
                p.xxa = e.shouldMoveLeftRight
            }
            if (e.shouldLookUpDown != 0f) {
                p.yRot = e.shouldLookUpDown
            }
            if (e.shouldLookLeftRight != 0f) {
                p.xRot = e.shouldLookLeftRight
            }
            if (e.shouldJump) {
                if (p.onGround()) {
                    p.jumpFromGround()
                } else if (p.isInWater || p.isInLava) {
                    p.setJumping(true)
                } else {
                    p.setJumping(false)
                }
            }

            if (e.shouldAttack) {
                // p.swing is local
                if (e.shouldAttackPeriod == -1) {

                    // TODO: do I really want to do it this way?
                    // Hmm...
                    p.swing(p.usedItemHand) // Momentary
                    e.shouldAttack = false
                    changed = true
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
                    changed = true
                } else if (e.shouldUsePeriod == 0) {
                    //p.swing(player!!.usedItemHand) // Continuous
                } else if ((level!!.gameTime % e.shouldUsePeriod) == 0L) {

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
            }


            if (e.shouldHotbarSlot != -1) {
                p.inventory.selected = e.shouldHotbarSlot + 1
                e.shouldHotbarSlot = -1 // reset, no reason to be persistent?
                changed = true
            }

            if (e.shouldSwapHands) {
                // PlayerActionPacketAPI.swapHands(p)
                val tempItemStack = p.getItemInHand(InteractionHand.MAIN_HAND)
                p.setItemInHand(InteractionHand.MAIN_HAND, p.getItemInHand(InteractionHand.OFF_HAND))
                p.setItemInHand(InteractionHand.OFF_HAND, tempItemStack)
                e.shouldSwapHands = false
                //data.setDirty()
                changed = true
            }

            if (e.shouldDrop) {
                p.drop(e.shouldDropStack)
                // p.updateOptions()
                e.shouldDrop = false
                //data.setDirty()
                changed = true
            }
        }
        if (changed) {
            val data = PlayerControlData.get(server)
            data.setDirty()
            changed = false
        }
        // Todo: smarter way to mark dirty?
        // data.setDirty()
    }

    fun initServer() {
        TickEvent.Server.SERVER_POST.register {
                server ->
            onServerTick(server)
        }
        PlayerEvent.PLAYER_JOIN.register { serverPlayer ->
            println("Adding player to thing")
            val server = serverPlayer.server
            connectedPlayers[serverPlayer.uuid] = Pair(serverPlayer, PlayerControlData.get(server).getOrCreate(serverPlayer.uuid))
            println(connectedPlayers)
        }
        PlayerEvent.PLAYER_QUIT.register { serverPlayer ->
            connectedPlayers.remove(serverPlayer.uuid)
        }
    }
}