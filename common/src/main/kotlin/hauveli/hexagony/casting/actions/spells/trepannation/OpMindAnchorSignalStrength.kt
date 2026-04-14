package hauveli.hexagony.casting.actions.spells.trepannation

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import hauveli.hexagony.common.mind_anchor.MindAnchorManager.getSignalStrength

object OpMindAnchorSignalStrength : ConstMediaAction  {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = env.castingEntity
        if (target != null) {
            env.assertEntityInRange(target)
            val strength = getSignalStrength(target.uuid)
            if (strength != null) {
                return listOf(DoubleIota(strength))
            }
        }
        return listOf(GarbageIota())
    }
}
