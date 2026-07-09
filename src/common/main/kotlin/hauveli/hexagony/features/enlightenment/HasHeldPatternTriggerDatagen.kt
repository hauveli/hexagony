package hauveli.hexagony.features.enlightenment

import hauveli.hexagony.Hexagony
import hauveli.hexagony.datagen.helpers.GsonBecauseTagsWontLoad
import hauveli.hexagony.registry.HexagonyCriterions
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.HolderLookup
import net.minecraft.data.advancements.AdvancementSubProvider
import java.nio.file.Path
import java.util.Optional
import java.util.function.Consumer


class HasHeldPatternTriggerDatagen(
    private val outputPath: Path,
    private val lookup: HolderLookup.Provider
) : AdvancementSubProvider {

    override fun generate(
        output: HolderLookup.Provider,
        holder: Consumer<AdvancementHolder>) {

        // lol! (i'm coping)
        val tagPath = Path.of(outputPath.parent.parent.parent.parent.toString(),
            "/src/common/main/resources/data/${Hexagony.MODID}/tags/")
        val patterns = GsonBecauseTagsWontLoad.readTags(
            Path.of(tagPath.toString(), "gated_patterns.json"),
            mutableMapOf(),
            tagPath)

        for (pattern in patterns) {

            val filename = pattern.path.replace('/', '_')

            val id = Hexagony.id("gated/${pattern.namespace}/${filename}")

            Advancement.Builder.advancement()
                .addCriterion(
                    "unlock",
                    HexagonyCriterions.HAS_HELD_PATTERN.value.createCriterion(
                        HasHeldPatternTrigger.Conditions(
                            Optional.empty(),
                            pattern
                        )
                    )
                )
                .save(holder, id.toString())
        }
    }
}