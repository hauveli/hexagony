package hauveli.hexagony.common.craft

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import hauveli.hexagony.common.craft.GraphCraftingRecipes.ItemNodeVanilla
import hauveli.hexagony.common.craft.GraphCraftingRecipes.connectBidirectional
import hauveli.hexagony.common.craft.GraphCraftingRecipes.connectNearest
import hauveli.hexagony.common.craft.GraphCraftingRecipes.findCenter
import hauveli.hexagony.common.craft.GraphCraftingRecipes.makePartitions
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
    data class Root(
        val partitions: Map<String, Map<String, Node>>,
        val result: ResultItem,
        val remaining: RemainingItems?
    )

    data class Node(
        val center: Boolean? = null,
        val validIngredients: List<String>,
        val count: Int? = 1,
        val relations: List<String>? = emptyList()
    )

    data class RemainingItems(
        val items: List<ResultItem>
    )

    data class ResultItem(
        val item: String,
        val count: Int? = 1
    )

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    fun parse(json: String): Root {
        return gson.fromJson(json, Root::class.java)
    }

    fun toStack(itemId: String, count: Int): ItemStack {
        val item = BuiltInRegistries.ITEM.get(ResourceLocation(itemId))
        return ItemStack(item, count)
    }

    fun toResult(stack: ResultItem): ItemStack {
        return toStack(stack.item, stack.count ?: 1)
    }

    fun ingredientsFromNode(jsonNode: Node): Array<ItemStack> {
        val count = jsonNode.count ?: 1
        return jsonNode.validIngredients
            .map { toStack(it, count) }
            .toTypedArray()
    }

    fun jsonNodeToItemNode(jsonNode: Node): ItemNodeVanilla {
        return ItemNodeVanilla(
            validIngredients = ingredientsFromNode(jsonNode),
            pos = Vec3.ZERO,
            shaped = true
        )
    }

    fun buildFromJson(file: String): Pair<ItemNodeVanilla, ResourceLocation> {

        val jsonRoot = parse(file)

        val partitions = jsonRoot.partitions
        val resultId = ResourceLocation(jsonRoot.result.item)
        val remainingItems = jsonRoot.remaining // not supported now, unsure if I want to support it?

        val nodes = mutableListOf<ItemNodeVanilla>()

        var centerNodeIndex = -1
        // First, need to get every node from every partition
        for (partition in partitions) {
            val partitionNeighborhood: MutableMap<String, ItemNodeVanilla> = mutableMapOf()
            for (node in partition.value) {
                val jsonNode = node.value
                val actualNode = jsonNodeToItemNode(jsonNode)
                nodes.add(actualNode)
                partitionNeighborhood[node.key] = actualNode
                if (jsonNode.center ?: false)
                    centerNodeIndex = nodes.count() - 1
            }
            // second loop, we want to give each node a neighbor
            for (node in partition.value) {
                val relations = node.value.relations ?: continue
                val actualNode = partitionNeighborhood[node.key] ?: continue
                for (neighbor in relations) {
                    val actualNeighbor = partitionNeighborhood[neighbor] ?: continue
                    connectBidirectional(
                        actualNode,
                        actualNeighbor
                    )
                }
            }
        }
        if (centerNodeIndex == -1) throw Error("No node was specified as center!")
        val centerNode = nodes[centerNodeIndex]
        // I don't even need to read partitions this way...
        makePartitions(centerNode)

        return centerNode to resultId // Pair strcuture is so weird in kotlin, (A,B) would be a bit more clear...
    }

    fun init() {

    }
}
