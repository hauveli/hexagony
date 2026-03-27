package hauveli.hexagony.casting.actions.spells.movement

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getLong
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityTooFarAway
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.misc.MediaConstants
import hauveli.hexagony.common.client.PlayerMovementAPI
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.List

object OpAttack : SpellAction {
    override val argc: Int
        get() = 2

    override fun executeWithUserdata(
        args: kotlin.collections.List<Iota>,
        env: CastingEnvironment,
        tags: CompoundTag
    ): SpellAction.Result {
        val target = args.getPlayer(0, argc)
        if (!env.isEntityInRange(target)) {
            // JavaMishapThrower.throwMishap(MishapEntityTooFarAway(player))
        }
        val caster: Entity? = env.getCastingEntity()
        if (caster !is ServerPlayer) {
            // JavaMishapThrower.throwMishap(MishapBadCaster())
        }
        /*
        if (!FakeplayerUtils.canBid(caster as ServerPlayer?, player)) JavaMishapThrower.throwMishap(
            MishapOthersName(
                player
            )
        )
        */
        val frequency = args.getInt(1, argc)

        return SpellAction.Result(
            OpAttack.Spell(target, frequency),
            MediaConstants.DUST_UNIT / 10,
            listOf(burst(target.position().add(0.0, target.getEyeHeight() / 2.0, 0.0), 1.0, 10)),
            1
        )
    }

    override fun hasCastingSound(castingEnvironment: CastingEnvironment): Boolean {
        return true
    }

    override fun awardsCastingStat(castingEnvironment: CastingEnvironment): Boolean {
        return true
    }

    override fun execute(
        args: kotlin.collections.List<Iota>,
        castingEnvironment: CastingEnvironment
    ): SpellAction.Result {
        throw IllegalStateException()
    }

    private  class Spell(private val target: ServerPlayer, private val frequency: Int) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val server = target.getServer()
            // val sourceStack = server!!.createCommandSourceStack()
            // val username: String? = FakeplayerUtils.getUsernameString(target)
            when (frequency) {
                0 -> PlayerMovementAPI.attackContinuous()
                -1 -> PlayerMovementAPI.attackOnce()
                else -> PlayerMovementAPI.attackPeriodic(frequency)
            }
        }

        override fun cast(env: CastingEnvironment, castingImage: CastingImage): CastingImage? {
            cast(env)
            return castingImage
        }
    }

    /*
    override fun operate(
        castingEnvironment: CastingEnvironment,
        castingImage: CastingImage,
        spellContinuation: SpellContinuation
    ): OperationResult {
        return operate.operate(this, castingEnvironment, castingImage, spellContinuation)
    }
    */
}