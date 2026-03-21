package hauveli.hexagony.fabric

import at.petrak.hexcasting.fabric.cc.CCBrainswept
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents.BRAINSWEPT;

import hauveli.hexagony.Hexagony

import net.fabricmc.api.DedicatedServerModInitializer
import net.minecraft.world.entity.LivingEntity

object FabricHexagonyServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        Hexagony.initServer()
    }
}
