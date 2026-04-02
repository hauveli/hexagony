package hauveli.hexagony.common.bilocation

import net.minecraft.network.Connection
import net.minecraft.network.protocol.PacketFlow

class DummyConnection(receiving: PacketFlow) : Connection(receiving)