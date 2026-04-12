package hauveli.hexagony.common.craft

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import hauveli.hexagony.common.craft.GraphCraftingRecipes.ItemNodeVanilla
import hauveli.hexagony.common.craft.GraphCraftingRecipes.connectNearest
import hauveli.hexagony.common.craft.GraphCraftingRecipes.findCenter
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.phys.Vec3

object GraphCraftingJson {
    data class GraphJson(
        val nodes: Map<String, NodeJson>,
        val result: String
    )

    data class NodeJson(
        val center: Boolean? = false,
        val item: String,
        val count: Int = 1,
        val relations: List<String> = emptyList()
    )

    fun buildFromJson(json: GraphJson): Pair<ItemNodeVanilla, ResourceLocation> {

        val gsonNodes = json.nodes

        // Shared node list (important: same reference for all nodes)
        val nodeList = mutableListOf<ItemNodeVanilla>()

        // Temporary map ID -> Node
        val builtNodes = mutableMapOf<String, ItemNodeVanilla>()

        // ---- PASS 1: Create nodes (no relations yet)
        for ((id, nodeJson) in gsonNodes) {

            val item = BuiltInRegistries.ITEM.get(ResourceLocation(nodeJson.item))
            val stack = ItemStack(item, nodeJson.count)

            val node = ItemNodeVanilla(
                validIngredients = arrayOf(stack),
                pos = Vec3.ZERO, // Replace with real positioning if needed
                nodeList = nodeList,
                shaped = true
            )

            builtNodes[id] = node
            nodeList.add(node)
        }

        // ---- PASS 2: Link relations
        for ((id, nodeJson) in gsonNodes) {
            val node = builtNodes[id]!!

            for (relationId in nodeJson.relations) {
                val neighbor = builtNodes[relationId]
                    ?: error("Unknown relation id: $relationId")

                // bi-directional relationship so I have to add both
                if (!node.neighbors.contains(neighbor)) {
                    node.neighbors.add(neighbor)
                }
                if (!neighbor.neighbors.contains(node)) {
                    neighbor.neighbors.add(node)
                }
            }
        }

        // ---- Find center node
        val centerEntry = gsonNodes.entries.find { it.value.center == true }
            ?: error("No center node defined")

        val centerNode = builtNodes[centerEntry.key]
            ?: error("Center node missing after build")

        val resultId = ResourceLocation(json.result)

        return centerNode to resultId // Pair strcuture is so weird in kotlin, (A,B) would be a bit more clear...
    }

    fun init() {

    }
}
