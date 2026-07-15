package hauveli.hexagony.casting.actions.spells.freecam

import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.misc.MediaConstants
import hauveli.hexagony.registry.HexagonyMobEffects
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance


// todo: decide if I'm satisfied with the visuals or not and remove comments if I am satisfied

object OpFreecam : SpellAction  {
    override val argc = 1

    override fun executeWithUserdata(
        args: List<Iota>,
        env: CastingEnvironment,
        userData: CompoundTag
    ): SpellAction.Result {
        val duration = args.getPositiveDouble(0, argc)
        if (env !is StaffCastEnv)
            throw MishapBadCaster() // it would be possible to trap a player indefinitely if this does not mishap.... I think?
        val caster = env.castingEntity as ServerPlayer
        val cost = MediaConstants.CRYSTAL_UNIT + MediaConstants.DUST_UNIT * duration * duration
        return SpellAction.Result(
            Spell(duration),
            cost.toLong(),
            listOf(burst(caster.position().add(0.0, caster.eyeHeight / 2.0, 0.0), 1.0, 10)),
            1
        )
    }

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): SpellAction.Result {
        throw IllegalStateException()
    }

    private class Spell(private val duration: Double) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (duration > 1.0 / 20.0) {
                // Server will apply a debuff to the player to convey that their camera is deatched.
                // this solves all the issues with de/serialization of the player's state client<->server, I think
                // as a bonus it means it's easier to use
                // extra extra bonus: for the mind anchor, I can use the timer as a count-down for when media runs out
                // based on currently remaining media?

                // I have no fucking idea how to make my registration thing return a holder but I'll figure that out later :clueless:
                HexagonyMobEffects.FREECAM.value
                val instance = MobEffectInstance(
                    HexagonyMobEffects.FREECAM.holder(),
                    duration.toInt(),
                    0, // amplifier, I guess I could use strength 1 if mind anchor for jank checks? that seems silly though, but it would work...
                    false,  // ambient effect?
                    false,  // visible in inventory?
                    false // visible in top right corner?
                )
                env.castingEntity!!.addEffect(instance)
            }
        }
    }
}
