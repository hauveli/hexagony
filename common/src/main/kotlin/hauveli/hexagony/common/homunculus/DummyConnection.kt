package hauveli.hexagony.common.homunculus

import net.minecraft.network.Connection
import net.minecraft.network.PacketSendListener
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow

class DummyConnection(receiving: PacketFlow) : Connection(receiving) {


    override fun isConnected(): Boolean = true
    override fun disconnect(component: Component) {}
    override fun send(packet: Packet<*>) {
        // swallow packet
    }

    override fun send(packet: Packet<*>, listener: PacketSendListener?) {
        listener?.onSuccess()
    }
}