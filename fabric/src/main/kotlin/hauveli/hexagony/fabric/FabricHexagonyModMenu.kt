package hauveli.hexagony.fabric

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import hauveli.hexagony.HexagonyClient

object FabricHexagonyModMenu : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory(HexagonyClient::getConfigScreen)
}
