package hauveli.hexagony.casting.actions.spells.craft

import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import hauveli.hexagony.casting.actions.spells.control.OpUseItem
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.common.craft.GraphCraftingRuntimeImport.matchRecipe
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3

object OpCraft : SpellAction  {
    override val argc = 1

    override fun executeWithUserdata(
        args: List<Iota>,
        env: CastingEnvironment,
        tags: CompoundTag
    ): SpellAction.Result {
        val entityList = args.getList(0, argc)
        //if (!env.isEntityInRange(entityList)) {
            //  JavaMishapThrower.throwMishap(MishapEntityTooFarAway(target))
        //}
        val caster: Entity? = env.getCastingEntity()
        if (caster !is ServerPlayer) {
            // JavaMishapThrower.throwMishap(MishapBadCaster())
        }
        // TODO: get center of one of the items
        val target = env.castingEntity!!
        return SpellAction.Result(
            Spell(entityList.toList()),
            MediaConstants.DUST_UNIT / 10,
            listOf(burst(target.position().add(0.0, target.getEyeHeight() / 2.0, 0.0), 1.0, 10)),
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
        args: kotlin.collections.List<Iota>,
        castingEnvironment: CastingEnvironment
    ): SpellAction.Result {
        throw IllegalStateException()
    }

    private class Spell(private val entityList: List<Iota>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val itemEntities = entityList.mapNotNull { iota ->
                val entity = (iota as? EntityIota)?.entity
                entity as? ItemEntity
            }
            println(itemEntities)

            // Now you have List<ItemEntity>
            val recipe = matchRecipe(itemEntities)

            println(recipe)
        }

        override fun cast(env: CastingEnvironment, castingImage: CastingImage): CastingImage? {
            cast(env)
            return castingImage
        }
    }
}
