package hauveli.hexagony.common.craft

import hauveli.hexagony.common.craft.GraphCrafting.ItemNode
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.phys.Vec3
import kotlin.math.pow

/*
    I couldn't be bothered figuring out how to construct a crafting system with integer spacing in an n-dimensional grid
    instead, I think just using distance-based nodes is easier to code, so I'm doing that.
    Bonus feature: it's also easily made n-dimensional, if minecraft ever adds higher dimensions!
 */
object GraphCraftingRuntimeImport {
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
                    if (recipe.id.path.equals("iron_block")) {
                        println(centerNode.validIngredients.toList().toString())
                        println(centerNode.neighbors.toList().toString())
                        println(centerNode.neighbors.size)
                    }
                }
            }
        }
        recipes =  list
    }

    class ItemNodeVanilla(
        val validIngredients: Array<ItemStack>,
        val pos: Vec3,
        val neighbors: MutableList<ItemNodeVanilla> = mutableListOf()
    )

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

                    kotlin.math.abs(dist - minDist) < EPSILON -> {
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

        return centerNode
    }

    fun matchesIngredient(stack: ItemStack, valid: Array<ItemStack>): Boolean {
        return valid.any { recipeStack ->
            ItemStack.isSameItemSameTags(stack, recipeStack)
        }
    }

    fun matchNode(
        world: ItemNode, // items in the world use this
        recipe: ItemNodeVanilla, // recipes use  this
        visitedWorld: MutableSet<ItemNode> = mutableSetOf(),
        visitedRecipe: MutableSet<ItemNodeVanilla> = mutableSetOf()
    ): Boolean {
        // recursion oopsie avoidance
        if (!visitedWorld.add(world)) return false
        if (!visitedRecipe.add(recipe)) return false

        // must have at least one match in valid ingredients
        if (!matchesIngredient(world.stack, recipe.validIngredients))
            return false

        // neighbor count must match
        if (world.neighbors.size != recipe.neighbors.size)
            return false

        // match each recipe neighbor to a world neighbor
        val unmatchedWorldNeighbors = world.neighbors.toMutableList()

        for (recipeNeighbor in recipe.neighbors) {
            val match = unmatchedWorldNeighbors.firstOrNull { worldNeighbor ->
                matchNode(
                    worldNeighbor,
                    recipeNeighbor,
                    visitedWorld.toMutableSet(),
                    visitedRecipe.toMutableSet()
                )
            } ?: return false

            unmatchedWorldNeighbors.remove(match)
        }

        return true
    }

    fun matchRecipe(entities: List<ItemEntity>): Recipe<*>? {
        val worldGraph = GraphCrafting.buildGraph(entities)
        for (recipe in recipes) {
            if (matchNode(worldGraph, recipe.second) ) return recipe.first
        }
        return null
    }
}