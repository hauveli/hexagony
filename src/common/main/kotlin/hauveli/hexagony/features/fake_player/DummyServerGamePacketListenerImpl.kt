package hauveli.hexagony.features.fake_player

import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

class DummyServerGamePacketListenerImpl(server: MinecraftServer, connection: DummyConnection, player: ServerPlayer
) : ServerGamePacketListenerImpl(server, connection, player) {

    private var playerInfoSent = false

    // unsure if I should ignore or not ignore ticks and movement, todo: check if asbtractplayer/abstractserverplayer exists
    override fun tick() {
        super.tick()
    }

    override fun handleMovePlayer(packet: ServerboundMovePlayerPacket) {
        super.handleMovePlayer(packet)
    }

    override fun send(packet: Packet<*>) {
        // only send PlayerInfo packet; ignore all others?
        if (!playerInfoSent && packet is ClientboundPlayerInfoUpdatePacket) {
            super.send(packet) // actually send to allow the server to recognize player
            playerInfoSent = true
        }

    }

    override fun send(packet: Packet<*>, listener: PacketSendListener?) {
        super.send(packet, listener)
        listener?.onSuccess()
    }

    override fun isAcceptingMessages(): Boolean {
        return true
    }
}