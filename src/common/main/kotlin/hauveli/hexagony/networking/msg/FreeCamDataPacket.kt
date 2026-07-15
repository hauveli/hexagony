package hauveli.hexagony.networking.msg

import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3


@JvmRecord
data class FreeCamDataPacketS2C(
    val junk: String
) : HexagonyMessageS2C {
    companion object : HexagonyMessageCompanion<FreeCamDataPacketS2C> {
        override val type = FreeCamDataPacketS2C::class.java
    }
}

@JvmRecord
data class FreeCamDataPacketC2S(
    val absolutePositionOfEyes: Vec3,
    val lookDirButViaHexAPI: Vec3
) : HexagonyMessageC2S {
    companion object : HexagonyMessageCompanion<FreeCamDataPacketC2S> {
        override val type = FreeCamDataPacketC2S::class.java
    }
}