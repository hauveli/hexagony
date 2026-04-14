package hauveli.hexagony.networking.handler

import dev.architectury.networking.NetworkManager.PacketContext
import hauveli.hexagony.common.control.PlayerActionAPI
import hauveli.hexagony.common.control.PlayerActionAPI.onServerTick
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.common.control.PlayerControlData.Companion.onJoinServer
import hauveli.hexagony.networking.msg.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult

fun HexagonyMessageC2S.applyOnServer(ctx: PacketContext) = ctx.queue {
    // NOTE: this is commented out because otherwise it fails to compile if there's nothing inside of the when expression

    when (this) {
        // add server-side message handlers here
        is MsgTestingC2S -> {
            println("Debug test message reached")
            when (action) {
                else -> {
                    println("Sub else message branch reached")
                }
            }
        }

        is MsgPlayerControlBooleanC2S -> {
            when (action) {
                PlayerControlData.MessageTypeSimple.DATA_REQUEST -> {
                    println("Data request received")
                    val player = ctx.player ?: return@queue
                    println("PLayer exists")
                    (player as ServerPlayer)
                    onJoinServer(player)
                }
            }
        }
        else -> {
            println("Main else branch reached")
        }
    }
}
