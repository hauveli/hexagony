package hauveli.hexagony

import net.fabricmc.api.ModInitializer

object FabricHexagony : ModInitializer {
    override fun onInitialize() {
        Hexagony.init()
    }
}
