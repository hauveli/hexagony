package hauveli.hexagony.casting.actions.spells.dissociate

import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.misc.MediaConstants
import hauveli.hexagony.casting.actions.spells.craft.MishapBadListEntry
import hauveli.hexagony.casting.actions.spells.craft.MishapEmptyList
import hauveli.hexagony.features.graph_crafting.GraphCrafting
import hauveli.hexagony.features.graph_crafting.GraphCraftingFromNormalRecipes
import hauveli.hexagony.registry.HexagonyAdvancements
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.phys.Vec3

// todo: decide if I'm satisfied with the visuals or not and remove comments if I am satisfied

object OpDissociate : SpellAction  {
    override val argc = 1

    override fun executeWithUserdata(
        args: List<Iota>,
        env: CastingEnvironment,
        tags: CompoundTag
    ): SpellAction.Result {
        val duration = args.getPositiveDouble(0, argc)
        if (env !is StaffCastEnv)
            throw MishapBadCaster() // it would be possible to trap a player indefinitely if this does not mishap....
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
        castingEnvironment: CastingEnvironment
    ): SpellAction.Result {
        throw IllegalStateException()
    }

    private class Spell(private val duration: Double) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (duration > 1.0 / 20.0) {

            }
        }
    }
}
