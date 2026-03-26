package hauveli.hexagony.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import hauveli.hexagony.casting.actions.spells.OpCongratulate
import hauveli.hexagony.casting.actions.spells.OpMindAnchorPosition
import hauveli.hexagony.casting.actions.spells.OpMindAnchorPowered
import hauveli.hexagony.casting.actions.spells.OpMindAnchorSignalStrength

object HexagonyActions : HexagonyRegistrar<ActionRegistryEntry>(
    HexRegistries.ACTION,
    { HexActions.REGISTRY },
) {
    val MIND_ANCHOR_POS = make("mind_anchor/pos", HexDir.WEST, "qaqqaeeqe", OpMindAnchorPosition)

    val MIND_ANCHOR_POWER = make("mind_anchor/state", HexDir.WEST, "qaqqaeeq", OpMindAnchorPowered)

    val MIND_ANCHOR_SIGNAL = make("mind_anchor/signal", HexDir.WEST, "qaqqaee", OpMindAnchorSignalStrength)

    val GREAT_CONGRATULATE = make("congratulate/great", HexDir.EAST, "qwwqqqwwqwded", OpCongratulate)

    private fun make(name: String, startDir: HexDir, signature: String, action: Action) =
        make(name, startDir, signature) { action }

    private fun make(name: String, startDir: HexDir, signature: String, getAction: () -> Action) = register(name) {
        ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), getAction())
    }
}
