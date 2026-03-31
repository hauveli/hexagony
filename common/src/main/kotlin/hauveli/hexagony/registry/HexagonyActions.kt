package hauveli.hexagony.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import hauveli.hexagony.casting.actions.spells.control.*
import hauveli.hexagony.casting.actions.spells.OpCongratulate
import hauveli.hexagony.casting.actions.spells.trepannation.OpMindAnchorPosition
import hauveli.hexagony.casting.actions.spells.trepannation.OpMindAnchorPowered
import hauveli.hexagony.casting.actions.spells.trepannation.OpMindAnchorSignalStrength
import hauveli.hexagony.casting.actions.spells.control.OpJumpButWithYourFeet
import hauveli.hexagony.casting.actions.spells.craft.OpCraft


object HexagonyActions : HexagonyRegistrar<ActionRegistryEntry>(
    HexRegistries.ACTION,
    { HexActions.REGISTRY },
) {
    // cool!!!!!
    val CRAFT = make( "craft", HexDir.NORTH_WEST,"daqedeqadedaqedeqad", OpCraft)


    // With a mind anchor, create attaches player to their body
    val CREATE = make( "create", HexDir.NORTH_WEST,"edeaqqwwawwqq", OpCreateFakeplayer)
    // With a mind anchor, destroy detaches player from their body (astral projection)
    // While astral projecting, the body can still be controlled independently.
    // However, targeting the playerEntity will teleport and affect their astral body, NOT their physical body.
    val DESTROY = make( "destroy", HexDir.NORTH_WEST,"edeaqqwadawqq", OpDestroyFakeplayer)

    val STOP_ALL = make( "move/stopall", HexDir.NORTH_WEST,"edeaqqwqqwqq", OpStopAll)

    val LOOK = make( "move/look", HexDir.NORTH_WEST,"edeaqwa", OpMadeYouLook)

    val WALK = make("move/walk", HexDir.NORTH_WEST, "edeqd", OpWalkAMileInTheseLouisVuittons)

    val STRAFE = make( "move/strafe", HexDir.NORTH_WEST,"edewa", OpStrafeInAforementionedLouisVuittons)

    val JUMP = make("move/jump", HexDir.NORTH_WEST, "edeqda", OpJumpButWithYourFeet)

    val SNEAK = make("move/sneak", HexDir.NORTH_WEST, "edeade", OpSetSneak)
    val SPRINT = make( "move/sprint", HexDir.NORTH_WEST,"edeaqad", OpSetSprint)

    val USE = make( "move/use", HexDir.NORTH_WEST,"edeaqwaaq", OpUseItem)
    val ATTACK = make("move/attack", HexDir.NORTH_WEST, "edeaqedde", OpAttack)

    val HOTBAR = make("move/hotbar", HexDir.NORTH_WEST, "edeawq", OpSelectHotbar)
    val SWAP_HANDS = make("move/swap_hands", HexDir.NORTH_WEST, "edeawqa", OpSwapHands)
    val DROP = make("move/drop", HexDir.NORTH_WEST, "edeaqe", OpDropAndRoll)



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
