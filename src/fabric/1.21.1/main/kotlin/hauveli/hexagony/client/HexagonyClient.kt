package hauveli.hexagony.client

import hauveli.hexagony.client.HexagonyClient
import net.fabricmc.api.ClientModInitializer

object FabricHexagonyClient : ClientModInitializer {
    override fun onInitializeClient() {
        HexagonyClient.init()
    }
}