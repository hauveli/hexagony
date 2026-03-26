package hauveli.hexagony.common.client

import com.mojang.authlib.GameProfile
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.Input
import net.minecraft.client.player.KeyboardInput
import net.minecraft.client.player.RemotePlayer
import net.minecraft.world.phys.Vec3
import java.util.*


class FreeCamAPI (minecraft: Minecraft) {
    private val mc = minecraft
    private var freeCam = false
    private var cameraPos: Vec3? = Vec3(0.0, 0.0, 0.0)
    private var cameraYaw = 0.0
    private var cameraPitch = 0.0

    fun preventInputs( prevent: Boolean) {
        if (prevent) {
            val player = mc.player
            if (player != null && player.input is KeyboardInput) {
                val input = Input()
                input.up = false
                input.down = false
                input.left = false
                input.right = false
                input.jumping = false
                input.shiftKeyDown = false
                input.forwardImpulse = 0f
                input.leftImpulse  = 0f
                player.input = input
            }
        }
    }

    fun detachCamera() {
        val world: ClientLevel? = mc.level
        val profile = GameProfile(UUID.randomUUID(), "CameraDummy")
        val dummy = RemotePlayer(world, profile)

        val cameraPos = Vec3(100.0, 80.0, -50.0)
        dummy.setPos(cameraPos.x, cameraPos.y, cameraPos.z)
        dummy.yRot = 180f // yaw
        dummy.xRot = 30f // pitch

        mc.setCameraEntity(dummy)
    }

    fun updateCamera() {
        val player = mc.player ?: return
        mc.cameraEntity = mc.cameraEntity

        val speed = if (mc.options.keyShift.isDown) 0.5 else 1
        val forward = mc.options.keyUp.isDown
        val back = mc.options.keyDown.isDown
        val left = mc.options.keyRight.isDown
        val right = mc.options.keyLeft.isDown
        val up = mc.options.keyJump.isDown
        val down = mc.options.keyShift.isDown

        // todo: figure out how to actually set the camera position???????????++

        player.noPhysics = true
    }
}