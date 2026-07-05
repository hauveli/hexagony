package hauveli.hexagony

import net.fabricmc.api.DedicatedServerModInitializer

object FabricHexagonyServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        Hexagony.initServer()
    }
}
