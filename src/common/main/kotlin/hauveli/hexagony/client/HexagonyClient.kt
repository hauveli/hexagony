package hauveli.hexagony.client

import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen

object HexagonyClient {
    @JvmField
    val MINECRAFT: Minecraft? = Minecraft.getInstance()

    fun init() {
    }
}