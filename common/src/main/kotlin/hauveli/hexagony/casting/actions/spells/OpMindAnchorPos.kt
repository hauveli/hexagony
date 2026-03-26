package hauveli.hexagony.casting.actions.spells

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import hauveli.hexagony.mind_anchor.MindAnchorManager
import hauveli.hexagony.mind_anchor.MindAnchorManager.getPosition
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import java.util.UUID

object OpMindAnchorPos : ConstMediaAction  {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = env.castingEntity
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
