package hauveli.hexagony

import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff
import hauveli.hexagony.registry.HexagonyCreativeTabs
import hauveli.hexagony.registry.HexagonyItems
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.server.MinecraftServer

object FabricHexagony : ModInitializer {
    override fun onInitialize() {
        Hexagony.init()
        onServerStart()
        registerCreativeModeTabItems()
        // why is this ok in fabric but not neoforge? what...
        //registerItemModelProperties()
    }

    fun registerCreativeModeTabItems() {
        ItemGroupEvents.modifyEntriesEvent(HexagonyCreativeTabs.HEXAGONY_MAIN_TAB.key).register { entries ->
            HexagonyItems.registerItemCreativeTab(
                entries,
                HexagonyCreativeTabs.HEXAGONY_MAIN_TAB.value
            )
        }
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
