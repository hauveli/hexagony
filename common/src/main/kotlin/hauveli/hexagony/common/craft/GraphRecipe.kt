package hauveli.hexagony.common.craft

import com.google.gson.JsonObject
import hauveli.hexagony.Hexagony
import hauveli.hexagony.common.craft.GraphCraftingJson.buildFromJson
import hauveli.hexagony.registry.HexagonyRecipeSerializers.GRAPH_SERIALIZER
import hauveli.hexagony.registry.HexagonyRecipeTypes.GRAPH_TYPE
import net.minecraft.core.NonNullList
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

// https://github.com/object-Object/HexDebug/blob/057b8100cdbc35138364c38bee6db9b72c1a2b8e/Common/src/main/kotlin/gay/object/hexdebug/recipes/FocusHolderFillingShapedRecipe.kt#L19
class GraphRecipe(
    id: ResourceLocation?, // dont need this for a recipe that doesnt exist but having it now might save me a headache
    val centerNode: GraphCraftingRecipes.ItemNodeVanilla,
    result: ItemStack,
    val resultInner: ItemStack
) : Recipe<Container> {


    val ID: ResourceLocation = Hexagony.id("graph_crafting")

    override fun getSerializer(): RecipeSerializer<*> {
        return GRAPH_SERIALIZER.value
    }

    override fun getType(): RecipeType<*> {
        return GRAPH_TYPE.value
    }


    override fun matches(container: Container, level: Level): Boolean {
        return false // your custom logic
    }

    override fun assemble(container: Container, registryAccess: RegistryAccess): ItemStack {
        return resultInner.copy()
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean = true

    override fun getResultItem(registryAccess: RegistryAccess): ItemStack {
        return resultInner.copy()
    }

    override fun getRemainingItems(container: Container): NonNullList<ItemStack?>? {
        // return super.getRemainingItems(container)
        TODO("Not yet implemented")
    }

    override fun getId(): ResourceLocation {
        return ID
    }

    companion object {
        private fun fromShapedRecipe(recipe: GraphRecipe,
                                     result: ItemStack,
                                     centerNode: GraphCraftingRecipes.ItemNodeVanilla): GraphRecipe {
            return recipe.run {
                GraphRecipe(
                    id = id,
                    result = result,
                    centerNode = centerNode,
                    resultInner = result
                )
            }
        }
    }

    class Serializer : RecipeSerializer<GraphRecipe> {
        override fun fromJson(recipeId: ResourceLocation, json: JsonObject): GraphRecipe {
            val (centerNode, resultId) = buildFromJson(json)
            val resultItem = BuiltInRegistries.ITEM.get(resultId).defaultInstance
            val recipe = GraphRecipe(
                resultId,
                centerNode,
                resultItem,
                resultItem)
            println("Hello!!!!! GRAPHTING!!!")
            // technically, this should probably be its own recipe type, as it is shaped, but not 2D.
            // GraphCraftingRecipes.graphRecipes.add(Pair(recipe, centerNode))
            //println("Loaded graph recipe: $path")
            return recipe
        }

        // Erm, because of the nature of the graph recipe
        // I'm not sure if I actually need these...
        override fun fromNetwork(recipeId: ResourceLocation, buf: FriendlyByteBuf): GraphRecipe {

            // This sucks
            // TOdo: actually do this
            val resultInner = buf.readItem()
            val bullshitNode = GraphCraftingRecipes.ItemNodeVanilla(
                arrayOf(resultInner),
                pos = Vec3.ZERO,
                shaped = true
            )
            val recipe = GraphRecipe(
                recipeId,
                centerNode = bullshitNode,
                result = resultInner,
                resultInner = resultInner,
            )
            return fromShapedRecipe(
                recipe, resultInner,
                centerNode = bullshitNode
            )
        }

        override fun toNetwork(
            buf: FriendlyByteBuf,
            recipe: GraphRecipe
        ) {
            //super.toNetwork(buf, recipe)
            // ???? does this even matter?
            buf.writeItem((recipe as GraphRecipe).resultInner)

        }
    }
}
