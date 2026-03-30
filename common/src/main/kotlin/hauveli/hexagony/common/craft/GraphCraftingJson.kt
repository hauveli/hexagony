package hauveli.hexagony.common.craft

import hauveli.hexagony.common.craft.GraphRecipes.RecipeNode
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

class GraphCraftingJson {
    data class JsonNode(
        val item: String,
        val count: Int = 1,
        val children: List<JsonNode>? = null
    )

    fun getItemByName(id: String): Item {
        return BuiltInRegistries.ITEM.get(ResourceLocation(id))
    }

    // Json to RecipeNode
    fun buildRecipeNode(json: JsonNode): RecipeNode {
        val item: Item = getItemByName(json.item)
        val node = RecipeNode(item, json.count)

        json.children?.forEach { childJson ->
            val childNode = buildRecipeNode(childJson)
            node.neighbors.add(childNode)
            childNode.neighbors.add(node) // Bidirectional connection
        }

        return node
    }

    /** Load full NodeRecipe from JSON string */
    fun loadNodeRecipe(jsonString: String): GraphRecipes.GraphRecipe {
        val gson = com.google.gson.Gson()
        val rootJson = gson.fromJson(jsonString, JsonNode::class.java)
        val rootNode = buildRecipeNode(rootJson)
        return GraphRecipes.GraphRecipe(rootNode)
    }
}