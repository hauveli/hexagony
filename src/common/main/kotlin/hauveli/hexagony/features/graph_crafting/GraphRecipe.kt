package hauveli.hexagony.features.graph_crafting

import com.google.gson.JsonObject
import com.mojang.serialization.MapCodec
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.graph_crafting.GraphCraftingJson.buildFromJson
import hauveli.hexagony.registry.HexagonyRecipeSerializers.GRAPH_SERIALIZER
import hauveli.hexagony.registry.HexagonyRecipeTypes.GRAPH_TYPE
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

// https://github.com/object-Object/HexDebug/blob/057b8100cdbc35138364c38bee6db9b72c1a2b8e/Common/src/main/kotlin/gay/object/hexdebug/recipes/FocusHolderFillingShapedRecipe.kt#L19
class GraphRecipe(
    id: ResourceLocation?, // dont need this for a recipe that doesnt exist but having it now might save me a headache
    val centerNode: GraphCraftingFromNormalRecipes.ItemNodeVanilla,
    result: ItemStack,
    val resultInner: ItemStack
) : Recipe<CraftingInput> {


    val ID: ResourceLocation = Hexagony.id("graph_crafting")

    override fun getSerializer(): RecipeSerializer<*> {
        return GRAPH_SERIALIZER.value
    }

    override fun getType(): RecipeType<*> {
        return GRAPH_TYPE.value
    }

    override fun matches(
        p0: CraftingInput,
        p1: Level
    ): Boolean {
        return false
    }

    override fun assemble(
        p0: CraftingInput,
        p1: HolderLookup.Provider
    ): ItemStack {
        return resultInner.copy()
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean = true
    override fun getResultItem(p0: HolderLookup.Provider): ItemStack {
        return resultInner.copy()
    }

    override fun getRemainingItems(p0: CraftingInput): NonNullList<ItemStack> {
        // return super.getRemainingItems(container)
        TODO("Report to developer if reached") // I don't think I actually have to do this?
    }

    /*
    override fun getId(): ResourceLocation {
        return ID
    }
     */

    companion object {
        private fun fromShapedRecipe(recipe: GraphRecipe,
                                     result: ItemStack,
                                     centerNode: GraphCraftingFromNormalRecipes.ItemNodeVanilla): GraphRecipe {
            return recipe.run {
                GraphRecipe(
                    id = recipe.ID, // uhhhhhh, is this fine?
                    result = result,
                    centerNode = centerNode,
                    resultInner = result
                )
            }
        }


        val EMPTY_FROM_JSON = GraphRecipe(
            id = Hexagony.id("mind_anchor/full"),
            centerNode = GraphCraftingFromNormalRecipes.ItemNodeVanilla(
                arrayOf(),
                Vec3.ZERO,
                shaped = false),
            result = ItemStack.EMPTY,
            resultInner = ItemStack.EMPTY
        )
    }

    class Serializer : RecipeSerializer<GraphRecipe> {
        fun fromJson(recipeId: ResourceLocation, json: JsonObject): GraphRecipe {
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


        override fun codec(): MapCodec<GraphRecipe> {
            return MapCodec.unit(EMPTY_FROM_JSON)
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, GraphRecipe> =
            StreamCodec.of(
                { buf, recipe ->
                    // ItemStack.STREAM_CODEC.encode(buf, recipe.resultInner)
                },
                { buf ->
                    EMPTY_FROM_JSON
                    /*
                    val resultInner = ItemStack.STREAM_CODEC.decode(buf)

                    val node = GraphCraftingRecipes.ItemNodeVanilla(
                        arrayOf(resultInner),
                        pos = Vec3.ZERO,
                        shaped = true
                    )

                    GraphRecipe(
                        id = null,
                        centerNode = node,
                        result = resultInner,
                        resultInner = resultInner
                    )

                     */
                }
            )

        /*
        // Erm, because of the nature of the graph recipe
        // I'm not sure if I actually need these...
        override fun fromNetwork(recipeId: ResourceLocation, buf: RegistryFriendlyByteBuf): GraphRecipe {

            // This sucks
            val resultInner = buf.
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
         */
    }
}
