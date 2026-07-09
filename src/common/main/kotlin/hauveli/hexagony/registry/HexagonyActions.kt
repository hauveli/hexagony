package hauveli.hexagony.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import hauveli.hexagony.casting.actions.spells.OpCongratulate
import hauveli.hexagony.casting.actions.spells.craft.OpCraft
import hauveli.hexagony.casting.actions.spells.dump.OpDump

object HexagonyActions : HexagonyRegistrar<ActionRegistryEntry>(
    HexRegistries.ACTION,
    { HexActions.REGISTRY },
) {
    // cool!!!!!
    val CRAFT = make("craft", HexDir.NORTH_WEST, "daqedeqadedaqedeqad", OpCraft)
    // cool!!!!!
    val DUMP = make("dump", HexDir.SOUTH_WEST, "wewwwdwqwdwwwew", OpDump)

    // val CONGRATULATE = make("congratulate", HexDir.WEST, "eed", OpCongratulate)

    // val GREAT_CONGRATULATE = make("congratulate/great", HexDir.EAST, "qwwqqqwwqwded", OpCongratulate)

    private fun make(name: String, startDir: HexDir, signature: String, action: Action) =
        make(name, startDir, signature) { action }

    private fun make(name: String, startDir: HexDir, signature: String, getAction: () -> Action) = register(name) {
        ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), getAction())
    }
}
