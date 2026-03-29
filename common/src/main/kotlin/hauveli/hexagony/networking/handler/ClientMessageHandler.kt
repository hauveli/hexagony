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

        // I'm sure it's possible to do something prettier but I don't predict this part will grow in scope by much
        // add more client-side message handlers here
        is MsgPlayerControlBooleanS2C -> {
            when (action) {
                PlayerControlData.MessageTypeBoolean.SHOULD_STOP -> {
                    PlayerActionAPI.Client.stop(bool)
                }
                PlayerControlData.MessageTypeBoolean.SHOULD_JUMP -> {
                    PlayerActionAPI.Client.jump(bool)
                }
                PlayerControlData.MessageTypeBoolean.SHOULD_SPRINT -> {
                    PlayerActionAPI.Client.sprint(bool)
                }
                PlayerControlData.MessageTypeBoolean.SHOULD_SNEAK -> {
                    PlayerActionAPI.Client.sneak(bool)
                }
                PlayerControlData.MessageTypeBoolean.SHOULD_ATTACK -> {
                    PlayerActionAPI.Client.attack(bool)
                }
                PlayerControlData.MessageTypeBoolean.SHOULD_USE -> {
                    PlayerActionAPI.Client.use(bool)
                }
                PlayerControlData.MessageTypeBoolean.SHOULD_SWAP_HANDS -> {
                    PlayerActionAPI.Client.swapHands(bool)
                }
                PlayerControlData.MessageTypeBoolean.SHOULD_DROP -> {
                    PlayerActionAPI.Client.drop(bool)
                }
                PlayerControlData.MessageTypeBoolean.SHOULD_DROP_STACK -> {
                    PlayerActionAPI.Client.dropStack(bool)
                }
            }
        }

        is MsgPlayerControlFloatS2C -> {
            when (action) {
                PlayerControlData.MessageTypeFloat.SHOULD_MOVE_FORWARD_BACKWARD -> {
                    PlayerActionAPI.Client.moveForwardBackward(float)
                }
                PlayerControlData.MessageTypeFloat.SHOULD_MOVE_LEFT_RIGHT -> {
                    PlayerActionAPI.Client.moveLeftRight(float)
                }
                PlayerControlData.MessageTypeFloat.SHOULD_LOOK_UP_DOWN -> {
                    PlayerActionAPI.Client.lookUpDown(float)
                }
                PlayerControlData.MessageTypeFloat.SHOULD_LOOK_LEFT_RIGHT -> {
                    PlayerActionAPI.Client.lookLeftRight(float)
                }
                PlayerControlData.MessageTypeFloat.SHOULD_LOOK_ROLL -> {
                    PlayerActionAPI.Client.lookRoll(float)
                }
            }
        }
        is MsgPlayerControlIntegerS2C -> {
            when (action) {
                PlayerControlData.MessageTypeInt.SHOULD_ATTACK_PERIOD -> {
                    PlayerActionAPI.Client.attackPeriodic(integer)
                }
                PlayerControlData.MessageTypeInt.SHOULD_USE_PERIOD -> {
                    PlayerActionAPI.Client.usePeriodic(integer)
                }
                PlayerControlData.MessageTypeInt.SHOULD_HOTBAR_SLOT -> {
                    PlayerActionAPI.Client.hotbarSlot(integer)
                }
            }
        }
    }
}
