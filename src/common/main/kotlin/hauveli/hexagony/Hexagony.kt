package hauveli.hexagony

//import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.config.HexagonyConfigs
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.registry.*
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


object Hexagony {
    const val MODID = "hexagony"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MODID)


    @JvmField
    val MINECRAFT: Minecraft? = Minecraft.getInstance()

    @JvmStatic
    fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MODID, path)

    fun init() {
        initRegistries(
            HexagonyCriterions,
            HexagonyActions,
            HexagonyBlocks,
            HexagonyBlockEntities,
            HexagonyItems,
            HexagonyMobEffects,
            HexagonyRecipeTypes,
            HexagonyRecipeSerializers,
            HexagonySounds
        )
        HexagonyNetworking.init()
        HexagonyConfigs.init()
    }

    fun initServer() {
    }
}
