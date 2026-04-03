package hauveli.hexagony.networking.handler

import dev.architectury.networking.NetworkManager.PacketContext
import hauveli.hexagony.common.control.PlayerActionAPI
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.networking.msg.*
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
        else -> {
            println("Main else branch reached")
        }
    }
}
