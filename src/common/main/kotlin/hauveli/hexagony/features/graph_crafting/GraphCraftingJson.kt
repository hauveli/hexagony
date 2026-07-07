package hauveli.hexagony.features.graph_crafting

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import hauveli.hexagony.features.graph_crafting.GraphCraftingFromNormalRecipes.ItemNodeVanilla
import hauveli.hexagony.features.graph_crafting.GraphCraftingFromNormalRecipes.connectBidirectional
import hauveli.hexagony.features.graph_crafting.GraphCraftingFromNormalRecipes.makePartitions
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import java.util.Optional
import kotlin.collections.iterator

// wip but also todo: properly implement CODEC so I can make an awesome JEI/EMI plugin that shows the crafts
object GraphCraftingJson {
    val PARTITIONS_CODEC =
        Codec.unboundedMap(
            Codec.STRING,
            Codec.unboundedMap(Codec.STRING, Node.CODEC)
        )

    data class Root(
        val partitions: Map<String, Map<String, Node>>,
        val result: ResultItem,
        val remaining: RemainingItems?
    ) {
        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                PARTITIONS_CODEC.fieldOf("partitions").forGetter(Root::partitions),
                ResultItem.CODEC.fieldOf("result").forGetter(Root::result),
                RemainingItems.CODEC.optionalFieldOf("remainingItems")
                    .forGetter { Optional.ofNullable(it.remaining) }
            ).apply(instance) { partitions, result, remaining ->
                Root(partitions, result, remaining.orElse(null))
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
        val count: Int? = 1
    ) {
        companion object {
            val CODEC = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("item").forGetter(ResultItem::item),
                    Codec.INT.optionalFieldOf("count", 1).forGetter(ResultItem::count)
                ).apply(instance, ::ResultItem)
            }
        }
    }

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    fun parse(json: JsonObject): Root {
        return gson.fromJson(json, Root::class.java)
    }

    fun toStack(itemId: String, count: Int): ItemStack {
        val item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))
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

    fun buildFromJson(json: JsonObject): Pair<ItemNodeVanilla, ResourceLocation> {

        val jsonRoot = parse(json)

        val partitions = jsonRoot.partitions
        val resultId = ResourceLocation.parse(jsonRoot.result.item)
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
