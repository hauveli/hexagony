package hauveli.hexagony.common.bilocation

import net.minecraft.network.Connection
import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

class DummyServerGamePacketListenerImpl(server: MinecraftServer, player: ServerPlayer
) : ServerGamePacketListenerImpl(server, DummyConnection(PacketFlow.CLIENTBOUND), player) {

    private var playerInfoSent = false

    override fun send(packet: Packet<*>) {
        // Only send PlayerInfo packet; ignore all others
        if (!playerInfoSent && packet is ClientboundPlayerInfoUpdatePacket) {
            super.send(packet) // actually send to allow the server to recognize player
            playerInfoSent = true
        }

    }

    override fun send(packet: Packet<*>, listener: PacketSendListener?) {
        send(packet)
        listener?.onSuccess()
    }

    companion object {
    fun sendDummyPlayerInfo(server: MinecraftServer, player: ServerPlayer) {
        val infoPacket = ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, player)

        // Directly handle the packet internally so Minecraft sees the player info
        server.playerList.broadcastAll(infoPacket)
    }
    }
}