package hauveli.hexagony.common.bilocation

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

class FreeCameraEntity(entityType: EntityType<*>?, level: Level) : Entity(entityType, level) {

    init {
        noPhysics = true
        isNoGravity = true
    }

    override fun defineSynchedData() {}
    override fun readAdditionalSaveData(pCompound: CompoundTag) {}
    override fun addAdditionalSaveData(pCompound: CompoundTag) {}


    companion object {
        var freeCam: LocalPlayer? = null
        var active = false

        fun detachCamera(client: Minecraft) {
            if (active) return

            val player = client.player ?: return
            val level = client.level ?: return

            val cam = LocalPlayer(client, level, player.connection, player.stats, player.recipeBook, false, false)

            cam.copyPosition(player)
            cam.yRot = player.yRot
            cam.xRot = player.xRot
            cam.noPhysics = true

            freeCam = cam
            client.setCameraEntity(cam)
            active = true
        }

        fun updateFreeCam() {
            if (!active) return
            val cam = freeCam ?: return
            val client = Minecraft.getInstance()

            val speed = 0.5f

            if (client.options.keyUp.isDown) cam.moveRelative(speed, Vec3(0.0, 0.0, 1.0))
            if (client.options.keyDown.isDown) cam.moveRelative(speed, Vec3(0.0, 0.0, -1.0))
            if (client.options.keyLeft.isDown) cam.moveRelative(speed, Vec3(1.0, 0.0, 0.0))
            if (client.options.keyRight.isDown) cam.moveRelative(speed, Vec3(-1.0, 0.0, 0.0))

            cam.move(MoverType.SELF, cam.deltaMovement)
            cam.setDeltaMovement(Vec3.ZERO)
        }

        fun reattachCamera(client: Minecraft) {
            if (!active) return

            val player = client.player ?: return
            client.setCameraEntity(player)

            freeCam = null
            active = false
        }
    }
}