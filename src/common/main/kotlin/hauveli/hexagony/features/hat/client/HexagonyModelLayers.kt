package hauveli.hexagony.features.hat.client

import at.petrak.hexcasting.api.HexAPI
import hauveli.hexagony.Hexagony
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.builders.LayerDefinition
import java.util.function.BiConsumer
import java.util.function.Supplier

// https://github.com/VazkiiMods/Botania/blob/1.19.x/Xplat/src/main/java/vazkii/botania/client/model/BotaniaModelLayers.java
object HexagonyModelLayers {
    val LIVING_HAT_0: ModelLayerLocation = make("living_hat_0")

    private fun make(name: String, layer: String = "main"): ModelLayerLocation {
        // Don't add to vanilla's ModelLayers. It seems to only be used for error checking
        // And would be annoying to do under Forge's parallel mod loading
        return ModelLayerLocation(Hexagony.id(name), layer)
    }

    // moving this stuff into the same file:
    // https://github.com/VazkiiMods/Botania/blob/1.19.x/Xplat/src/main/java/vazkii/botania/client/model/BotaniaLayerDefinitions.java
    fun init(consumer: BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>>) {
        consumer.accept(LIVING_HAT_0, Supplier { HexagonyLivingHatModel.variant0() })
    }
}