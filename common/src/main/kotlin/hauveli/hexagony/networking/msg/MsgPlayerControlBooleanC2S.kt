package hauveli.hexagony.networking.msg

import hauveli.hexagony.common.control.PlayerControlData
import net.minecraft.network.FriendlyByteBuf

data class MsgPlayerControlBooleanC2S(
    val action: PlayerControlData.MessageTypeSimple,
    val bool: Boolean
) : HexagonyMessageC2S {

    companion object : HexagonyMessageCompanion<MsgPlayerControlBooleanC2S> {

        override val type = MsgPlayerControlBooleanC2S::class.java

        override fun decode(buf: FriendlyByteBuf) = MsgPlayerControlBooleanC2S(
            buf.readEnum(PlayerControlData.MessageTypeSimple::class.java),
            buf.readBoolean()
        )

        override fun MsgPlayerControlBooleanC2S.encode(buf: FriendlyByteBuf) {
            buf.writeEnum(action)
            buf.writeBoolean(bool)
        }
    }
}