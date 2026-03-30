package hauveli.hexagony.common.craft

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

class GraphRecipes {
    data class RecipeNode(
        val item: Item,
        val count: Int = 1,
        val neighbors: MutableList<RecipeNode> = mutableListOf()
    )

    class GraphRecipe(val root: RecipeNode) {
        fun allNodes(): List<RecipeNode> {
            val visited = mutableSetOf<RecipeNode>()
            val result = mutableListOf<RecipeNode>()
            fun dfs(node: RecipeNode) {
                if (!visited.add(node)) return
                result.add(node)
                node.neighbors.forEach { dfs(it) }
            }
            dfs(root)
            return result
        }

        fun matches(nodes: List<GraphCrafting.ItemNode>): Boolean {
            val recipeNodes = allNodes()
            if (nodes.size != recipeNodes.size) return false

            // Basic mapping: each recipe node must have a matching ItemNode
            return recipeNodes.all { rNode ->
                nodes.any { iNode ->
                    iNode.stack.item == rNode.item && iNode.stack.count >= rNode.count
                }
            }

            // TODO: Extend to check graph structure (neighbors) if needed
        }
    }


}