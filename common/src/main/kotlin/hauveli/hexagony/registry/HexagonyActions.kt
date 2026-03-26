package hauveli.hexagony.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import hauveli.hexagony.casting.actions.spells.OpCongratulate
import hauveli.hexagony.casting.actions.spells.OpMindAnchorPos

object HexagonyActions : HexagonyRegistrar<ActionRegistryEntry>(
    HexRegistries.ACTION,
    { HexActions.REGISTRY },
) {
    val MIND_ANCHOR_POS = make("mind_anchor_pos", HexDir.WEST, "eed", OpMindAnchorPos)

    val GREAT_CONGRATULATE = make("congratulate/great", HexDir.EAST, "qwwqqqwwqwded", OpCongratulate)

    private fun make(name: String, startDir: HexDir, signature: String, action: Action) =
        make(name, startDir, signature) { action }

    private fun make(name: String, startDir: HexDir, signature: String, getAction: () -> Action) = register(name) {
        ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), getAction())
    }
}
