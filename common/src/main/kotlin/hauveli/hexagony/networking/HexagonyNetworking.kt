package hauveli.hexagony.networking

import dev.architectury.networking.NetworkChannel
import hauveli.hexagony.Hexagony
import hauveli.hexagony.networking.msg.HexagonyMessageCompanion

object HexagonyNetworking {
    val CHANNEL: NetworkChannel = NetworkChannel.create(Hexagony.id("networking_channel"))

    fun init() {
        for (subclass in HexagonyMessageCompanion::class.sealedSubclasses) {
            subclass.objectInstance?.register(CHANNEL)
        }
    }
}
