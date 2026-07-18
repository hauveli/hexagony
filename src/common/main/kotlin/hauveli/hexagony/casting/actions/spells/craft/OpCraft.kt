package hauveli.hexagony.casting.actions.spells.craft

import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import hauveli.hexagony.features.graph_crafting.GraphCraftingInTheWorld.attemptToCraftTheGraph
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3

// todo: decide if I'm satisfied with the visuals or not and remove comments if I am satisfied

object OpCraft : SpellAction  {
    override val argc = 2

    override fun executeWithUserdata(
        args: List<Iota>,
        env: CastingEnvironment,
        userData: CompoundTag
    ): SpellAction.Result {
        val entityList = args.getList(0, argc)
        val orientation = args.getVec3(1, argc)
        val level = env.castingEntity?.level() as ServerLevel
        if (entityList.length() == 0) {
            // custom mishap here, possibly
            throw MishapEmptyList.of(args[0], 0, "item")
        }
        // todo: do I need a custom Mishap for these two as well, maybe? garbage-ing the entire list might make sense for this spell...
        for (subIota in entityList) {
            if (subIota is EntityIota) {
                val ent = subIota.getEntity(level)
                if (ent !is ItemEntity) {
                    throw MishapBadListEntry.of(args[0], entityList.indexOf(subIota), "item")
                }
                env.assertEntityInRange(ent)
            } else {
                throw MishapBadListEntry.of(args[0], entityList.indexOf(subIota), "item")
            }
        }
        val caster: Entity? = env.castingEntity
        if (caster !is ServerPlayer) {
            // do nothing I guess
        }
        val target = env.castingEntity!!
        return SpellAction.Result(
            Spell(entityList.toList(), orientation),
            MediaConstants.CRYSTAL_UNIT + MediaConstants.DUST_UNIT * entityList.length(),
            listOf(burst(target.position().add(0.0, target.eyeHeight / 2.0, 0.0), 1.0, 10)),
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

    private class Spell(private val entityList: List<Iota>, private val orientation: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val itemEntities = entityList.mapNotNull { iota ->
                val entity = (iota as? EntityIota)?.getEntity(env.world)
                entity as? ItemEntity
            }
            val playSoundOnSuccess = true
            val craftSuccess = attemptToCraftTheGraph(itemEntities, orientation, env, playSoundOnSuccess)
        }

        override fun cast(env: CastingEnvironment, image: CastingImage): CastingImage? {
            cast(env)
            return image
        }
    }
}
