package hauveli.hexagony.networking.msg

import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.mind_anchor.MindAnchorManager
import hauveli.hexagony.mind_anchor.MindAnchorScanner
import net.minecraft.network.FriendlyByteBuf
import org.joml.Vector3f

data class MsgMindAnchorFloatS2C(
    val action: MindAnchorManager.MessageTypesFloat,
    val value: Float
) : HexagonyMessageS2C {

    companion object : HexagonyMessageCompanion<MsgMindAnchorFloatS2C> {

        override val type = MsgMindAnchorFloatS2C::class.java

        override fun decode(buf: FriendlyByteBuf) = MsgMindAnchorFloatS2C(
            buf.readEnum(MindAnchorManager.MessageTypesFloat::class.java),
            buf.readFloat()
        )

        override fun MsgMindAnchorFloatS2C.encode(buf: FriendlyByteBuf) {
            buf.writeEnum(action)
            buf.writeFloat(value)
        }
    }
}