package hauveli.hexagony.common.craft

import hauveli.hexagony.common.craft.GraphCrafting.ItemNode
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.pow

/*
    I couldn't be bothered figuring out how to construct a crafting system with integer spacing in an n-dimensional grid
    instead, I think just using distance-based nodes is easier to code, so I'm doing that.
    Bonus feature: it's also easily made n-dimensional, if minecraft ever adds higher dimensions!
 */
object GraphCraftingRecipes {
    // recipes here is what matters, and init
    lateinit var recipes: MutableList<Pair<Recipe<*>, ItemNodeVanilla>>

    fun init(level: ServerLevel) {
        val manager = level.recipeManager
        val list = mutableListOf<Pair<Recipe<*>, ItemNodeVanilla>>()

        manager.recipes.forEach { recipe ->
            if (recipe is CraftingRecipe) {
                val centerNode = fromCraftingRecipe(recipe)
                if (centerNode != null) {
                    list.add(Pair(recipe, centerNode))
                }
            }
        }
        recipes =  list
    }

    class ItemNodeVanilla(
        val validIngredients: Array<ItemStack>,
        val pos: Vec3, // huh...? actually yeah beacuse it's useful to keep for checking stuff so why not
        val neighbors: MutableList<ItemNodeVanilla> = mutableListOf(),
        val nodeList: MutableList<ItemNodeVanilla> = mutableListOf(),
        val partitions: MutableList<Set<ItemNodeVanilla>> = mutableListOf(),
        val orientation: Vec3
    )

    fun calculateOrientation(rootNode: ItemNodeVanilla) {
        /*
            I think this should work for determining orientation?
            committing now because tummy ache

            Ex:   -> (1,0,0) + (1,0,1) + (1,0,2) = (3,0,3)
                  -> (0,0,0) + (0,0,1) + (0,0,2) = (0,0,3)
            _ X _
            _ X _
            _ X _

            Ex:   -> (0,0,2) + (1,0,1) + (2,0,0) = (3,0,3)
                  -> (0,0,0) + (1,0,-1) + (2,0,-2) = (3,0,-3)
            _ _ X
            _ X _
            X _ _

            Ex:   -> (0,0,1) + (1,0,1) + (2,0,1) = (3,0,3)
                  -> (0,0,0) + (1,0,0) + (2,0,0) = (3,0,0)
            _ _ _
            X X X
            _ _ _
         */
        var summedPositions = Vec3.ZERO
        for (node in rootNode.nodeList) {
            summedPositions = summedPositions.add(node.pos.subtract(rootNode.pos))
        }

    }

    fun distanceSquared(a: Vec3, b: Vec3): Double {
        // if the game ever adds more dimensions, this is where I'd want to change it...
        val dx = (a.x - b.x)
        val dy = (a.y - b.y)
        val dz = (a.z - b.z)
        // Vector4d(dx, dy, dz, 1.0).lengthSquared()
        return dx.pow(2) + dy.pow(2) + dz.pow(2) // don't care about euclidean distance
    }

    fun findCenter(items: List<ItemNodeVanilla>): ItemNodeVanilla? {
        if (items.isEmpty()) return null
        val avgX = items.map { it.pos.x }.average()
        val avgY = items.map { it.pos.y }.average()
        val avgZ = items.map { it.pos.z }.average()
        // val avgW = items.map { it.w }.average()
        // arithmetic mean
        // I do kind of wonder how the compiler sees this,
        // it has enough information to know it could simply re-use the 3 variables instead of making a new vector...
        // but this is more readable so I'm doing it this way.
        val centroid = Vec3(avgX,avgY,avgZ)
        return items.minByOrNull {
            distanceSquared(it.pos, centroid)
        }
    }

    // tolerance for equidistant nodes, ugh I didn't consider this at first
    private const val EPSILON = 1e-4

    fun connectBidirectional(a: ItemNodeVanilla, b: ItemNodeVanilla) {
        if (!a.neighbors.contains(b)) {
            a.neighbors.add(b)
        }
        if (!b.neighbors.contains(a)) {
            b.neighbors.add(a)
        }
    }

    fun connectNearest(nodes: List<ItemNodeVanilla>) {
        for (node in nodes) {

            var minDist = Double.MAX_VALUE
            val nearest = mutableListOf<ItemNodeVanilla>()

            for (other in nodes) {
                if (node === other) continue

                val dist = node.pos.distanceToSqr(other.pos)

                // when it finds the best option any equidistant points will be kept
                when {
                    dist + EPSILON < minDist -> {
                        minDist = dist
                        nearest.clear()
                        nearest.add(other)
                    }

                    abs(dist - minDist) < EPSILON -> {
                        nearest.add(other)
                    }
                }
            }

            for (n in nearest) {
                connectBidirectional(node, n)
            }
        }
    }

