package hauveli.hexagony

//import hauveli.hexagony.networking.HexagonyNetworking
import at.petrak.hexcasting.xplat.IXplatAbstractions
import hauveli.hexagony.config.HexagonyConfigs
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.registry.*
import net.minecraft.client.Minecraft
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.function.BiConsumer


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
            HexagonyCreativeTabs,
            HexagonyBlocks,
            HexagonyItems,
            HexagonyBlockEntities,
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
