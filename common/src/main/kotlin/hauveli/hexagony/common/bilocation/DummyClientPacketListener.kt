package hauveli.hexagony.common.bilocation

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow

class DummyClientPlayNetworkHandler(minecraft: Minecraft) : ClientPacketListener(
    minecraft,
    null,
    DummyConnection(PacketFlow.CLIENTBOUND),
    null,
    minecraft.player?.gameProfile,
    null
) {
    override fun send(packet: Packet<*>) {
        // super.send(packet)
        // do nothing because this is a dummy
    }
}