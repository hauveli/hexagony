package hauveli.hexagony.casting.actions.spells

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import hauveli.hexagony.common.mind_anchor.MindAnchorManager.getPosition
import net.minecraft.server.level.ServerPlayer

object OpCongratulate : ConstMediaAction  {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = args.getEntity(0, argc)
        env.assertEntityInRange(target)
        val pos = getPosition(target as ServerPlayer)
        if (pos == null) {
            return listOf(GarbageIota())
        } else {
            return listOf(Vec3Iota(pos))
        }
    }
}
