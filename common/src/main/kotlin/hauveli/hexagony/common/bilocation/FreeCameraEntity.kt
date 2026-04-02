package hauveli.hexagony.common.bilocation

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
        var yaw = 0f
        var pitch = 0f
        var active = false

        fun detachCamera(client: Minecraft) {
            val player = client.player ?: return

            val freeCamera = FreeCameraEntity(client)
            freeCamera.xRot = player.xRot
            freeCamera.yRot = player.yRot
            freeCamera.setPos(player.position())

            // set the freecam keybinds to be the keyboard inputs
            //freeCamera.input = KeyboardInput(client.options)
            // set the player inputs to be empty

            freeCamera.input = player.input
            // player.input = Input()


            // client.level?.addFreshEntity(freeCamera)
            // client.setCameraEntity(freeCamera)

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
            //if (mc.options.cameraType != CameraType.THIRD_PERSON_BACK)
                // mc.options.setCameraType(CameraType.THIRD_PERSON_BACK)

            var speed = 0.5

            var upDown = 0.0
            var forward = 0.0
            var strafe = 0.0


            val sensitivity = mc.options.sensitivity().get() * 0.6f + 0.2f
            val sensMult = sensitivity * sensitivity * sensitivity * 2.0f
            val inverted = if (mc.options.invertYMouse().get()) -1 else 1

            // Dude come on mojang really? xpos isnt for xrot but for yrot? man... what is this naming scheme...
            var deltaX = mc.mouseHandler.xpos() - lastMouseX
            var deltaY = mc.mouseHandler.ypos() - lastMouseY
            if (abs(deltaX) > 200)
                deltaX = 0.0
            if (abs(deltaY) > 200)
                deltaY = 0.0
            lastMouseX = mc.mouseHandler.xpos()
            lastMouseY = mc.mouseHandler.ypos()


            if (input.up) forward += 1
            if (input.down) forward -= 1
            if (input.left) strafe += 1
            if (input.right) strafe -= 1
            if (input.jumping) upDown += 1
            if (input.shiftKeyDown) upDown -= 1

            val yaw = freeCamera.yRot + deltaX * sensMult
            val pitch = (freeCamera.xRot + deltaY * sensMult * inverted).coerceIn(-90.0, 90.0)
            val yawRad = Math.toRadians(yaw)
            val pitchRad = Math.toRadians(pitch)

            freeCamera.setRot(
                yaw.toFloat(),
                pitch.toFloat()
            )

            val dx = (-sin(yawRad) * cos(pitchRad)) * forward * speed + cos(yawRad) * strafe * speed
            val dy = (-sin(pitchRad)) * forward * speed + upDown * speed
            val dz = (cos(yawRad) * cos(pitchRad)) * forward * speed + sin(yawRad) * strafe * speed

            freeCamera.setDeltaMovement(dx, dy, dz)
            freeCamera.move(MoverType.SELF, freeCamera.deltaMovement)

            val camera = mc.gameRenderer.mainCamera as CameraExtension
            camera.`hexagony$bilocationSetCameraPosition`(freeCamera.position())
            camera.`hexagony$bilocationSetCameraRotation`(freeCamera.yRot, freeCamera.xRot)

            /*
            val player = originalPlayer
            if (player == null) return
            mc.entityRenderDispatcher.render(
                player,
                player.x,
                player.y,
                player.z,
                player.yRot,
                player.xRot,
                player.pose,


            )
            */
        }

        fun reattachCamera(client: Minecraft) {
            if (!active) return
            val player = originalPlayer ?: return
            client.setCameraEntity(player)
            if (player.input != null) {
                player.input = KeyboardInput(client.options)
            } else {
                throw Error("Player existed but had no input field!")
            }

            freeCam?.discard()
            freeCam = null
            active = false
        }
    }
}