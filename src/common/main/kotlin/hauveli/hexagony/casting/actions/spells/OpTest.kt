package hauveli.hexagony.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import hauveli.hexagony.features.fake_player.FakeServerPlayer
import hauveli.hexagony.features.fake_player.FakeServerPlayerUtils
import net.minecraft.core.UUIDUtil
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import java.util.UUID

object OpTest : SpellAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        //val target = args.getEntity(env.world, 0, argc)
        //env.assertEntityInRange(target)

        return SpellAction.Result(
            Spell(),
            (0.1 * MediaConstants.DUST_UNIT).toLong(),
            listOf(ParticleSpray.Companion.cloud(env.castingEntity!!.position().add(0.0, env.castingEntity!!.eyeHeight / 2.0, 0.0), 1.0))
        )
    }

    private data class Spell(val junk: Boolean = true) : RenderedSpell {
        // IMPORTANT: do not throw mishaps in this method! mishaps should ONLY be thrown in SpellAction.execute
        override fun cast(env: CastingEnvironment) {
            env.printMessage("text.hexagony.congrats".asTranslatedComponent("STARTED"))
            FakeServerPlayerUtils.spawnFakeClone(env.castingEntity!! as ServerPlayer, env.castingEntity!!.position(), UUID.randomUUID())
            env.printMessage("text.hexagony.congrats".asTranslatedComponent("FINISHED"))
        }
    }
}