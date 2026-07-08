package hauveli.hexagony

import hauveli.hexagony.config.HexagonyConfigs
import hauveli.hexagony.networking.HexagonyNetworking
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
//import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.registry.HexagonyActions
import hauveli.hexagony.registry.HexagonyBlockEntities
import hauveli.hexagony.registry.HexagonyBlocks
import hauveli.hexagony.registry.HexagonyCriterions
import hauveli.hexagony.registry.HexagonyItems
import hauveli.hexagony.registry.HexagonyRecipeSerializers
import hauveli.hexagony.registry.HexagonyRecipeTypes

object Hexagony {
    const val MODID = "hexagony"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MODID)



    @JvmStatic
    fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MODID, path)

    fun init() {
        initRegistries(
            HexagonyCriterions,
            HexagonyActions,
            HexagonyBlocks,
            HexagonyBlockEntities,
            HexagonyItems,
            HexagonyRecipeTypes,
            HexagonyRecipeSerializers
        )
        HexagonyNetworking.init()
        HexagonyConfigs.init()
    }

    fun initServer() {
    }
}
