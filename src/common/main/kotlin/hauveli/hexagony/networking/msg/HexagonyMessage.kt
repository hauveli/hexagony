package hauveli.hexagony.networking.msg

import hauveli.hexagony.Hexagony
/*
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.networking.handler.applyOnClient
import hauveli.hexagony.networking.handler.applyOnServer
import io.wispforest.owo.network.ClientAccess
import io.wispforest.owo.network.OwoNetChannel
import io.wispforest.owo.network.ServerAccess
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer

sealed interface HexagonyMessage

sealed interface HexagonyMessageC2S : HexagonyMessage {
    fun <T> T.sendToServer() where T : Record {
        HexagonyNetworking.CHANNEL.clientHandle().send(this)
    }
}

sealed interface HexagonyMessageS2C : HexagonyMessage {
}

fun <T> T.sendToPlayer(player: ServerPlayer) where T : Record {
    HexagonyNetworking.CHANNEL.serverHandle(player).send( this)
}

fun <T> T.sendToPlayers(players: Iterable<ServerPlayer>) where T : Record {
    players.forEach { sendToPlayer(it) }
}

sealed interface HexagonyMessageCompanion<T> where T : HexagonyMessage, T : Record {
    val type: Class<T>

    fun apply(msg: T, access: ServerAccess): Unit {
        Hexagony.LOGGER.debug("Server received packet from {}: {}", access.player().name.string, this)
        when (msg) {
            is HexagonyMessageC2S -> msg.applyOnServer(access)
            else -> Hexagony.LOGGER.warn("Message not handled on server: {}", msg::class)
        }
    }

    fun apply(msg: T, access: ClientAccess): Unit {
        Hexagony.LOGGER.debug("Client received packet: {}", this)
        when (msg) {
            is HexagonyMessageS2C -> msg.applyOnClient(access)
            else -> Hexagony.LOGGER.warn("Message not handled on client: {}", msg::class)
        }
    }

    fun register(channel: OwoNetChannel) {
        channel.registerServerbound(type) { msg, access -> apply(msg, access) }
    }
}
*/
