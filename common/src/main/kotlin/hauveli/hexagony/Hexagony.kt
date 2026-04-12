package hauveli.hexagony

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import hauveli.hexagony.common.control.PlayerActionAPI
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.common.control.PlayerControlData.Companion.onJoinServer
import hauveli.hexagony.common.craft.GraphCraftingRecipes
import hauveli.hexagony.common.misc.TickScheduler
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import hauveli.hexagony.config.HexagonyServerConfig
import hauveli.hexagony.mind_anchor.MindAnchorManager
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.registry.HexagonyActions
import hauveli.hexagony.registry.HexagonyBlockEntities
import hauveli.hexagony.registry.HexagonyBlocks
import hauveli.hexagony.registry.HexagonyDamageTypes
import hauveli.hexagony.registry.HexagonyItems
import hauveli.hexagony.registry.HexagonyRecipeSerializers
import hauveli.hexagony.registry.HexagonyRecipeTypes
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
            // HexagonyDamageTypes,
            HexagonyBlockEntities, // Blocks must be registered first in order to access block.value!!!
            HexagonyRecipeSerializers,
            HexagonyRecipeTypes
        )
        // It works and I'm lazy?
        HexagonyNetworking.init()

        LifecycleEvent.SERVER_STARTED.register({
            server ->
            // custom weirdo recipe stuff
            GraphCraftingRecipes.init(server.overworld().level)
            // GraphRecipeLoader.loadAll() // TODO: only run this once somehow...
            // player clone and control stuff
            PlayerControlData.init(server)
        })
        TickScheduler.init()
    }

    fun initServer() {
        HexagonyServerConfig.initServer()
        PlayerActionAPI.initServer()
        MindAnchorManager.initServer()
    }
}
