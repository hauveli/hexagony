package hauveli.hexagony.casting.actions.spells.control

import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import hauveli.hexagony.common.control.PlayerControlData
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity


// https://github.com/abilliontrillionstars/hex-movesthemind/blob/main/src/main/java/abilliontrillionstars/movesthemind/casting/actions/spells/OpJumpButWithYourFeet.java
object OpJumpButWithYourFeet : SpellAction {
    override val argc: Int
        get() = 2

    override fun executeWithUserdata(
        args: List<Iota>,
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
        val doesJump = args.getBool(1, argc)
        return SpellAction.Result(
            Spell(target, doesJump),
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
        args: List<Iota>,
        castingEnvironment: CastingEnvironment
    ): SpellAction.Result {
        throw IllegalStateException()
    }

    private class Spell(private val target: ServerPlayer, private val doesJump: Boolean) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val server = target.getServer()
            if (server == null) return
            PlayerControlData.get(server).getOrCreate(target.uuid).jump(target, doesJump)
        }

        override fun cast(env: CastingEnvironment, castingImage: CastingImage): CastingImage? {
            cast(env)
            return castingImage
        }
    }

    // No idea how to make this work............
    /*
    override fun operate(
        castingEnvironment: CastingEnvironment,
        castingImage: CastingImage,
        spellContinuation: SpellContinuation
    ): OperationResult {
        return SpellAction. operate(this, castingEnvironment, castingImage, spellContinuation)
    }
     */
}