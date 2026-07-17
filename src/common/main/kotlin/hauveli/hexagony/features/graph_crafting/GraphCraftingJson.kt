package hauveli.hexagony.features.graph_crafting

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff.ItemNodeVanilla
import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff.connectBidirectional
import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff.makePartitions
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import java.util.Optional
import kotlin.collections.iterator


// i fucking can't think of how to make this work with the way the graph recipe has to work right now
// todo: revisit this and for realsies implement a proper codec instead of the hackjob I chose
// wip but also todo: properly implement CODEC so I can make an awesome JEI/EMI plugin that shows the crafts
object GraphCraftingJson {



    data class Root(
        val graph: Graph
    ) {
        companion object {
            val CODEC = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Graph.CODEC.fieldOf("graph")
                        .forGetter(Root::graph)
                ).apply(instance) { graph ->
                    Root(graph)
                }
            }
        }
    }

    val PARTITIONS_CODEC =
        Codec.unboundedMap(
            Codec.STRING,
            Codec.unboundedMap(Codec.STRING, Node.CODEC)
        )

    data class Graph(
        val partitions: Map<String, Map<String, Node>>,
        val result: ResultItem,
        val remaining: RemainingItems?
    ) {
        companion object {
            val CODEC = RecordCodecBuilder.create { instance ->
                instance.group(
                    PARTITIONS_CODEC
                        .fieldOf("partitions")
                        .forGetter(Graph::partitions),

                    ResultItem.CODEC
                        .fieldOf("result")
                        .forGetter(Graph::result),

                    RemainingItems.CODEC
                        .optionalFieldOf("remainingItems")
                        .forGetter { Optional.ofNullable(it.remaining) }

                ).apply(instance) { partitions, result, remaining ->
                    Graph(
                        partitions,
                        result,
                        remaining.orElse(null)
                    )
                }
            }
        }
    }

    data class Node(
        val center: Boolean? = null,
        val validIngredients: List<String>,
        val count: Int? = 1,
        val relations: List<String>? = emptyList()
    ) {
        companion object {
            val CODEC = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.BOOL.optionalFieldOf("center")
                        .forGetter { Optional.ofNullable(it.center) },

                    Codec.STRING.listOf()
                        .fieldOf("validIngredients")
                        .forGetter(Node::validIngredients),

                    Codec.INT.optionalFieldOf("count")
                        .forGetter { Optional.ofNullable(it.count) },

                    Codec.STRING.listOf()
                        .optionalFieldOf("relations")
                        .forGetter { Optional.ofNullable(it.relations) }
                ).apply(instance) { center, ingredients, count, relations ->
                    Node(
                        center.orElse(null),
                        ingredients,
                        count.orElse(null),
                        relations.orElse(null)
                    )
                }
            }
        }
    }

    data class RemainingItems(
        val items: List<ResultItem>
    ) {
        companion object {
            val CODEC = RecordCodecBuilder.create { instance ->
                instance.group(
                    ResultItem.CODEC.listOf()
                        .fieldOf("items")
                        .forGetter(RemainingItems::items)
                ).apply(instance, ::RemainingItems)
            }
        }
    }

    data class ResultItem(
        val item: String,
        val count: Int = 1
    ) {
        companion object {
            val CODEC = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("item").forGetter(ResultItem::item),
                    Codec.INT.fieldOf("count").forGetter(ResultItem::count)
                ).apply(instance, ::ResultItem)
            }
        }
    }

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    fun parseFromRoot(json: JsonObject): Root {
        return gson.fromJson(json, Root::class.java)
    }

    fun parse(json: JsonObject): Graph {
        return gson.fromJson(json, Graph::class.java)
    }

    fun toStack(itemId: String, count: Int?): ItemStack {
        val item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))
        val countOrOne = count ?: 1
        return ItemStack(item, countOrOne)
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

    fun buildFromJsonFromGraph(json: JsonObject): GraphRecipe {
        val sillyWorkAround = JsonObject()
        sillyWorkAround.add("graph", json)
        return buildFromJson(sillyWorkAround)
    }

    fun buildFromJson(json: JsonObject): GraphRecipe {

        val jsonRoot = parseFromRoot(json)

        val partitions = jsonRoot.graph.partitions
        val result = jsonRoot.graph.result

        // for some really fucked up reason, .parse() can't do namespace:itemwithslash/otherstuff,
        // but .fromNamespaceAndPath() can.... huh???
        val resultId = ResourceLocation.parse(result.item)
        val remainingItems = jsonRoot.graph.remaining // not supported now, unsure if I want to support it? would be good for milk bucket recipes and such...

        val nodes = mutableListOf<ItemNodeVanilla>()

        var centerNodeIndex = -1
        var centerNodePartition = partitions.entries
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
        centerNode.nodeList.addAll(nodes) // oops!
        // I don't even need to read partitions this way...
        makePartitions(centerNode)
        // this is such a bad solution....

        // the resourceLocation is unused but if I put this here maybe one day I will remember to figure it out
        return GraphRecipe(
            id = Hexagony.id("graph_temp_".plus(jsonRoot.hashCode()).plus(resultId.path)),
            centerNode = centerNode,
            result = ItemStack.EMPTY,
            resultInner = toResult(result)
        )
    }


    // i'm such a fucking chuuuud
    fun convertToJson(recipe: GraphRecipe): JsonObject{
        val centerNode = recipe.centerNode
        val partitionsJson = JsonObject()

        // I want to uhh have each node know what it's neighbor is.
        // to do this I need to have them all agree on some index they should have.
        // what the index is doesn't actually matter, it's like a name rather than an actual index, for this.
        val ids = centerNode.nodeList.withIndex().associate { (i, node) ->
            node to (i + 1).toString()
        }

        centerNode.partitions.forEachIndexed { index, partition ->
            val partitionJson = JsonObject()

            partition.forEach { node ->
                val nodeJson = JsonObject()

                if (node === centerNode)
                    nodeJson.addProperty("center", true) // center is an optional field, but I could set the non-center ones to false if I wanted to...

                val ingredients = JsonArray()
                node.validIngredients.forEach {
                    // check if it.item.tostring is enough
                    ingredients.add(BuiltInRegistries.ITEM.getKey(it.item).toString())
                }
                nodeJson.add("validIngredients", ingredients)

                val relations = JsonArray()
                node.neighbors
                    .filter { it in partition }
                    .forEach {
                        relations.add(ids[it])
                    }
                if (relations.size() > 0)
                    nodeJson.add("relations", relations)

                partitionJson.add(ids[node], nodeJson)
            }

            partitionsJson.add("partition_$index", partitionJson)
        }

        val graph = JsonObject()
        graph.add("partitions", partitionsJson)

        val result = JsonObject()
        result.addProperty("item", recipe.resultInner.item.toString())
        result.addProperty("count", recipe.resultInner.count)
        graph.add("result", result)

        // val root = JsonObject()
        // root.add("graph", graph)
        return graph
    }

    fun init() {

    }
}
