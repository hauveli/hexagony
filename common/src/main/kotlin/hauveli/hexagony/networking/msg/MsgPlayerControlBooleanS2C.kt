package hauveli.hexagony.networking.msg

import hauveli.hexagony.common.control.PlayerControlData
import net.minecraft.network.FriendlyByteBuf

data class MsgPlayerControlBooleanS2C(
    val action: PlayerControlData.MessageTypeBoolean,
    val bool: Boolean
) : HexagonyMessageS2C {

    companion object : HexagonyMessageCompanion<MsgPlayerControlBooleanS2C> {

        override val type = MsgPlayerControlBooleanS2C::class.java

        override fun decode(buf: FriendlyByteBuf) = MsgPlayerControlBooleanS2C(
            buf.readEnum(PlayerControlData.MessageTypeBoolean::class.java),
            buf.readBoolean()
        )

        override fun MsgPlayerControlBooleanS2C.encode(buf: FriendlyByteBuf) {
            buf.writeEnum(action)
            buf.writeBoolean(bool)
        }
    }
}