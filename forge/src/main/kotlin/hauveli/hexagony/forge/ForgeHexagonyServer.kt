package hauveli.hexagony.forge

import hauveli.hexagony.Hexagony
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent

object ForgeHexagonyServer {
    @Suppress("UNUSED_PARAMETER")
    fun init(event: FMLDedicatedServerSetupEvent) {
        Hexagony.initServer()
    }
}
