package hauveli.hexagony.fabric

import hauveli.hexagony.HexagonyClient
import net.fabricmc.api.ClientModInitializer

object FabricHexagonyClient : ClientModInitializer {
    override fun onInitializeClient() {
        HexagonyClient.init()
    }
}
