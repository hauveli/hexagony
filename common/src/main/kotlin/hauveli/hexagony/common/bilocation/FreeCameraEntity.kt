package hauveli.hexagony.common.bilocation

import at.petrak.hexcasting.common.lib.HexAttributes
import hauveli.hexagony.mind_anchor.MindAnchorManager
import hauveli.hexagony.mind_anchor.MindAnchorManager.getPosition
import net.minecraft.client.Camera
import net.minecraft.client.CameraType
import net.minecraft.client.ClientRecipeBook
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.player.Input
import net.minecraft.client.player.KeyboardInput
import net.minecraft.client.player.LocalPlayer
import net.minecraft.nbt.CompoundTag
import net.minecraft.stats.StatsCounter
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class FreeCameraEntity(minecraft: Minecraft) : LocalPlayer(
    minecraft,
    minecraft.level,
    DummyClientPlayNetworkHandler(minecraft),
    StatsCounter(),
    ClientRecipeBook(),
    false,
    false
) {

    init {
        noPhysics = true
        isNoGravity = true
        // isInvisibleTo()
        // isInvisible = true
        abilities.invulnerable = true // unneeded? idk...
        abilities.mayfly = true
        abilities.flying = true
        // isSpectator = true // not needed if I can set invuln+flying+invisible?
    }


    override fun tick() {
        super.tick()
        /*
        xo = x
        yo = y
        zo = z

        xOld = x
        yOld = y
        zOld = z
        */
        // super.tick()

        // movement logic...
    }

    // override fun defineSynchedData() {}
    override fun readAdditionalSaveData(pCompound: CompoundTag) {}
    override fun addAdditionalSaveData(pCompound: CompoundTag) {}

    companion object {
        var originalPlayer: LocalPlayer? = null
        var freeCam: FreeCameraEntity? = null
        var lastMouseX = 0.0
        var lastMouseY = 0.0
        var backupInput: Input? = null
        var active = false

        fun moveTowardsBodyIfNeeded() {
            val freeCamera = freeCam ?: return
            val player = originalPlayer ?: return
            val diffPlayer = player.position().subtract(freeCamera.position())
            val anchor = MindAnchorManager.localPos
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
            val mult = (1 - ambit / target.lengthSqr())
            freeCamera.setDeltaMovement(target.x * mult, target.y * mult, target.z * mult)
            freeCamera.move(MoverType.SELF, freeCamera.deltaMovement)
        }

        fun detachCamera(client: Minecraft) {
            val player = client.player ?: return

            val freeCamera = FreeCameraEntity(client)
            freeCamera.xRot = player.xRot
            freeCamera.yRot = player.yRot
            freeCamera.setPos(player.eyePosition)

            // set the freecam keybinds to be the keyboard inputs
            //freeCamera.input = KeyboardInput(client.options)
            // set the player inputs to be empty

            backupInput = player.input
            // freeCamera.input = KeyboardInput(client.options) // player.input
            freeCamera.input = backupInput
            // player.input = Input()


            client.level?.addFreshEntity(freeCamera)
            client.setCameraEntity(freeCamera)

            //client.options.hideGui = true
            originalPlayer = player
            freeCam = freeCamera
            active = true
        }

        fun updateFreeCam() {
            if (!active) return
            val freeCamera = freeCam ?: return
            val mc = Minecraft.getInstance()
            val input = freeCamera.input ?: return

            // Usually I would like to just overwrite this value but I do not know if doing so calls extra bogus each time...
            if (mc.options.cameraType != CameraType.THIRD_PERSON_BACK)
                mc.options.cameraType = CameraType.THIRD_PERSON_BACK

            var speed = 0.5

            var upDown = 0.0
            var forward = 0.0
            var strafe = 0.0


            val sensitivity = mc.options.sensitivity().get() * 0.6f + 0.2f
            val sensMult = sensitivity * sensitivity * sensitivity * 2.0f
            val inverted = if (mc.options.invertYMouse().get()) -1 else 1

            // Dude come on mojang really? xpos isnt for xrot but for yrot? man... what is this naming scheme...
            val deltaX = mc.mouseHandler.xpos() - lastMouseX
            val deltaY = mc.mouseHandler.ypos() - lastMouseY
            lastMouseX = mc.mouseHandler.xpos()
            lastMouseY = mc.mouseHandler.ypos()


            if (input.up) forward += 1
            if (input.down) forward -= 1
            if (input.left) strafe += 1
            if (input.right) strafe -= 1
            if (input.jumping) upDown += 1
            if (input.shiftKeyDown) upDown -= 1

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

            val dx = (-sin(yawRad) * cos(pitchRad)) * forward * speed + cos(yawRad) * strafe * speed
            val dy = (-sin(pitchRad)) * forward * speed + upDown * speed
            val dz = (cos(yawRad) * cos(pitchRad)) * forward * speed + sin(yawRad) * strafe * speed


            freeCamera.setOldPosAndRot()
            freeCamera.setDeltaMovement(dx, dy, dz)
            freeCamera.move(MoverType.SELF, freeCamera.deltaMovement)

            moveTowardsBodyIfNeeded()
            //val camera = mc.gameRenderer.mainCamera as CameraExtension
            //camera.`hexagony$bilocationSetCameraPosition`(freeCamera.position())
        }

        fun reattachCamera(client: Minecraft) {
            if (!active) return
            val player = originalPlayer ?: return
            client.setCameraEntity(player)
            if (backupInput != null) {
                player.input = backupInput
            } else {
                throw Error("Player existed but had no input field!")
            }
            client.options.cameraType = CameraType.FIRST_PERSON

            // client.options.hideGui = false
            freeCam?.discard()
            freeCam = null
            active = false
        }
    }
}