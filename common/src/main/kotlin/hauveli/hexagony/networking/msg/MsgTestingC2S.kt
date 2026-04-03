package hauveli.hexagony.networking.msg

import hauveli.hexagony.common.control.PlayerControlData
import net.minecraft.network.FriendlyByteBuf

data class MsgTestingC2S(
    val action: PlayerControlData.MessageTypeBoolean,
    val bool: Boolean
) : HexagonyMessageC2S {

    companion object : HexagonyMessageCompanion<MsgTestingC2S> {

        override val type = MsgTestingC2S::class.java

        override fun decode(buf: FriendlyByteBuf) = MsgTestingC2S(
            buf.readEnum(PlayerControlData.MessageTypeBoolean::class.java),
            buf.readBoolean()
        )

        override fun MsgTestingC2S.encode(buf: FriendlyByteBuf) {
            buf.writeEnum(action)
            buf.writeBoolean(bool)
        }
    }
}