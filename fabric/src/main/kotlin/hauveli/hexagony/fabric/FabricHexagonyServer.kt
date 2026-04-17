package hauveli.hexagony.fabric

import at.petrak.hexcasting.fabric.cc.CCBrainswept
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents.BRAINSWEPT;

import hauveli.hexagony.Hexagony
import hauveli.hexagony.common.control.PlayerActionAPI.onServerTick

import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.function.BiConsumer

object FabricHexagonyServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        println("Initserver is being called!!!!")
        Hexagony.initServer()
        initRegistries()

        ServerLifecycleEvents.SERVER_STARTED.register{
            server ->
            println("Hello...")
            server.sendSystemMessage(Component.nullToEmpty("Hello!!!!"))
        }
    }

    private fun initRegistries() {
    }

    private fun <T> bind(registry: Registry<in T>): BiConsumer<T, ResourceLocation> =
        BiConsumer<T, ResourceLocation> { t, id -> Registry.register(registry, id, t) }
}
