package hauveli.hexagony.fabric

import hauveli.hexagony.Hexagony
import net.fabricmc.api.ModInitializer

object FabricHexagony : ModInitializer {
    override fun onInitialize() {
        Hexagony.init()
    }
}
