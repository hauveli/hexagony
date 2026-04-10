package hauveli.hexagony.casting.actions.spells.control

import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityTooFarAway
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.misc.MediaConstants
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.common.misc.AdvancementProvider.grantAdvancement
import hauveli.hexagony.common.misc.AdvancementProvider.isTrepanned
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

object OpDestroyFakeplayer : SpellAction {
    override val argc: Int
        get() = 1

    override fun executeWithUserdata(
        args: List<Iota>,
        env: CastingEnvironment,
        userData: CompoundTag
    ): SpellAction.Result {
        val player = args.getPlayer(0, argc)
        if (!env.isEntityInRange(player)) {
            throw MishapEntityTooFarAway(player)
        }
        val caster: Entity? = env.castingEntity
        // Can't cast from box?
        if (caster !is ServerPlayer) {
            throw MishapBadCaster()
        }

        // can't cast on non-consenting players (you can if you add the tag yourself, though!) I don't know what would happen...
        // if target is not self, and target does not consent, abort
        if (player != caster && !player.tags.contains(caster.uuid.toString())) {
            throw MishapOthersName(player)
        }
        // TODO: make the advancement have a success variant? Might not be needed though...

        if (caster.getStringUUID() == player.getStringUUID() && caster.javaClass == ServerPlayer::class.java) {
            // easter egg joke advancement! I love modding.
            // I could not figure out how to keep the original namespace (movesthemind) when adding an advancement
            grantAdvancement(player, "try_banish_self")
            if (!isTrepanned(player))
                throw(MishapOthersName(caster))
        }

        return SpellAction.Result(
            Spell(player),
            MediaConstants.DUST_UNIT * 5,
            listOf(burst(player.position().add(0.0, player.eyeHeight / 2.0, 0.0), 1.0, 10)),
            1
        )
    }

    override fun hasCastingSound(ctx: CastingEnvironment): Boolean {
        return true
    }

    override fun awardsCastingStat(ctx: CastingEnvironment): Boolean {
        return true
    }

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): SpellAction.Result {
        throw IllegalStateException()
    }

    private class Spell(private val target: ServerPlayer) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val server = env.getWorld().server
            // val sourceStack = server.createCommandSourceStack()
            // server.getCommands().performPrefixedCommand(sourceStack, "player " + FakeplayerUtils.getUsernameString(player) + " kill")
            // TODO: Deatch player
            target.sendSystemMessage(Component.nullToEmpty("Totally Detached the player!!!"))
            PlayerControlData.get(server).getOrCreate(target.uuid).detach(target)
            // PlayerControlData.get(server).getOrCreate(target.uuid).drop(true)
        }

        override fun cast(env: CastingEnvironment, image: CastingImage): CastingImage? {
            cast(env)
            return image
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