package hauveli.hexagony

import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent

object NeoForgeHexagonyServer {
    @Suppress("UNUSED_PARAMETER")
    fun init(event: FMLDedicatedServerSetupEvent) {
        Hexagony.initServer()
    }
}

