package hauveli.hexagony.networking.msg

import dev.architectury.networking.NetworkChannel
import dev.architectury.networking.NetworkManager.PacketContext
import hauveli.hexagony.Hexagony
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.networking.handler.applyOnClient
import hauveli.hexagony.networking.handler.applyOnServer
import net.fabricmc.api.EnvType
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import java.util.function.Supplier

sealed interface HexagonyMessage

sealed interface HexagonyMessageC2S : HexagonyMessage {
    fun sendToServer() {
        HexagonyNetworking.CHANNEL.sendToServer(this)
    }
}

sealed interface HexagonyMessageS2C : HexagonyMessage {
    fun sendToPlayer(player: ServerPlayer) {
        HexagonyNetworking.CHANNEL.sendToPlayer(player, this)
    }

    fun sendToPlayers(players: Iterable<ServerPlayer>) {
        HexagonyNetworking.CHANNEL.sendToPlayers(players, this)
    }
}

sealed interface HexagonyMessageCompanion<T : HexagonyMessage> {
    val type: Class<T>

    fun decode(buf: FriendlyByteBuf): T

    fun T.encode(buf: FriendlyByteBuf)

    fun apply(msg: T, supplier: Supplier<PacketContext>) {
        val ctx = supplier.get()
        when (ctx.env) {
            EnvType.SERVER, null -> {
                Hexagony.LOGGER.debug("Server received packet from {}: {}", ctx.player.name.string, this)
                when (msg) {
                    is HexagonyMessageC2S -> msg.applyOnServer(ctx)
                    else -> Hexagony.LOGGER.warn("Message not handled on server: {}", msg::class)
                }
            }
            EnvType.CLIENT -> {
                Hexagony.LOGGER.debug("Client received packet: {}", this)
                when (msg) {
                    is HexagonyMessageS2C -> msg.applyOnClient(ctx)
                    else -> Hexagony.LOGGER.warn("Message not handled on client: {}", msg::class)
                }
            }
        }
    }

    fun register(channel: NetworkChannel) {
        channel.register(type, { msg, buf -> msg.encode(buf) }, ::decode, ::apply)
    }
}
