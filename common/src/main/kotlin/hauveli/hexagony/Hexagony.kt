package hauveli.hexagony

import dev.architectury.event.events.common.LifecycleEvent
import hauveli.hexagony.common.craft.GraphCraftingRecipes
import hauveli.hexagony.common.craft.GraphRecipeLoader
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import hauveli.hexagony.config.HexagonyServerConfig
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.registry.HexagonyActions
import hauveli.hexagony.registry.HexagonyBlockEntities
import hauveli.hexagony.registry.HexagonyBlocks
import hauveli.hexagony.registry.HexagonyItems
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

/*
TODO: lore
Why does Nature not always know where the mind anchor is? (it's because I could not figure out a clean way to track it without costing a lot)
Should I just make it impossible to mine by anyone but the person who placed it?

 */

object Hexagony {
    const val MODID = "hexagony"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MODID)

    @JvmStatic
    fun id(path: String) = ResourceLocation(MODID, path)

    fun init() {
        HexagonyServerConfig.init()
        initRegistries(
            HexagonyActions,
            HexagonyBlocks,
            HexagonyItems,
            HexagonyBlockEntities // Blocks must be registered first in order to access block.value!!!
        )
        // It works and I'm lazy?
        HexagonyNetworking.init()

        LifecycleEvent.SERVER_STARTED.register({
            server ->
            // custom weirdo recipe stuff
            GraphCraftingRecipes.init(server.overworld().level)
            GraphRecipeLoader.loadAll() // TODO: only run this once somehow...
        })
    }

    fun initServer() {
        HexagonyServerConfig.initServer()
    }
}
