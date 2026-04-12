package hauveli.hexagony.registry

import dev.architectury.registry.registries.RegistrySupplier
import hauveli.hexagony.Hexagony
import hauveli.hexagony.common.craft.GraphRecipe
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType

// https://github.com/object-Object/HexDebug/blob/5db5f78f5489863e2c20a3df472571a984877b7d/Common/src/main/kotlin/gay/object/hexdebug/registry/HexDebugRecipeSerializers.kt

object HexagonyRecipeTypes : HexagonyRegistrar<RecipeType<*>>(
    Registries.RECIPE_TYPE,
    { BuiltInRegistries.RECIPE_TYPE },
) {
    const val GRAPH_CRAFTING = "graph_crafting"

    val GRAPH_TYPE = register(GRAPH_CRAFTING) {
        GraphRecipeType
    }

    object GraphRecipeType : RecipeType<GraphRecipe> {
        override fun toString(): String =
            Hexagony.id("graph_crafting").toString()
    }

    fun initRecipeTypes() {
        /*
        register(
            BuiltInRegistries.RECIPE_TYPE,
            Hexagony.id("graph_crafting"),
            GRAPH_TYPE
        )
        */
    }
}