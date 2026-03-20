package hauveli.hexagony

import hauveli.hexagony.config.HexagonyClientConfig
import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.gui.screens.Screen

object HexagonyClient {
    fun init() {
        HexagonyClientConfig.init()
    }

    fun getConfigScreen(parent: Screen): Screen {
        return AutoConfig.getConfigScreen(HexagonyClientConfig.GlobalConfig::class.java, parent).get()
    }
}
