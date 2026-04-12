package hauveli.hexagony.registry

import dev.architectury.registry.registries.RegistrySupplier
import hauveli.hexagony.common.craft.GraphRecipe
import hauveli.hexagony.registry.HexagonyRecipeTypes.GRAPH_CRAFTING
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType

// https://github.com/object-Object/HexDebug/blob/5db5f78f5489863e2c20a3df472571a984877b7d/Common/src/main/kotlin/gay/object/hexdebug/registry/HexDebugRecipeSerializers.kt

object HexagonyRecipeSerializers : HexagonyRegistrar<RecipeSerializer<*>>(
    Registries.RECIPE_SERIALIZER,
    { BuiltInRegistries.RECIPE_SERIALIZER },
) {
    val GRAPH_SERIALIZER = register(GRAPH_CRAFTING) {
        GraphRecipe.Serializer()
    }
}