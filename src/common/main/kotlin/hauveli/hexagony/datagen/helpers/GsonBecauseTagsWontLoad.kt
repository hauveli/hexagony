package hauveli.hexagony.datagen.helpers

import com.google.gson.JsonParser
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.pathString

object GsonBecauseTagsWontLoad {


    fun readTags(
        pathToFile: Path,
        cache: MutableMap<ResourceLocation, List<ResourceLocation>>,
        tagPath: Path
    ): List<ResourceLocation> {
        val json = Files.newBufferedReader(pathToFile).use {
            JsonParser.parseReader(it).asJsonObject
        }
        val values = json.getAsJsonArray("values")
        val result = mutableListOf<ResourceLocation>()

        for (element in values) {
            val id = element.asString
            if (id.startsWith("#")) {

                val tagId = ResourceLocation.tryParse(id.substring(1)) // note to self: removing the #
                    ?: continue
                val resolved = cache[tagId] ?: run {
                    val newPath = resolveTagPath(tagId, tagPath)
                    val computed = readTags(newPath, cache, tagPath)
                    cache[tagId] = computed
                    computed
                }

                result.addAll(resolved)
            }
            else {
                ResourceLocation.tryParse(id)?.let(result::add)
            }
        }

        return result
    }

    fun resolveTagPath(tagId: ResourceLocation, tagPath: Path): Path {
        return Path.of(tagPath.pathString, tagId.path)
    }
}