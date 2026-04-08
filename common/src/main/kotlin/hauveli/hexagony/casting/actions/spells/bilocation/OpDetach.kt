package hauveli.hexagony.casting.actions.spells.bilocation

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import hauveli.hexagony.mind_anchor.MindAnchorManager
import net.minecraft.server.level.ServerPlayer

object OpDetach : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = env.castingEntity
        if (target != null) {
            env.assertEntityInRange(target)
            val pos = MindAnchorManager.getPosition(target as ServerPlayer)
            if (pos != null) {
                return listOf(Vec3Iota(pos))
            }
        }
        return listOf(GarbageIota())
    }
}