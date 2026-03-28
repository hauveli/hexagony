package hauveli.hexagony.networking.msg

import hauveli.hexagony.common.control.PlayerControlData
import net.minecraft.network.FriendlyByteBuf

data class MsgPlayerControlFloatS2C(
    val action: PlayerControlData.MessageTypeFloat,
    val float: Float
) : HexagonyMessageS2C {

    companion object : HexagonyMessageCompanion<MsgPlayerControlFloatS2C> {

        override val type = MsgPlayerControlFloatS2C::class.java

        override fun decode(buf: FriendlyByteBuf) = MsgPlayerControlFloatS2C(
            buf.readEnum(PlayerControlData.MessageTypeFloat::class.java),
            buf.readFloat()
        )

        override fun MsgPlayerControlFloatS2C.encode(buf: FriendlyByteBuf) {
            buf.writeEnum(action)
            buf.writeFloat(float)
        }
    }
}