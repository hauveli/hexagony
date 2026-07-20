package hauveli.hexagony.features.fake_player

import net.minecraft.network.Connection
import net.minecraft.network.PacketListener
import net.minecraft.network.PacketSendListener
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow

class DummyConnection : Connection(PacketFlow.SERVERBOUND) {


    override fun isConnected(): Boolean = true
    override fun disconnect(component: Component) {}
    override fun send(packet: Packet<*>) {
        // swallow packet gulp
    }

    override fun send(packet: Packet<*>, listener: PacketSendListener?) {
        // listener.onSuccess()
    }

    override fun send(packet: Packet<*>, packetSendListener: PacketSendListener?, boolean: Boolean) {}

    override fun setReadOnly() {}

    override fun handleDisconnection() {}

    override fun setListenerForServerboundHandshake(packetListener: PacketListener) {}

    override fun <T : PacketListener?> setupInboundProtocol(protocolInfo: ProtocolInfo<T?>, packetListener: T?) {}
}