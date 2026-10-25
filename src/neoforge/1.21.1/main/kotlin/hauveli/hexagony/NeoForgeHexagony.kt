package hauveli.hexagony

import hauveli.hexagony.client.NeoForgeHexagonyClient
import hauveli.hexagony.datagen.NeoForgeHexagonyDatagen
import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff
import hauveli.hexagony.registry.HexagonyItems
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent


@Mod(Hexagony.MODID)
class NeoForgeHexagony(modBus: IEventBus, container: ModContainer) {
    init {

        modBus.apply {
            addListener(NeoForgeHexagonyClient::init)
            addListener(NeoForgeHexagonyDatagen::init)
            addListener(NeoForgeHexagonyServer::init)
            addListener(::registerCreativeModeTabItems)
        }
        Hexagony.init()
        // my cringe stuff
        NeoForge.EVENT_BUS.addListener(this::onServerStarted)
    }


    // uhh did I fix this when I updated to 1.21.1? todo: figure out if I did...
    fun onServerStarted(event: ServerStartedEvent) {
        GraphCraftingRecipeStuff.init(event.server.allLevels.first())
    }

    // how do I do this in common? if anybody knows please tell me
    fun registerCreativeModeTabItems(event: BuildCreativeModeTabContentsEvent) {
        // Hexagony.LOGGER.info("wtf it runs but doesn't work???? TEST FUCK {}", event.tab.displayName)
        HexagonyItems.registerItemCreativeTab(event, event.tab);
    }

    companion object {
        internal val container: ModContainer
            get() = ModList.get().getModContainerById(Hexagony.MODID).get()
    }
}
