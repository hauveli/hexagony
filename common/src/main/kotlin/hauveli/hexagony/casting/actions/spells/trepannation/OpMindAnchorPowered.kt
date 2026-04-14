package hauveli.hexagony.casting.actions.spells.trepannation

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import hauveli.hexagony.common.mind_anchor.MindAnchorManager.getPowered

object OpMindAnchorPowered : ConstMediaAction  {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = env.castingEntity
        if (target != null) {
            env.assertEntityInRange(target)
            val bool = getPowered(target.uuid)
            if (bool != null) {
                return listOf(BooleanIota(bool))
            }
        }
        return listOf(GarbageIota())
    }
}
