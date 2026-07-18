package hauveli.hexagony.features.graph_crafting.emi

import dev.emi.emi.api.widget.WidgetHolder
import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff
import hauveli.hexagony.interop.ModifiedSlotWidget
import net.minecraft.world.phys.Vec3
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

object SpringyBoingBoing {
    data class EmiNode(
        val node: GraphCraftingRecipeStuff.ItemNodeVanilla,
        val entity: GraphCraftingEmiStack,
        val partition: Int,

        var pos: Vec3,
        var vel: Vec3 = Vec3.ZERO
    )

    private const val ORBIT_RADIUS = 1.0
    private const val INITIAL_RADIUS = 1.0

    private const val ITERATIONS = 100

    // todo: determine optimal spring-length based on number of items? this becomes a problem to display in EMI otherwise, at very large partitions,
    // or very large number of partitions
    private const val SPRING_LENGTH = 20.0
    private const val SPRING_STRENGTH = 0.05

    private const val REPULSION = 5.0
    private const val REPULSION_RADIUS = SPRING_LENGTH * 1.25 // this needs to be a little bit bigger than teh spring length to keep things from being equidistant when they shouldn't be

    private const val DAMPING = 0.8

    private const val CAMERA_DISTANCE = 10f
    private const val SLOT_SCALE = 2.5f

    // This could really probably just be done using random noise or a grid... I think?
    private fun initialLayout(
        centerNode: GraphCraftingRecipeStuff.ItemNodeVanilla,
        entities: List<GraphCraftingEmiStack>
    ): List<EmiNode> {
        val partitionLookup = buildMap {
            centerNode.partitions.forEachIndexed { partitionIndex, partition ->
                partition.forEach { node ->
                    put(node, partitionIndex)
                }
            }
        }

        return entities.mapIndexed { index, entity ->
            val theta = Math.PI * 2 * index / entities.size
            val phi = acos(1 - 2.0 * (index + 0.5) / entities.size)

            val node = centerNode.nodeList[index]

            EmiNode(
                node = node,
                entity = entity,
                partition = partitionLookup[node] ?: -1,
                pos = Vec3(
                    INITIAL_RADIUS * sin(phi) * cos(theta),
                    INITIAL_RADIUS * cos(phi),
                    INITIAL_RADIUS * sin(phi) * sin(theta)
                )
            )
        }
    }

    private fun clusterPartitions(
        centerNode: GraphCraftingRecipeStuff.ItemNodeVanilla,
        lookup: Map<GraphCraftingRecipeStuff.ItemNodeVanilla, EmiNode>
    ) {
        centerNode.partitions.forEachIndexed { index, partition ->

            val theta = 2.0 * Math.PI * index / centerNode.partitions.size

            val c = cos(theta)
            val s = sin(theta)

            val orbitX = ORBIT_RADIUS * c
            val orbitY = ORBIT_RADIUS * s

            val partitionNodes = partition.mapNotNull(lookup::get)

            if (partitionNodes.isEmpty()) return@forEachIndexed

            val clusterCenter = partitionNodes
                .map { it.pos }
                .reduce(Vec3::add)
                .scale(1.0 / partitionNodes.size)

            partitionNodes.forEach { node ->

                val relative = node.pos.subtract(clusterCenter)

                val rx = relative.x * c - relative.y * s
                val ry = relative.x * s + relative.y * c

                node.pos = Vec3(
                    orbitX + rx,
                    orbitY + ry,
                    node.pos.z
                )
            }
        }
    }

    private fun springForce(a: EmiNode, b: EmiNode): Vec3 {
        val delta = b.pos.subtract(a.pos)
        val dist = delta.length()

        if (dist <= 1e-4) {
            return Vec3.ZERO
        }

        val forceMagnitude = (dist - SPRING_LENGTH) * SPRING_STRENGTH
        return delta.scale(forceMagnitude / dist)
    }

    private fun repulsionForce(a: EmiNode, b: EmiNode): Vec3 {
        val delta = a.pos.subtract(b.pos)
        val distSq = delta.lengthSqr()

        if (distSq >= REPULSION_RADIUS * REPULSION_RADIUS) {
            return Vec3.ZERO
        }

        return delta.normalize().scale(
            REPULSION / distSq.coerceAtLeast(1e-4)
        )
    }

    private fun simulate(
        nodes: List<EmiNode>,
        lookup: Map<GraphCraftingRecipeStuff.ItemNodeVanilla, EmiNode>
    ) {
        repeat(ITERATIONS) {

            for (a in nodes) {
                var force = Vec3.ZERO

                for (neighbor in a.node.neighbors) {
                    lookup[neighbor]?.let {
                        force = force.add(springForce(a, it))
                    }
                }

                for (b in nodes) {
                    if (a !== b) {
                        force = force.add(repulsionForce(a, b))
                    }
                }

                a.vel = a.vel.add(force).scale(DAMPING)
            }

            for (node in nodes) {
                node.pos = node.pos.add(node.vel)
            }
        }
    }

    // I should have named my stuff better, centerNode is the "center-most item" and this returns the center of mass, if each item weighed the same amount
    private fun getNodeCoordinateCenter(nodes: List<EmiNode>): Vec3 {
        if (nodes.isEmpty()) return Vec3.ZERO // probably can't happen but just in case

        return nodes
            .map { it.pos }
            .reduce(Vec3::add)
            .scale(1.0 / nodes.size)
    }

    private fun moveBackToCorner(
        nodes: List<EmiNode>
    ) {
        if (nodes.isEmpty()) return

        val minX = nodes.minOf { it.pos.x }
        val minY = nodes.minOf { it.pos.y }
        val minZ = nodes.minOf { it.pos.y }

        for (node in nodes) {
            node.pos = node.pos.subtract(
                Vec3(
                    minX,
                    minY,
                    minZ
                )
            )
        }
    }

    private fun centerNodesOnCorner(
        nodes: List<EmiNode>
    ) {
        moveBackToCorner(nodes)

        val graphCenter = getNodeCoordinateCenter(nodes)

        for (node in nodes) {
            node.pos = node.pos.subtract(graphCenter)
        }
    }

    private fun draw(
        widgets: WidgetHolder,
        nodes: List<EmiNode>
    ) {
        for (node in nodes) {
            val depth = (CAMERA_DISTANCE - node.pos.z).coerceAtLeast(0.1)
            val scale = CAMERA_DISTANCE / depth

            // Uhhh I have to draw something between these nodes and their neighbors (alt. edges) to make it make sense more easily
            // so I'll just draw a glowing
            widgets.add(
                ModifiedSlotWidget(
                    node.entity,
                    node.partition,
                    node.pos.x,
                    node.pos.y,
                    node.pos.z,
                    SLOT_SCALE * scale.toFloat() // how big the items themselves are, not how big the partitions are
                )
            )
        }
    }

    // this only runs once per clicking the recipe in emi so it should be fine to do some nonsense?
    fun boing(
        widgets: WidgetHolder,
        centerNode: GraphCraftingRecipeStuff.ItemNodeVanilla,
        inputEntities: List<GraphCraftingEmiStack>
    ) {
        val nodes = initialLayout(centerNode, inputEntities)
        val lookup = nodes.associateBy { it.node }

        clusterPartitions(centerNode, lookup)

        simulate(nodes, lookup)

        centerNodesOnCorner(nodes)

        draw(widgets, nodes)
    }
}