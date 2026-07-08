package hauveli.hexagony

import hauveli.hexagony.client.NeoForgeHexagonyClient
import hauveli.hexagony.datagen.NeoForgeHexagonyDatagen
import hauveli.hexagony.features.graph_crafting.GraphCraftingFromNormalRecipes
import net.minecraft.server.MinecraftServer
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.event.server.ServerStartedEvent

@Mod(Hexagony.MODID)
class NeoForgeHexagony(modBus: IEventBus, container: ModContainer) {
    init {
        modBus.apply {
            addListener(NeoForgeHexagonyClient::init)
            addListener(NeoForgeHexagonyDatagen::init)
            addListener(NeoForgeHexagonyServer::init)
        }
        Hexagony.init()
    }


    companion object {
        @SubscribeEvent
        @JvmStatic
        fun onServerStarted(event: ServerStartedEvent) {
            GraphCraftingFromNormalRecipes.init(event.server.allLevels.first())
        }

        internal val container: ModContainer
            get() = ModList.get().getModContainerById(Hexagony.MODID).get()
    }
}
