package hauveli.hexagony.features.freecam

import at.petrak.hexcasting.common.lib.HexAttributes
import com.mojang.authlib.GameProfile
import hauveli.hexagony.Hexagony
import hauveli.hexagony.Hexagony.MINECRAFT
import hauveli.hexagony.Hexagony.id
import hauveli.hexagony.registry.HexagonyMobEffects
import net.minecraft.client.CameraType
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.player.Input
import net.minecraft.client.player.KeyboardInput
import net.minecraft.client.player.LocalPlayer
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Mth
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.Pose
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.Scoreboard
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin


// I had an implementation working but it was awful so I will be using MinecraftFreecam/Freecam as a reference.
// Thank you very kindly to all the people who have worked on MinecraftFreecam, your project has been a tremendous help, would not
// have given AbstractClientPlayer a try if I didn't see it used here. LocalPLayer was so much worse to try to use.
// https://github.com/MinecraftFreecam/Freecam/blob/main/common/src/main/java/net/xolt/freecam/util/FreeCamera.java#L30
class FreeCameraEntity : AbstractClientPlayer (
    MINECRAFT!!.level, GameProfile(UUID.randomUUID(), "FreeCamera")
) {

    // todo: static UUID? low priority
    fun init(id: Int) {
        setId(id)
        pose = Pose.SWIMMING
        abilities.flying = true
        input = KeyboardInput(MINECRAFT!!.options)
    }

    override fun getScoreboard(): Scoreboard? {
        return null
    }

    override fun isSpectator(): Boolean {
        return true
    }

    override fun isCreative(): Boolean {
        return false
    }

    // note to self: yes i do need this to prevent it from fucking up
    // override fun defineSynchedData() {}
    override fun readAdditionalSaveData(pCompound: CompoundTag) {}
    override fun addAdditionalSaveData(pCompound: CompoundTag) {}


    companion object {
        private val FREECAM_SHADER = id("shaders/post/freecam.json")


        var input: Input? = null
        var originalPlayer: LocalPlayer? = null
        var freeCam: FreeCameraEntity? = null
        var lastMouseX = 0.0
        var lastMouseY = 0.0
        var backupInput: Input? = null
        var active = false

        var smoothCameraSettingStorage: Boolean = false


        // I use this stuff mostly so I can play a neat animation when it starts/expires
        @JvmField
        var returningAnimationActive: Boolean = false
        var returningFromEyePosDistance: Double? = null
        var distanceToPlayer: Double = 0.0 // this can be whatever because it only affects the shader
        var distanceToPlayerRelativeToAmbit: Float = 0f // this can be whatever between 0 and 1 because it only affects the shader

        // todo: make a simpler version of this so that it is easy to extend
        // ex: whatever(attribute, positionGetter, cameraPosition) -> null if in ambit, relative vector position if not (which I can then just normalize
        // this would let me just compare the lengths of each, pick the lowest and be done
        // (or if any were null, skip all the rest)
        // bonus points if I make note of whichever evaluated to null most recently, then evaluate that one first on the next check
        // also todo: maybe get ambit attributes in a smarter way somehow, I could just store them somewhere most of the time...
        // I only really need to be mindful of if they update...
        fun moveTowardsAmbitIfNeeded(dt: Float) {
            val freeCamera = freeCam ?: return
            val player = originalPlayer ?: return
            val diffPlayer = player.eyePosition.subtract(freeCamera.position())
            val anchor: Vec3? = null // MindAnchorManager.localPos
            val diffAnchor: Vec3
            if (anchor != null) {
                diffAnchor = anchor.subtract(freeCamera.position())
            } else {
                diffAnchor = diffPlayer
            }
            // TODO: let it move towards sentinel?
            // TODO: make it move towards mind anchor?
            // probably make it move to mind anchor actually...
            val diffPlayerSqr = diffPlayer.lengthSqr()
            val diffAnchorSqr = diffAnchor.lengthSqr()
            val target: Vec3
            if (diffAnchorSqr <= diffPlayerSqr) {
                target = diffAnchor
            } else {
                target = diffPlayer
            }

            val ambitAttr = player.getAttribute(HexAttributes.AMBIT_RADIUS) ?: return
            //val ambitSentAttr = player.getAttribute(HexAttributes.SENTINEL_RADIUS) ?: return
            val ambit = ambitAttr.value * ambitAttr.value
            //val sentAmbit = ambitSentAttr.value * ambitSentAttr.value
            if (target.lengthSqr() < ambit) return // within that ambit
            //if (diffSqr < sentAmbit) return
            val mult = (1 - ambit / target.lengthSqr()) * dt
            // todo: this determines how hard the player bounces off of ambit (be really gentle...)
            freeCamera.deltaMovement = target.scale(mult + min(freeCamera.deltaMovement.lengthSqr(), 0.0025))  // bounce back as hard as I ran into it
            freeCamera.move(MoverType.SELF, freeCamera.deltaMovement)
        }

        // moves the freecam to the player and also coerces the lookdir to be the same
        fun moveTowardsPlayer(dt: Float) {
            if (!returningAnimationActive) return
            val freeCamera = freeCam ?: return
            val player = originalPlayer ?: return
            if (returningFromEyePosDistance == null) {
                returningFromEyePosDistance = freeCamera.eyePosition.subtract(player.eyePosition).length()
            }
            // ensure noclip is active!!!!!!!!!!!! oops!!!!
            freeCamera.noPhysics = true
            // this is just to get it to move to the player at a decent speed, not so that it always takes the same amount of time.
            val diffPlayer = player.eyePosition.subtract(freeCamera.position())
            val diffPlayerLength = diffPlayer.length()
            val dist = abs((diffPlayerLength - dt) / returningFromEyePosDistance!!)
            val mult = 0.05 * abs(1 - dist) // take 10 times longer than otherwise (still very fast)
            freeCamera.setDeltaMovement(diffPlayer.x * mult, diffPlayer.y * mult, diffPlayer.z * mult)
            freeCamera.move(MoverType.SELF, freeCamera.deltaMovement)

            // the rotation of the camera scales to distance, which is controlled entirely via position

            // hmm I think I need ot use a vector or it'll look like shit to interpolate without some headachey conditions
            // freeCamera.xRot = player.xRot * (1 - ambitLerp) + freeCamera.xRot * ambitLerp
            // freeCamera.yRot = player.yRot * (1 - ambitLerp) + freeCamera.yRot * ambitLerp

            // wow this works well... similar behavior to blender if you set a target and set follow strength to 1 or whatever it's called.
            // todo: loosen it to be not exactly 1 strength but more like a lerp from 0 to 1 based on distance?
            // I think that would look good...
            // so I want to lerp between uhhh... current eye position + current lookdir and player eye pos and player lookdir?
            val lerpedLookTarget = Vec3.ZERO.add(
                freeCamera.eyePosition.add(freeCamera.lookAngle).scale(dist)
            ).add(
                player.eyePosition.add(player.lookAngle).scale(1 - dist)
            )
            freeCamera.lookAt(EntityAnchorArgument.Anchor.FEET, lerpedLookTarget)
            if (diffPlayerLength <= 0.05) {
                reattachCamera()
                returningAnimationActive = false
                returningFromEyePosDistance = null
            }
        }

        fun detachCamera() {
            // TODO: do the shader thing HERE
            // Shader should likely be set from within detach, and unset from reattach
            val player = MINECRAFT!!.player ?: return

            val freeCamera = FreeCameraEntity()
            freeCamera.pose = Pose.SWIMMING // small hitbox?
            freeCamera.xRot = player.xRot
            freeCamera.yRot = player.yRot
            freeCamera.setPos(player.eyePosition)

            // set the freecam keybinds to be the keyboard inputs
            //freeCamera.input = KeyboardInput(client.options)
            // set the player inputs to be empty

            // player.input = Input() // empty the input so it doesn't keep jumping
            player.input.jumping = false
            player.input.up = false
            player.input.left = false
            player.input.down = false
            player.input.right = false
            player.input.forwardImpulse = 0f
            player.input.leftImpulse = 0f

            player.setJumping(false)

            // how do I make the player not move so much oh my god........
            // player.tryResetCurrentImpulseContext()
            // player.resetCurrentImpulseContext()
            // player.setDeltaMovement(0.0,0.0,0.0)
            // player.deltaMovement = Vec3.ZERO
            // player.moveTo(player.position())
            // move is relative
            // player.move(MoverType.SELF, Vec3.ZERO)

            // close the hexcasting grid, too...

            backupInput = player.input
            // freeCamera.input = KeyboardInput(client.options) // player.input
            input = backupInput
            // player.input = Input()
            // client.level?.addFreshEntity(freeCamera)

            MINECRAFT.setCameraEntity(freeCamera)

            //client.options.hideGui = true
            originalPlayer = player
            freeCam = freeCamera
            active = true

            smoothCameraSettingStorage = MINECRAFT.options.smoothCamera
            MINECRAFT.options.smoothCamera = true

            ShaderRenderer.setEffect(FREECAM_SHADER)


            // todo: play /playsound minecraft:ambient.basalt_deltas.loop
        }


        // todo: distant future: somehow chunkload around a thing?
        // todo: make it floaty like spectator mode
        // todo: add a nice shader
        // todo: make it feel good to control and not so stiff
        fun updateFreeCam(dt: Float) {
            // janky bugfix
            if (!active) return
            val freeCamera = freeCam ?: return
            val input = input ?: return


            // todo: can I just put this in detach instead?
            // Usually I would like to just overwrite this value but I do not know if doing so calls extra bogus each time...
            if (MINECRAFT!!.options.cameraType != CameraType.THIRD_PERSON_BACK)
                MINECRAFT.options.cameraType = CameraType.THIRD_PERSON_BACK

            val speed = 0.01 * dt // todo: also tweak this value if modifying how floaty the movement is

            var upDown = 0.0
            var forward = 0.0
            var strafe = 0.0


            /*
            val sensitivity = MINECRAFT.options.sensitivity().get() * 0.6f + 0.2f
            val sensMult = sensitivity * sensitivity * sensitivity * 2.0f
            val inverted = if (MINECRAFT.options.invertYMouse().get()) -1 else 1

            // Dude come on mojang really? xpos isnt for xrot but for yrot? man... what is this naming scheme...
            val deltaX = MINECRAFT.mouseHandler.xpos() - lastMouseX
            val deltaY = MINECRAFT.mouseHandler.ypos() - lastMouseY
            lastMouseX = MINECRAFT.mouseHandler.xpos()
            lastMouseY = MINECRAFT.mouseHandler.ypos()
             */

            if (input.up) forward += 1
            if (input.down) forward -= 1
            if (input.left) strafe += 1
            if (input.right) strafe -= 1
            if (input.jumping) upDown += 1
            if (input.shiftKeyDown) upDown -= 1


            val normalizedInputPlusDelta = Vec3(forward, upDown, strafe).normalize() // .add(freeCamera.deltaMovement)

            val yaw = freeCamera.yRot
            val pitch = (freeCamera.xRot.toDouble()).coerceIn(-90.0, 90.0)
            val yawRad = Math.toRadians(yaw.toDouble())
            val pitchRad = Math.toRadians(pitch)

            /*
            freeCamera.setRot(
                yaw.toFloat(),
                pitch.toFloat()
            )
            */

            val dx = (-sin(yawRad) * cos(pitchRad)) * normalizedInputPlusDelta.x * speed +
                    cos(yawRad) * normalizedInputPlusDelta.z * speed
            val dy = (-sin(pitchRad)) * normalizedInputPlusDelta.x * speed +
                    normalizedInputPlusDelta.y * speed
            val dz = (cos(yawRad) * cos(pitchRad)) * normalizedInputPlusDelta.x * speed +
                    sin(yawRad) * normalizedInputPlusDelta.z * speed

            freeCamera.setOldPosAndRot()
            val newDM = Vec3(dx, dy, dz)
            //freeCamera.setDeltaMovement(dx, dy, dz)
            freeCamera.deltaMovement =  newDM.add(freeCamera.deltaMovement.scale(0.99)) // todo: note to self: this makes it more or less floaty, change this for the mind graft
            freeCamera.move(MoverType.SELF, freeCamera.deltaMovement)

            // hmm.....
            distanceToPlayer = originalPlayer!!.eyePosition.subtract(freeCamera.eyePosition).length()

            val ambitAttr = originalPlayer!!.getAttribute(HexAttributes.AMBIT_RADIUS)!!
            //val ambitSentAttr = player.getAttribute(HexAttributes.SENTINEL_RADIUS) ?: return
            val ambit = ambitAttr.value
            distanceToPlayerRelativeToAmbit = (distanceToPlayer / ambit).toFloat() + 0.1f
            // Hexagony.LOGGER.info("ambit thing: {}", distanceToPlayerRelativeToAmbit)

            moveTowardsAmbitIfNeeded(dt)
            moveTowardsPlayer(dt)
            //val camera = mc.gameRenderer.mainCamera as CameraExtension
            //camera.`hexagony$bilocationSetCameraPosition`(freeCamera.position())
        }

        fun reattachCamera() {
            if (!active) return
            val player = originalPlayer ?: return
            // freecam must exist here or this wont make sense I think...? what if the server shuts down? todo: figure that out
            freeCam!!.pose = player.pose
            MINECRAFT!!.setCameraEntity(player)
            if (backupInput != null) {
                player.input = backupInput
            } else {
                throw Error("${Hexagony.MODID}: Player existed but had no input field!") // I sort of doubt this can happen but I am keeping this check here just in case so I can figure out how it can happen
            }
            MINECRAFT.options.cameraType = CameraType.FIRST_PERSON
            MINECRAFT.options.smoothCamera = smoothCameraSettingStorage

            // client.options.hideGui = false
            freeCam?.discard()
            freeCam = null
            active = false


            ShaderRenderer.setEffect(null)
            // todo: STOP /playsound minecraft:ambient.basalt_deltas.loop
        }

        // just in case in the future I realize I need to do extra stuff, I've got this in its own method.
        fun onLeave() {
            reattachCamera()
            // set shader for some reason doesn't work ? hmm...
            ShaderRenderer.setEffect(null)
        }

        fun distanceToPlayer(): Float {
            // return min(distanceToPlayerRelativeToAmbit, 1f) // .length() > 0 => distanceToPlayerRelativeToAmbit >= 0.1f
            return distanceToPlayerRelativeToAmbit / 1.5f // if divisor is 2, bounded between 0.05 and 0.55 at most, this value is also already dependent on dt so no need to pass dt
        }

        fun durationLeftRelativeToFiveSeconds(dt: Float): Float {
            val dissociated = originalPlayer!!.getEffect(HexagonyMobEffects.FREECAM.holder()) ?: return 0.5f // max it out if duration expired
            val durationLeft = dissociated.duration
            val sixSeconds = 6 * 20f
            if (durationLeft > sixSeconds) return 0f
            // sine wave to make it oscillate once timer is about to run out
            // no, I don't want the square, I want something else... shift it up by 1, divide by two=
            // sin(1 * 2pi) = 0 so it should start from zero?
            // sin(1.5 pi) = -1, 1-1 = 0
            val sinVal = 1 + sin((1 - (durationLeft - dt) / ( sixSeconds )) * Mth.TWO_PI - Mth.HALF_PI) // I need to square this I think? I'm unsure how I want it to flash...
            return sinVal / 8 // less intense should be fine.
        }

        fun timeSinceStartedSmoothing(dt: Float): Float {
            // the player might be silly so I want to get the remaining duration and return if it has expired (or is about to expire?)
            val dissociated = originalPlayer!!.getEffect(HexagonyMobEffects.FREECAM.holder()) ?: return 0.0f // max it out if duration expired
            val deltaTickCount = freeCam!!.tickCount + dt
            return min(deltaTickCount / 30f, 1f) // smooth across 1.5 seconds
        }
    }
}
