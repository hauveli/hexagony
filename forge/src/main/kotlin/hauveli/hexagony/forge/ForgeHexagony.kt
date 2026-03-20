package hauveli.hexagony.forge

import dev.architectury.platform.forge.EventBuses
import hauveli.hexagony.Hexagony
import hauveli.hexagony.forge.datagen.ForgeHexagonyDatagen
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(Hexagony.MODID)
class ForgeHexagony {
    init {
        MOD_BUS.apply {
            EventBuses.registerModEventBus(Hexagony.MODID, this)
            addListener(ForgeHexagonyClient::init)
            addListener(ForgeHexagonyDatagen::init)
            addListener(ForgeHexagonyServer::init)
        }
        Hexagony.init()
    }
}
