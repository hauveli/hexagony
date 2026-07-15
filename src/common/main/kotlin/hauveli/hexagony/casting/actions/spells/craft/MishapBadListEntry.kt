package hauveli.hexagony.casting.actions.spells.craft

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.casting.mishaps.MishapUnescapedValue
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.common.casting.arithmetic.operator.list.OperatorReplace
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import hauveli.hexagony.Hexagony
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3
// todo
// should likely use InvalidIota color (dark gray)


// I'm not sure if I want InvalidIota or an UnescapedValue-like or this...
// but this is a bit funny and novel, so I'm sticking with it for now, hehe...
/**
 * The value failed some kind of predicate.
 */
class MishapBadListEntry(
    val badList: Iota,
    val perpetratorIndex: Int,
    val expected: Component
) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.GRAY)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        // todo: garbify the element in the list because it would be funny
        /*
        OperatorReplace.apply(
            // [list, num, any] so I want uhhh the entityList, the index and then garbage.
            // so I want to push index and garbage to the stack first
        )
        val list = perpetrator as ListIota
        val tempList = mutableListOf<Iota>()
        for (iota in list.list) {
            tempList.addLast(iota)
        }
        tempList[index] = GarbageIota()

         */

        (badList as ListIota).list.minusElement(badList.list[perpetratorIndex])
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component {
        val perp = (badList as ListIota).list[perpetratorIndex].display()

        return error(
            "${Hexagony.MODID}.bad_list_entry", expected, perpetratorIndex,
            perp, badList.display()
        )
    }

    companion object {
        @JvmStatic
        fun ofType(perpetrator: Iota, perpetratorIndex: Int, name: String): MishapBadListEntry {
            return of(perpetrator, perpetratorIndex, "class.$name")
        }

        @JvmStatic
        fun of(perpetrator: Iota, perpetratorIndex: Int, name: String, vararg translations: Any): MishapBadListEntry {
            val key = "${Hexagony.MODID}.mishap.bad_list_entry.$name"
            return MishapBadListEntry(perpetrator, perpetratorIndex, key.asTranslatedComponent(*translations))
        }
    }
}