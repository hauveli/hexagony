package hauveli.hexagony.networking.handler

import dev.architectury.networking.NetworkManager.PacketContext
import hauveli.hexagony.common.control.PlayerActionAPI
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.config.HexagonyServerConfig
import hauveli.hexagony.networking.msg.*
import net.minecraft.network.FriendlyByteBuf


fun HexagonyMessageS2C.applyOnClient(ctx: PacketContext) = ctx.queue {
    when (this) {
        is MsgSyncConfigS2C -> {
            HexagonyServerConfig.onSyncConfig(serverConfig)
        }

        // add more client-side message handlers here
        is MsgPlayerControlBooleanS2C -> {
            when (action) {
                PlayerControlData.MessageTypeBoolean.SHOULD_JUMP -> {
                    PlayerActionAPI.Client.jump(bool)
                }
                PlayerControlData.MessageTypeBoolean.SHOULD_SPRINT -> TODO()
                PlayerControlData.MessageTypeBoolean.SHOULD_SNEAK -> {
                    PlayerActionAPI.Client.sneak(bool)
                }
                PlayerControlData.MessageTypeBoolean.SHOULD_ATTACK -> TODO()
                PlayerControlData.MessageTypeBoolean.SHOULD_USE -> TODO()
                PlayerControlData.MessageTypeBoolean.SHOULD_SWAP_HANDS -> TODO()
                PlayerControlData.MessageTypeBoolean.SHOULD_DROP -> TODO()
                PlayerControlData.MessageTypeBoolean.SHOULD_DROP_STACK -> TODO()
            }
        }

        is MsgPlayerControlFloatS2C -> {
            when (action) {
                PlayerControlData.MessageTypeFloat.SHOULD_MOVE_FORWARD_BACKWARD -> {
                    PlayerActionAPI.Client.moveForwardBackward(float)
                }
                PlayerControlData.MessageTypeFloat.SHOULD_MOVE_LEFT_RIGHT -> TODO()
                PlayerControlData.MessageTypeFloat.SHOULD_LOOK_UP_DOWN -> TODO()
                PlayerControlData.MessageTypeFloat.SHOULD_LOOK_LEFT_RIGHT -> TODO()
                PlayerControlData.MessageTypeFloat.SHOULD_LOOK_ROLL -> TODO()
            }
        }
        is MsgPlayerControlIntegerS2C -> TODO()
    }
}
