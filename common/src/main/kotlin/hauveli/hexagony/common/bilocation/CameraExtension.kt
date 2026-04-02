package hauveli.hexagony.common.bilocation

import net.minecraft.world.phys.Vec3

interface CameraExtension {
    fun `hexagony$bilocationSetCameraPosition`(position: Vec3)
    fun `hexagony$bilocationSetCameraRotation`(rotY: Float, rotX: Float)
}