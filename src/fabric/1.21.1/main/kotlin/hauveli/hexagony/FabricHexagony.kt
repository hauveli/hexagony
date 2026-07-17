package hauveli.hexagony

import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

object FabricHexagony : ModInitializer {
    override fun onInitialize() {
        Hexagony.init()
        onServerStart()
    }

    fun onServerStart() {
        ServerLifecycleEvents.SERVER_STARTED.register(
            ServerLifecycleEvents.ServerStarted {
                server: MinecraftServer ->
                GraphCraftingRecipeStuff.init(server.allLevels.first())
            }
        )
    }
}
