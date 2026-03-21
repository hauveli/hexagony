package hauveli.hexagony.fabric

import at.petrak.hexcasting.fabric.cc.CCBrainswept
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents.BRAINSWEPT;

import hauveli.hexagony.Hexagony
import hauveli.hexagony.common.lib.HexagonyBlocks
import hauveli.hexagony.common.lib.HexagonyItems

import net.fabricmc.api.DedicatedServerModInitializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.function.BiConsumer

object FabricHexagonyServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        Hexagony.initServer()
        initRegistries()
    }

    private fun initRegistries() {

        HexagonyBlocks.registerBlocks(bind(BuiltInRegistries.BLOCK) as BiConsumer<Block?, ResourceLocation?>)
        HexagonyBlocks.registerBlockItems(bind(BuiltInRegistries.ITEM) as BiConsumer<Item?, ResourceLocation?>)
    }

    private fun <T> bind(registry: Registry<in T>): BiConsumer<T, ResourceLocation> =
        BiConsumer<T, ResourceLocation> { t, id -> Registry.register(registry, id, t) }
}
