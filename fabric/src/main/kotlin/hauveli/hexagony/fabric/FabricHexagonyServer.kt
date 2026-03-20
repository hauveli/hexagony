package hauveli.hexagony.fabric

import hauveli.hexagony.Hexagony
import net.fabricmc.api.DedicatedServerModInitializer

object FabricHexagonyServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        Hexagony.initServer()
    }
}
