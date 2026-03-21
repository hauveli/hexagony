package hauveli.hexagony.casting.actions.spells

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.getLivingEntityButNotArmorStand
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapAlreadyBrainswept
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBrainsweep
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

object OpBrainsweep : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val sacrifice = args.getPlayer(0, argc)
        val vecPos = args.getVec3(1, argc)
        val pos = BlockPos.containing(vecPos)
        env.assertEntityInRange(sacrifice)


        env.assertVecInRange(vecPos)
        env.assertEntityInRange(sacrifice)

        if (!env.canEditBlockAt(pos))
            throw MishapBadLocation(vecPos, "forbidden")

        /*
        if (sacrifice.type.`is`(HexTags.Entities.NO_BRAINSWEEPING))
            throw MishapBadBrainsweep(sacrifice, pos)

        if (IXplatAbstractions.INSTANCE.isBrainswept(sacrifice))
            throw MishapAlreadyBrainswept(sacrifice)
        */

        val state = env.world.getBlockState(pos)

        val recman = env.world.recipeManager
        val recipes = recman.getAllRecipesFor(HexRecipeStuffRegistry.BRAINSWEEP_TYPE)

        val recipe = recipes.find { it.matches(state, sacrifice, env.world) }
            ?: throw MishapBadEntity(sacrifice, Component.empty() )  // throw MishapBadBrainsweep(sacrifice, pos)

        return SpellAction.Result(
            Spell(pos, state, sacrifice, recipe),
            recipe.mediaCost,
            listOf(
                ParticleSpray.cloud(sacrifice.position(), 1.0),
                ParticleSpray.burst(Vec3.atCenterOf(pos), 0.3, 100))
        )
    }

    private data class Spell(
        val pos: BlockPos,
        val state: BlockState,
        val sacrifice: ServerPlayer,
        val recipe: BrainsweepRecipe
    ) : RenderedSpell {
        // IMPORTANT: do not throw mishaps in this method! mishaps should ONLY be thrown in SpellAction.execute
        override fun cast(env: CastingEnvironment) {
            env.printMessage("text.hexagony.congrats".asTranslatedComponent(sacrifice.displayName));
        }
    }
}
