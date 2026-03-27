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
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.List

object OpDestroyFakeplayer : SpellAction {
    override val argc: Int
        get() = 1

    override fun executeWithUserdata(
        args: kotlin.collections.List<Iota>,
        env: CastingEnvironment,
        tags: CompoundTag
    ): SpellAction.Result {
        val player = args.getPlayer(0, argc)
        if (!env.isEntityInRange(player)) {
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
        // TODO: make the advancement have a success variant? Might not be needed though...
        /*
        if (caster!!.getStringUUID() == player.getStringUUID() && caster.javaClass == ServerPlayer::class.java) {
            // easter egg joke advancement! I love modding.
            val server = env.getWorld().getServer()
            val sourceStack = server.createCommandSourceStack().withSuppressedOutput()
            server.getCommands().performPrefixedCommand(
                sourceStack,
                "advancement grant " + FakeplayerUtils.getUsernameString(caster as ServerPlayer) + " only minecraft:movesthemind/try_banish_self"
            )

            JavaMishapThrower.throwMishap(MishapOthersName(caster))
        }
        */
        return SpellAction.Result(
            Spell(player),
            MediaConstants.DUST_UNIT * 5,
            listOf(burst(player.position().add(0.0, player.getEyeHeight() / 2.0, 0.0), 1.0, 10)),
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

    private class Spell(private val player: ServerPlayer?) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val server = env.getWorld().getServer()
            // val sourceStack = server.createCommandSourceStack()
            // server.getCommands().performPrefixedCommand(sourceStack, "player " + FakeplayerUtils.getUsernameString(player) + " kill")
            // TODO: Deatch player
            player?.sendSystemMessage(Component.nullToEmpty("Totally Detached the player!!!"))
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