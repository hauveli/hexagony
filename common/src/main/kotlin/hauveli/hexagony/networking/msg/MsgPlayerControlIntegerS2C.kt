package hauveli.hexagony.networking.msg

import hauveli.hexagony.common.control.PlayerControlData
import net.minecraft.network.FriendlyByteBuf

data class MsgPlayerControlIntegerS2C(
    val action: PlayerControlData.MessageTypeInt,
    val integer: Int
) : HexagonyMessageS2C {

    companion object : HexagonyMessageCompanion<MsgPlayerControlIntegerS2C> {

        override val type = MsgPlayerControlIntegerS2C::class.java

        override fun decode(buf: FriendlyByteBuf) = MsgPlayerControlIntegerS2C(
            buf.readEnum(PlayerControlData.MessageTypeInt::class.java),
            buf.readInt()
        )

        override fun MsgPlayerControlIntegerS2C.encode(buf: FriendlyByteBuf) {
            buf.writeEnum(action)
            buf.writeInt(integer)
        }
    }
}