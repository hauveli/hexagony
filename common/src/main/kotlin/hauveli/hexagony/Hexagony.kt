package hauveli.hexagony

import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import hauveli.hexagony.config.HexagonyServerConfig
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.registry.HexagonyActions
import hauveli.hexagony.registry.HexagonyBlockEntities
import hauveli.hexagony.registry.HexagonyBlocks
import hauveli.hexagony.registry.HexagonyDamageTypes
import hauveli.hexagony.registry.HexagonyItems

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
            HexagonyItems // Blocks must be registered first in order to access block.value!!!
        )
        // It works and I'm lazy?
        initRegistries(
            HexagonyBlockEntities
        )
        HexagonyNetworking.init()
    }

    fun initServer() {
        HexagonyServerConfig.initServer()
    }
}
