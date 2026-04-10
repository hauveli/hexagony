package hauveli.hexagony.common.craft

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

object GraphCrafting {

    class ItemNode(
        val entity: ItemEntity,
        val pos: Vec3,
        val stack: ItemStack,
        val neighbors: MutableList<ItemNode> = mutableListOf(),
        val nodeList: MutableList<ItemNode> = mutableListOf()
    )

    fun distanceSquared(a: Vec3, b: Vec3): Double {
        // if the game ever adds more dimensions, this is where I'd want to change it...
        val dx = (a.x - b.x)
        val dy = (a.y - b.y)
        val dz = (a.z - b.z)
        // Vector4d(dx, dy, dz, 1.0).lengthSquared()
        return dx.pow(2) + dy.pow(2) + dz.pow(2) // don't care about euclidean distance
    }

    fun findCenter(items: List<ItemEntity>): ItemEntity? {
        if (items.isEmpty()) return null
        val avgX = items.map { it.x }.average()
        val avgY = items.map { it.y }.average()
        val avgZ = items.map { it.z }.average()
        // val avgW = items.map { it.w }.average()
        // arithmetic mean
        // I do kind of wonder how the compiler sees this,
        // it has enough information to know it could simply re-use the 3 variables instead of making a new vector...
        // but this is more readable so I'm doing it this way.
        val centroid = Vec3(avgX,avgY,avgZ)
        return items.minByOrNull {
            val p = it.position() // .blockPosition() also exists... could be cool?
            distanceSquared(p, centroid)
        }
    }


    // tolerance for equidistant nodes, ugh I didn't consider this at first
    private const val EPSILON = 1e-4

    fun connectBidirectional(a: ItemNode, b: ItemNode, visualize: Boolean) {
        if (!a.neighbors.contains(b)) {
            a.neighbors.add(b)
        }
        if (!b.neighbors.contains(a)) {
            b.neighbors.add(a)
        }
        if (visualize) {
            val level = a.entity.level() as ServerLevel
            drawLine(level, a.pos, b.pos)
        }
    }

    fun connectNearest(nodes: List<ItemNode>, visualize: Boolean) {
        for (node in nodes) {

            var minDist = Double.MAX_VALUE
            val nearest = mutableListOf<ItemNode>()

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
                connectBidirectional(node, n, visualize)
            }
        }
    }

    // Builds the graph from a list of itemEntities, then returns the
    fun buildGraph(items: List<ItemEntity>, visualize: Boolean = false): ItemNode {
        // make unconnected graph of all Itementities as node
        val nodes = items.map { ItemNode(it, it.position(), it.item.copy()) }.toMutableList()

        // get center entity, then use this entity to get the centerNode from nodes
        val centerEntity = findCenter(items) ?: return nodes[0] // should only be possible if empty? might be better to do !!
        val centerNode = nodes.first { it.entity == centerEntity }

        connectNearest(nodes, visualize)

        centerNode.nodeList.addAll(nodes)

        println("visualize: ${visualize}")
        if (visualize)
            drawBall(
                centerNode.entity.level() as ServerLevel,
                centerNode.pos,
                0.25
            )

        println(centerNode.neighbors.size)
        println(centerNode.stack.displayName.toString())

        return centerNode
    }

    fun drawBall(level: ServerLevel, origin: Vec3, radius: Double) {
        val density = 25
        for (i in 0..density) {
            val randomDirection = Vec3.directionFromRotation(Random.nextFloat(), Random.nextFloat()).scale(radius)
            level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                origin.x, origin.y, origin.z,
                3,   // count
                randomDirection.x, randomDirection.y, randomDirection.z,
                0.1  // speed
            );
        }
    }

    fun drawLine(level: ServerLevel, start: Vec3, end: Vec3) {
        val steps = (start.distanceTo(end) * 5).roundToInt()

        println("drawing lines!")
        for (i in 0..steps) {
            val t = i.toDouble() / steps.toDouble()

            val x = start.x + (end.x - start.x) * t
            val y = start.y + (end.y - start.y) * t
            val z = start.z + (end.z - start.z) * t

            level.sendParticles(
                ParticleTypes.END_ROD,
                x, y, z,
                1,   // count
                (0.5-Random.nextDouble())*0.1, (0.5-Random.nextDouble())*0.1, (0.5-Random.nextDouble())*0.1,
                0.0  // speed
            );
        }
    }

    /*
    fun createExampleRecipe(): GraphRecipes.GraphRecipe {
        val center = GraphRecipes.RecipeNode(Items.DIAMOND)
        val left = GraphRecipes.RecipeNode(Items.IRON_INGOT)
        val right = GraphRecipes.RecipeNode(Items.GOLD_INGOT)

        // Connect nodes to form a mini graph
        center.neighbors.add(left)
        center.neighbors.add(right)
        left.neighbors.add(center)
        right.neighbors.add(center)

        return GraphRecipes.GraphRecipe(center)
    }
    */
}