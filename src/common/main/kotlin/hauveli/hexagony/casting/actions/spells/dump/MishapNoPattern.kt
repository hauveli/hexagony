package hauveli.hexagony.casting.actions.spells.dump

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import hauveli.hexagony.Hexagony
import net.minecraft.world.item.DyeColor

import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.utils.TreeList

class MishapNoPattern : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.ORANGE)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        stack.add(GarbageIota())
        /*
        val pat = errorCtx.pattern
        if (pat != null) {
            stack.add(PatternIota(HexPattern.fromAngles(pat.anglesSignature(), pat.startDir)))
        }
        */
        return stack
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("${Hexagony.MODID}.needs_patterns")
}