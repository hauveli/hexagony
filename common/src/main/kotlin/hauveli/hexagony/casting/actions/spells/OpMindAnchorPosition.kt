package hauveli.hexagony.casting.actions.spells

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import hauveli.hexagony.common.client.FreeCamAPI
import hauveli.hexagony.mind_anchor.MindAnchorManager.getPosition
import net.minecraft.client.Minecraft

object OpMindAnchorPosition : ConstMediaAction  {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = env.castingEntity

        if (target?.level() != null && target.level().isClientSide) {
            val mc = Minecraft.getInstance()
            //FreeCamAPI(mc).detachCamera()
            val player = mc.player
            if (player != null) {
                player.input.jumping = true
            }
        }

        if (target != null) {
            env.assertEntityInRange(target)
            val pos = getPosition(target.uuid)
            if (pos != null) {
                return listOf(Vec3Iota(pos))
            }
        }
        return listOf(GarbageIota())
    }
}
