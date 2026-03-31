package hauveli.hexagony.common.craft

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeSerializer

class GraphRecipeSerializer : RecipeSerializer<GraphRecipe> {

    override fun fromJson(
        id: ResourceLocation,
        json: JsonObject
    ): GraphRecipe {

        val graphJson = Gson().fromJson(json, GraphCraftingJson.GraphJson::class.java)

        val (centerNode, resultId) =
            GraphCraftingJson.buildFromJson(graphJson)

        return GraphRecipe(id, centerNode, resultId)
    }

    override fun fromNetwork(
        id: ResourceLocation,
        buffer: FriendlyByteBuf
    ): GraphRecipe {
        // todo: implement?
        throw UnsupportedOperationException()
    }

    override fun toNetwork(
        buffer: FriendlyByteBuf,
        recipe: GraphRecipe
    ) {
        // todo: implement?
    }
}