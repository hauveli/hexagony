package hauveli.hexagony.casting.actions.spells.trepannation

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import hauveli.hexagony.mind_anchor.MindAnchorManager.getMedia
import net.minecraft.server.level.ServerPlayer

object OpMindAnchorMedia : ConstMediaAction  {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = env.castingEntity
        if (target != null) {
            env.assertEntityInRange(target)
            val media = getMedia(target as ServerPlayer)
            if (media != null) {
                return listOf(DoubleIota(media.toDouble()))
            }
        }
        return listOf(GarbageIota())
    }
}
