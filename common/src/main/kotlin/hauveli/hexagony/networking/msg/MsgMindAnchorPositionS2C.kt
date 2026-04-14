package hauveli.hexagony.networking.msg

import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.mind_anchor.MindAnchorManager
import hauveli.hexagony.mind_anchor.MindAnchorScanner
import net.minecraft.network.FriendlyByteBuf
import org.joml.Vector3f

data class MsgMindAnchorPositionS2C(
    val action: MindAnchorManager.MessageTypesVec,
    val vec: Vector3f
) : HexagonyMessageS2C {

    companion object : HexagonyMessageCompanion<MsgMindAnchorPositionS2C> {

        override val type = MsgMindAnchorPositionS2C::class.java

        override fun decode(buf: FriendlyByteBuf) = MsgMindAnchorPositionS2C(
            buf.readEnum(MindAnchorManager.MessageTypesVec::class.java),
            buf.readVector3f()
        )

        override fun MsgMindAnchorPositionS2C.encode(buf: FriendlyByteBuf) {
            buf.writeEnum(action)
            buf.writeVector3f(vec)
        }
    }
}