    fun fromCraftingRecipe(recipe: CraftingRecipe): ItemNodeVanilla? {
        // First construct a pretend layout of the items as they would be in a crafting table (in worldspace)
        val ingredients = recipe.ingredients
        val width = (recipe as? ShapedRecipe)?.width ?: 0
        val height = (recipe as? ShapedRecipe)?.height ?: 0

        val nodes = mutableListOf<ItemNodeVanilla>()

        var index = 0 // never exceeds height*width
        for (y in 0 until height) {
            for (x in 0 until width) {
                val ingredient = ingredients[index]
                index++
                // skip empty
                if (ingredient.test(ItemStack.EMPTY)) continue
                if (recipe.id.path.equals("iron_block")) println(ingredient.items.toString())
                // all valid ingredients
                val matchStack = ingredient.items
                val pos = Vec3(x.toDouble(), 0.0, y.toDouble())
                nodes.add(ItemNodeVanilla(matchStack, pos))
            }
        }

        // Now make the graph as before

        // empty list to keep track of them later
        //val connected = mutableSetOf<ItemNodeVanilla>()

        // get center entity, then use this entity to get the centerNode from nodes
        val centerNode = findCenter(nodes) ?: return null // should only be possible if empty? might be better to do !!
        // centerNode is referenced in nodes
        connectNearest(nodes)

        centerNode.nodeList.addAll(nodes)

        makePartitions(centerNode)

        return centerNode
    }

    fun nodesAreEqual(rootWorld: ItemNode, rootRecipe: ItemNodeVanilla) : Boolean {
        if (rootWorld.neighbors.size != rootRecipe.neighbors.size) return false
        if (!rootRecipe.validIngredients.any( {ingredient -> ItemStack.isSameItemSameTags(rootWorld.stack, ingredient)})) return false
        return true
    }

    fun neighborhoodIsEqual(
        nodeWorld: ItemNode,
        nodeRecipe: ItemNodeVanilla,
        visited: MutableSet<Pair<ItemNode, ItemNodeVanilla>> = mutableSetOf()
    ): Boolean {

        if (!visited.add(nodeWorld to nodeRecipe)) {
            return true // already validated this pair
        }

        if (!nodesAreEqual(nodeWorld, nodeRecipe)) return false

        val unmatchedRecipeNeighbors = nodeRecipe.neighbors.toMutableList()

        for (worldNeighbor in nodeWorld.neighbors) {
            val match = unmatchedRecipeNeighbors.firstOrNull {
                nodesAreEqual(worldNeighbor, it) &&
                        neighborhoodIsEqual(worldNeighbor, it, visited)
            } ?: return false

            unmatchedRecipeNeighbors.remove(match)
        }

        return true
    }

    private fun atLeastMatchesRecipe(worldItemNode: ItemNode, recipeItemNode: ItemNodeVanilla): Boolean {
        val consumedMatches = mutableListOf<Set<ItemNode>>()
        for (recipePartition in recipeItemNode.partitions) {
            // continue comes back here, note to self in future
            for (worldPartition in worldItemNode.partitions) {
                if (partitionsEqual(worldPartition, recipePartition)) {
                    if (!consumedMatches.contains(worldPartition)) {
                        consumedMatches.add(worldPartition)
                        continue // next worldPartition right away
                    }
                }
            }
        }
        if (consumedMatches.count() == recipeItemNode.partitions.count()) {
            worldItemNode.matchingPartitions.addAll(consumedMatches)
            return true
        }
        return false
    }

    private fun partitionsEqual(itemNodePartition: Set<ItemNode>, recipeNodePartition: Set<ItemNodeVanilla>): Boolean {
        if (itemNodePartition.size < recipeNodePartition.size) return false // abort if recipe is larger

        val unmatched: MutableList<ItemNodeVanilla> = ArrayList(recipeNodePartition)

        for (nodeA in itemNodePartition) {
            var matched = false

            val iterator: MutableIterator<ItemNodeVanilla> = unmatched.iterator()
            while (iterator.hasNext()) {
                val nodeB: ItemNodeVanilla = iterator.next()

                if (nodesAreEqual(nodeA, nodeB)) {
                    iterator.remove()
                    matched = true
                    break
                }
            }

            if (!matched) return false
        }

        return true
    }

    // TODO: here, get the partition
    // Note: the important point is that for each partition in the RECIPE, there is at least one matching set of partitions
    // in the world....
    private fun makePartitions(rootNode: ItemNodeVanilla) {
        val visited = mutableSetOf<ItemNodeVanilla>()

        for (node in rootNode.nodeList) {
            if (node in visited) continue

            val component = mutableSetOf<ItemNodeVanilla>()
            explore(node, visited, component)
            rootNode.partitions.add(component)
        }
    }

    // recursively explore directly connected graphs
    private fun explore(
        node: ItemNodeVanilla,
        visited: MutableSet<ItemNodeVanilla>,
        component: MutableSet<ItemNodeVanilla>
    ) {
        if (!visited.add(node)) return

        component.add(node)

        for (neighbor in node.neighbors) {
            explore(neighbor, visited, component)
        }
    }

    fun matchGraphs(
        worldRoot: ItemNode,
        recipeRoot: ItemNodeVanilla
    ): Boolean {

        val equal = neighborhoodIsEqual(worldRoot, recipeRoot)
        if (equal) {
            val partitionsEqualToo = atLeastMatchesRecipe(worldRoot, recipeRoot)
            return partitionsEqualToo
        }
        println("returning false I guess")

        return false
    }

    fun matchRecipe(entities: List<ItemEntity>, orientation: Vec3): Pair<Recipe<*>?,ItemNode> {
        val worldGraph = GraphCrafting.buildGraph(entities)
        for (recipe in recipes) {
            if ( matchGraphs(worldGraph, recipe.second) ) {
                return Pair(recipe.first, worldGraph)
            }
        }
        return Pair(null, worldGraph)
    }
}