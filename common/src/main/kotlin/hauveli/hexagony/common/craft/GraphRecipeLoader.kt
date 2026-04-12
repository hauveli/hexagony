package hauveli.hexagony.common.craft

import com.google.common.graph.GraphBuilder
import com.google.gson.Gson
import com.google.gson.JsonElement
import hauveli.hexagony.Hexagony
import hauveli.hexagony.common.craft.GraphCraftingJson.GraphJson
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.item.crafting.Recipe

object GraphRecipeLoader {

    fun loadAll() {
        println("Loading graph recipes manually...")
        val folder = "graph_recipes"

        // todo: not hardcode this
        val files = listOf(
            "/data/${Hexagony.MODID}/$folder/mind_anchor.json"
        )

        // todo: support for shapeless recipes?
        // not even sure if that would be needed, it should be possible
        // to register a big shapeless recipe and have it work so I think I won't do that...
        for (path in files) {
            try {
                val input = javaClass.getResourceAsStream(path)
                    ?: throw RuntimeException("File not found: $path")
                val json = input.bufferedReader().use { it.readText() }
                val graphJson = Gson().fromJson(json, GraphJson::class.java)
                val (centerNode, resultId) = GraphCraftingJson.buildFromJson(graphJson)
                val recipe = GraphRecipe(resultId, centerNode, resultId)
                // technically, this should probably be its own recipe type, as it is shaped, but not 2D.
                GraphCraftingRecipes.shapedRecipes.add(Pair(recipe, centerNode))
                println("Loaded graph recipe: $path")

            } catch (e: Exception) {
                println(e)
            }
        }
    }
}
