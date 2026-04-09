package hauveli.hexagony.casting.actions.spells.craft

import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.pigment.FrozenPigment
import com.mojang.math.Transformation
import hauveli.hexagony.common.craft.GraphCrafting
import hauveli.hexagony.common.craft.GraphCraftingRecipes.matchRecipe
import hauveli.hexagony.common.misc.TickScheduler
import hauveli.hexagony.mixin.craft.SetInterpolationDurationDisplayInvoker
import hauveli.hexagony.mixin.craft.SetItemStackItemDisplayInvoker
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Display.ItemDisplay
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.random.Random


object DisplayItemHelper {
    fun setDisplayItem(display: ItemDisplay, itemStack: ItemStack) {
        (display as SetItemStackItemDisplayInvoker).setStack(itemStack)
    }
    fun setInterpolationDuration(display: ItemDisplay, ticks: Int) {
        (display as SetInterpolationDurationDisplayInvoker).setLerpDur(ticks)
    }
    fun setInterpolationDelay(display: ItemDisplay, ticks: Int) {
        (display as SetInterpolationDurationDisplayInvoker).setLerpDelay(ticks)
    }
    fun setTransformation(display: ItemDisplay, trans: Transformation) {
        (display as SetInterpolationDurationDisplayInvoker).setTrans(trans)
    }
}

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


    fun spawnItemDisplay(
        level: Level,
        position: Vec3,
        stack: ItemStack
    ): ItemDisplay {
        val ent = ItemDisplay(
            EntityType.ITEM_DISPLAY,
            level
        )
        ent.setPos(position)
        ent.isInvisible = false
        DisplayItemHelper.setDisplayItem(ent, stack)
        val transformation = Transformation(
            Vector3f(),
            Quaternionf(),
            Vector3f(0.25f,0.25f,0.25f),
            Quaternionf(),
        )
        DisplayItemHelper.setInterpolationDelay(ent, 2)
        DisplayItemHelper.setTransformation(ent, transformation)
        level.addFreshEntity(ent)
        return ent
    }

    fun theatrics(itemEntities: List<ItemEntity>, recipe: Recipe<*>) {
        val worldGraph = GraphCrafting.buildGraph(itemEntities) // this is a bit redundant, but whatever....

        val level = itemEntities[0].level() ?: return
        val toCreate = ItemEntity(
            level,
            worldGraph.pos.x,
            worldGraph.pos.y,
            worldGraph.pos.z,
            ItemStack(BuiltInRegistries.ITEM.get(recipe.id))
        )

        val sortedByDistance = itemEntities.sortedBy { it.distanceToSqr(worldGraph.entity) }

        val startDelay = 2
        var delay = 10L
        val totalDuration = startDelay + delay * ( sortedByDistance.count() + 1 ) // plus one so minimum lerpDur is greater than 0

        for (itemEntity in sortedByDistance) {
            itemEntity.setPickUpDelay(totalDuration.toInt())
            val dummy = spawnItemDisplay(
                itemEntity.level(),
                itemEntity.position(),
                itemEntity.item
            )
            // dummy.deltaMovement = worldGraph.pos.subtract(dummy.position()).scale(0.0275)
            //dummy.lerpTo(worldGraph.pos.x, worldGraph.pos.y, worldGraph.pos.z, 0f, 0f, 2000, true)
            TickScheduler.schedule(
                delay,
                {
                    val thisDelay = (totalDuration).toInt()
                    DisplayItemHelper.setInterpolationDelay(dummy, startDelay)
                    DisplayItemHelper.setInterpolationDuration(dummy, totalDuration.toInt())
                    val transformation = Transformation(
                        worldGraph.pos.subtract(itemEntity.position()).scale(0.99).toVector3f(),
                        Quaternionf(),
                        Vector3f(0.1f,0.1f,0.1f),
                        Quaternionf(),
                    )
                    DisplayItemHelper.setTransformation(dummy, transformation)

                    // schedule removal inside the scheduler so it happens after it has moved
                    TickScheduler.schedule(
                        thisDelay.toLong(),
                        {
                            burst(worldGraph.pos.add(
                                0.5-Random.nextDouble(), 0.5-Random.nextDouble(), 0.5-Random.nextDouble()),
                                1.0, 10).sprayParticles(
                                level as ServerLevel,
                                FrozenPigment.ANCIENT.get()
                            )
                            dummy.kill()
                            dummy.remove(Entity.RemovalReason.DISCARDED)
                            dummy.discard()
                            itemEntity.item.count--
                        }
                    )
                }
            )
            // Subtract after scheduling...
            //dummy.lerpMotion(worldGraph.pos.x, worldGraph.pos.y, worldGraph.pos.z)
            delay += 10L
        }
        // reveal the item
        TickScheduler.schedule(
            totalDuration,
            {
                toCreate.setPickUpDelay(10) // 10 is delay of naturally dropped items
                toCreate.level().addFreshEntity(toCreate)
            }
        )

    }

    private class Spell(private val entityList: List<Iota>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val itemEntities = entityList.mapNotNull { iota ->
                val entity = (iota as? EntityIota)?.entity
                entity as? ItemEntity
            }
            println(itemEntities)

            // Now you have List<ItemEntity>
            val recipe = matchRecipe(itemEntities) ?: return

            println(recipe.id)

            theatrics(itemEntities, recipe)

        }

        override fun cast(env: CastingEnvironment, castingImage: CastingImage): CastingImage? {
            cast(env)
            return castingImage
        }
    }
}
