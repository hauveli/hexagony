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
import hauveli.hexagony.features.graph_crafting.GraphCrafting
import hauveli.hexagony.features.graph_crafting.GraphCraftingFromNormalRecipes
import hauveli.hexagony.registry.HexagonyAdvancements
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.phys.Vec3

// todo: decide if I'm satisfied with the visuals or not and remove comments if I am satisfied

object DisplayItemHelper {
    /*
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

    fun setBillboardConstraints(display: ItemDisplay, constraints: Display.BillboardConstraints) {
        (display as SetInterpolationDurationDisplayInvoker).setBillboard(constraints)
    }
     */
}

object OpCraft : SpellAction  {
    override val argc = 2

    override fun executeWithUserdata(
        args: List<Iota>,
        env: CastingEnvironment,
        tags: CompoundTag
    ): SpellAction.Result {
        val entityList = args.getList(0, argc)
        val orientation = args.getVec3(1, argc)
        //if (!env.isEntityInRange(entityList)) {
            //  JavaMishapThrower.throwMishap(MishapEntityTooFarAway(target))
        //}
        val caster: Entity? = env.getCastingEntity()
        if (caster !is ServerPlayer) {
            // JavaMishapThrower.throwMishap(MishapBadCaster())
        }
        val target = env.castingEntity!!
        return SpellAction.Result(
            Spell(entityList.toList(), orientation),
            MediaConstants.DUST_UNIT / 10,
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
        castingEnvironment: CastingEnvironment
    ): SpellAction.Result {
        throw IllegalStateException()
    }

/*
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
        DisplayItemHelper.setBillboardConstraints(ent, Display.BillboardConstraints.FIXED)
        val scale = if (stack.item is BlockItem) 0.25f else 0.5f
        val transformation = Transformation(
            Vector3f(),
            Quaternionf(),
            Vector3f(scale,scale,scale),
            Quaternionf(),
        )
        DisplayItemHelper.setInterpolationDelay(ent, 2)
        DisplayItemHelper.setTransformation(ent, transformation)
        level.addFreshEntity(ent)
        return ent
    }

 */

    fun theatrics(itemEntities: List<ItemEntity>, recipe: Recipe<*>, worldGraph: GraphCrafting.ItemNode) {
        val level = itemEntities[0].level() ?: return
        val toCreate = ItemEntity(
            level,
            worldGraph.pos.x,
            worldGraph.pos.y,
            worldGraph.pos.z,
            recipe.getResultItem(level.registryAccess())
        )
        GraphCrafting.subtract(worldGraph, recipe)

        val sortedByDistance = itemEntities.sortedBy { it.distanceToSqr(worldGraph.entity) }

        val startDelay = 2
        var delay = 0L
        val totalDuration = startDelay + delay * ( sortedByDistance.count() + 1 ) // plus one so minimum lerpDur is greater than 0

        /*
        for (itemEntity in sortedByDistance) {
            /*
            val dummy = spawnItemDisplay(
                itemEntity.level(),
                itemEntity.position(),
                itemEntity.item
            )
            val destination = dummy.position().subtract(worldGraph.pos).scale(0.99)
            val transformation = Transformation(
                // worldGraph.pos.subtract(dummy.position()).scale(0.99).toVector3f(),
                destination.toVector3f(),
                Quaternionf(),
                //Vector3f(1f,1f,1f),
                Vector3f(0.01f,0.01f,0.01f), // make it size 0?
                Quaternionf(),
            )

             */

            // TODO: Miyu said thusly in hexcord
            /*
                Just make it spawn a particle effect
                That is what particle effects were made for
                You can make a custom particle that renders as an item stack and then make it fly or do whatever you want, and particles automatically die as needed
                Even the item pickup animation ( basically the same as what you are doing ) is a particle effect
             */

            // itemEntity.item.count--
            // Hexes are instant
            // I couldnt figure out another way...
            /*
            TickScheduler.schedule(
                1,
                {
                    // crawl the rootNode to avoid deleting other items
                    // pros: kind of cool
                    // cons: ??
                    //subtract(worldGraph)
                }
            )
            */

            // dummy.deltaMovement = worldGraph.pos.subtract(dummy.position()).scale(0.0275)
            //dummy.lerpTo(worldGraph.pos.x, worldGraph.pos.y, worldGraph.pos.z, 0f, 0f, 2000, true)
            /*
            TickScheduler.schedule(
                delay,
                {
                    DisplayItemHelper.setInterpolationDelay(dummy, 1)
                    DisplayItemHelper.setInterpolationDuration(dummy, 10)
                    DisplayItemHelper.setTransformation(dummy, transformation)

                    // schedule removal inside the scheduler so it happens after it has moved
                    TickScheduler.schedule(
                        11,
                        {
                            burst(destination.add(
                                0.5-Random.nextDouble(), 0.5-Random.nextDouble(), 0.5-Random.nextDouble()),
                                1.0, 10).sprayParticles(
                                level as ServerLevel,
                                FrozenPigment.ANCIENT.get()
                            )
                            dummy.kill()
                            dummy.remove(Entity.RemovalReason.DISCARDED)
                            dummy.discard()
                        }
                    )
                }
            )
             */
            // Subtract after scheduling...
            //dummy.lerpMotion(worldGraph.pos.x, worldGraph.pos.y, worldGraph.pos.z)
            delay += 1L
        }
         */
        level.playSound(
            null, // all nearby players?
            worldGraph.entity.blockPosition(),
            SoundEvents.AMETHYST_BLOCK_CHIME,
            SoundSource.BLOCKS,
            1.0f,
            1.0f
        )
        toCreate.setPickUpDelay(10) // 10 is delay of naturally dropped items
        toCreate.level().addFreshEntity(toCreate)
        // Hexes are instant
        /*
        // reveal the item
        TickScheduler.schedule(
            totalDuration + delay,
            {
                toCreate.setPickUpDelay(10) // 10 is delay of naturally dropped items
                toCreate.level().addFreshEntity(toCreate)
            }
        )
         */

    }

    private class Spell(private val entityList: List<Iota>, private val orientation: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val itemEntities = entityList.mapNotNull { iota ->
                val entity = (iota as? EntityIota)?.getEntity(env.world)
                entity as? ItemEntity
            }

            val match = GraphCraftingFromNormalRecipes.matchRecipe(itemEntities, orientation)
            val recipe = match.first
            val worldGraph = match.second
            val casterMaybePlayer = env.castingEntity
            if (recipe != null
                && casterMaybePlayer is Player
                && casterMaybePlayer is ServerPlayer) {
                // it checks anyway so whatever
                HexagonyAdvancements.tryGrantingAdvancement(
                    casterMaybePlayer,
                    HexagonyAdvancements.GRAPHTING)

                GraphCrafting.sprayAndPray(worldGraph)
                theatrics(itemEntities, recipe, worldGraph)
            } else {
                GraphCrafting.sprayAndPray(worldGraph)
            }

        }

        override fun cast(env: CastingEnvironment, castingImage: CastingImage): CastingImage? {
            cast(env)
            return castingImage
        }
    }
}
