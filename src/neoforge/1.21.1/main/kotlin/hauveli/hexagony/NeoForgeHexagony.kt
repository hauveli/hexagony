package hauveli.hexagony

import hauveli.hexagony.client.NeoForgeHexagonyClient
import hauveli.hexagony.datagen.NeoForgeHexagonyDatagen
import hauveli.hexagony.features.graph_crafting.GraphCraftingFromNormalRecipes
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
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
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
    }

    fun onServerStarted(event: ServerStartedEvent) {
        GraphCraftingFromNormalRecipes.init(event.server.allLevels.first())
    }


    companion object {
        internal val container: ModContainer
            get() = ModList.get().getModContainerById(Hexagony.MODID).get()
    }
}
