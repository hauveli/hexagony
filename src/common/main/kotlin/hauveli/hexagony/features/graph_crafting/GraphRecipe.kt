package hauveli.hexagony.features.graph_crafting

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import com.mojang.serialization.MapCodec
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.graph_crafting.GraphCraftingJson.buildFromJsonFromGraph
import hauveli.hexagony.features.graph_crafting.GraphCraftingJson.convertToJson
import hauveli.hexagony.registry.HexagonyRecipeSerializers.GRAPH_SERIALIZER
import hauveli.hexagony.registry.HexagonyRecipeTypes.GRAPH_TYPE
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
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
    val centerNode: GraphCraftingRecipeStuff.ItemNodeVanilla,
    result: ItemStack, // what the fuck was I using result for again? nothing? for side effect result item ex. bucket? i forgor...
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
        TODO("Report to developer if reached") // I don't think I actually have to do this? but I may as well so that if it ever is reached, I get a bug report and learn how it was reached (not likely)
    }


    companion object Serializer : RecipeSerializer<GraphRecipe> {

        private val GSON = Gson()

        val CODEC: MapCodec<GraphRecipe> =
            Codec.PASSTHROUGH.xmap(
                { dynamic ->
                    val element = dynamic.convert(JsonOps.INSTANCE).value
                    buildFromJsonFromGraph(element.asJsonObject)
                },
                { recipe ->
                    Dynamic(
                        JsonOps.INSTANCE,
                        convertToJson(recipe)
                    )
                }
            ).fieldOf("graph")

        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, GraphRecipe> {
            override fun encode(buf: RegistryFriendlyByteBuf, recipe: GraphRecipe) {
                val recipeJson = GSON.toJson(convertToJson(recipe))
                buf.writeUtf(recipeJson)
            }

            override fun decode(buf: RegistryFriendlyByteBuf): GraphRecipe {
                val json = GSON.fromJson(buf.readUtf(), JsonObject::class.java)
                return buildFromJsonFromGraph(json)
            }
        }

        override fun codec(): MapCodec<GraphRecipe> {
            return CODEC
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, GraphRecipe> {
            return STREAM_CODEC
        }

        // junk stuff for testing
        private fun fromShapedRecipe(recipe: GraphRecipe,
                                     result: ItemStack,
                                     centerNode: GraphCraftingRecipeStuff.ItemNodeVanilla): GraphRecipe {
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
            centerNode = GraphCraftingRecipeStuff.ItemNodeVanilla(
                arrayOf(),
                Vec3.ZERO,
                shaped = false),
            result = ItemStack.EMPTY,
            resultInner = ItemStack.EMPTY
        )
    }
}
