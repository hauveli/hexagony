package hauveli.hexagony.features.freecam

import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

class FreeCam {
    // the client and the server need to somehow know when to get info from the other. I could write complicated code for this, OR I could ask every 400 ticks

    object ServerSideData {

    }

    object ClientSideData {

        private fun createCamera() {
            if (camera != null)
                camera!!.discard()
            val mc = Minecraft.getInstance()

            camera = LocalPlayer(mc, mc.level, null, null, null, false, false) // your client-side camera entity
            mc.setCameraEntity(camera!!)
        }

        private fun discardCamera() {
            camera?.discard()
            camera = null
        }

        @JvmStatic
        fun position(): Vec3 {
            return Vec3(x,y,z)
        }

        @JvmStatic
        fun setPosition(vector: Vec3) {
            if(camera == null) return
            camera!!.x = vector.x
            y = vector.y
            z = vector.z
        }

        @JvmStatic
        fun turn(yaw: Double, pitch: Double) {
            yRot += yaw
            xRot += pitch
        }

        fun lookVector(pitch: Double, yaw: Double): Vec3 {
            val pitchRad = Math.toRadians(pitch)
            val yawRad = Math.toRadians(yaw)

            val x = cos(pitchRad) * sin(yawRad)
            val y = sin(pitchRad)
            val z = cos(pitchRad) * cos(yawRad)
            return Vec3(x,z,y)
        }

        const val MOVEMENT_PER_STEP = 0.2

        @JvmStatic
        fun keyToMovement(key: KeyMapping) {
            when (key.category) {
                KeyMapping.CATEGORY_MOVEMENT -> {
                    if (!key.isDown) return
                    // get look vector, then apply a vector with some magniture relative to that
                    val lookVec = lookVector(xRot, yRot)
                    when (key.name) {
                        "key.forward" -> {
                            setPosition(position().add(lookVec.scale(MOVEMENT_PER_STEP)))
                        }
                        "key.back" -> {
                            setPosition(position().add(lookVec.scale(-MOVEMENT_PER_STEP)))
                        }
                        "key.jump" -> {
                            setPosition(position().add(Vec3(0.0,MOVEMENT_PER_STEP,0.0)))
                        }
                        "key.shift" -> {
                            setPosition(position().add(Vec3(0.0,-MOVEMENT_PER_STEP,0.0)))
                        }
                        else -> {
                            return
                        }
                    }
                }

                else -> {
                    return
                }
            }
        }

        @JvmField
        var playerEntityInputsDisabled: Boolean = true

        @JvmStatic
        var yRot = 0.0
        @JvmStatic
        var xRot = 0.0
        var x = 0.0
        var y = 0.0
        var z = 0.0
        var camera: LocalPlayer? = null


    }
}