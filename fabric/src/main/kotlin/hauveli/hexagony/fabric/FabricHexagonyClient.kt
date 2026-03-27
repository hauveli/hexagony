package hauveli.hexagony.fabric

import hauveli.hexagony.HexagonyClient
import hauveli.hexagony.common.control.PlayerActionAPI
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object FabricHexagonyClient : ClientModInitializer {
    override fun onInitializeClient() {
        HexagonyClient.init()
    }
}
