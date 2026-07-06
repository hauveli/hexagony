package hauveli.hexagony.datagen

import hauveli.hexagony.features.enlightenment.HasHeldPatternTriggerDatagen
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.advancements.AdvancementProvider
import java.util.concurrent.CompletableFuture


class HexagonyAdvancements(
    output: PackOutput,
    provider: CompletableFuture<HolderLookup.Provider>
) : AdvancementProvider(output, provider, listOf(
    HasHeldPatternTriggerDatagen(output.outputFolder, provider.get())
))