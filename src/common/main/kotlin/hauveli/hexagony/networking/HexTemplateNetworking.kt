package hauveli.hexagony.networking

import hauveli.hexagony.Hexagony
import hauveli.hexagony.networking.msg.HexagonyMessageC2S
import hauveli.hexagony.networking.msg.HexagonyMessageCompanion
import hauveli.hexagony.networking.msg.HexagonyMessageS2C
import io.wispforest.owo.network.OwoNetChannel

object HexagonyNetworking {
    val CHANNEL: OwoNetChannel = OwoNetChannel.create(Hexagony.id("networking_channel"))

    fun init() {
        for (companionClass in HexagonyMessageCompanion::class.sealedSubclasses) {
            val companion = companionClass.objectInstance ?: continue

            when {
                HexagonyMessageC2S::class.java.isAssignableFrom(companion.type) ->
                    companion.registerServerbound(CHANNEL)

                HexagonyMessageS2C::class.java.isAssignableFrom(companion.type) ->
                    companion.registerClientbound(CHANNEL)

                else ->
                    error("Unknown packet type: ${companion.type}") // is this possible?
            }
        }
    }
}
