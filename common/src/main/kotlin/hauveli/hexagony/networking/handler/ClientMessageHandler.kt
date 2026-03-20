package hauveli.hexagony.networking.handler

import dev.architectury.networking.NetworkManager.PacketContext
import hauveli.hexagony.config.HexagonyServerConfig
import hauveli.hexagony.networking.msg.*

fun HexagonyMessageS2C.applyOnClient(ctx: PacketContext) = ctx.queue {
    when (this) {
        is MsgSyncConfigS2C -> {
            HexagonyServerConfig.onSyncConfig(serverConfig)
        }

        // add more client-side message handlers here
    }
}
