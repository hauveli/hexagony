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
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityTooFarAway
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.misc.MediaConstants
import hauveli.hexagony.common.client.PlayerMovementAPI
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.List

object OpSwapHands : SpellAction {
    override val argc: Int
        get() = 1

    override fun executeWithUserdata(
        args: kotlin.collections.List<Iota>,
        env: CastingEnvironment,
        tags: CompoundTag
    ): SpellAction.Result {
        val target = args.getPlayer(0, argc)
        if (!env.isEntityInRange(target)) {
            //  JavaMishapThrower.throwMishap(MishapEntityTooFarAway(target))
        }
        val caster: Entity? = env.getCastingEntity()
        if (caster !is ServerPlayer) {
            // JavaMishapThrower.throwMishap(MishapBadCaster())
        }
        /*
        if (!FakeplayerUtils.canBid(caster as ServerPlayer?, target)) JavaMishapThrower.throwMishap(
            MishapOthersName(
                target
            )
        )
        */

        return SpellAction.Result(
            OpSwapHands.Spell(target),
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

    private class Spell(private val target: ServerPlayer) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val server = target.getServer()
            PlayerMovementAPI.swapHands()
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