package hauveli.hexagony.casting.actions.spells.freecam

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

class MishapNotFreecam : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BLUE) // I think? I really can't quite figure out what to do with these

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        stack.add(GarbageIota())
        /*
        val pat = errorCtx.pattern
        if (pat != null) {
            stack.add(PatternIota(HexPattern.fromAngles(pat.anglesSignature(), pat.startDir)))
        }
        */
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("${Hexagony.MODID}.not_freecam")
}