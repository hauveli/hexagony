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
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.misc.MediaConstants
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.List

object OpCreateFakeplayer : SpellAction {
    override val argc: Int
        get() = 1 // number of arguments should be 1 if the input is EntityIota

    override fun executeWithUserdata(
        args: kotlin.collections.List<Iota>,
        env: CastingEnvironment,
        tags: CompoundTag
    ): SpellAction.Result {
        val pos = args.getVec3(0, argc)
        // ambit check
        if (!env.isVecInAmbit(pos)) {
            //JavaMishapThrower.throwMishap(MishapBadLocation(pos, "too_far"))
        }
        val caster: Entity? = env.getCastingEntity()
        // shouldn't be cast playerless
        if (caster !is ServerPlayer) {
            // JavaMishapThrower.throwMishap(MishapBadCaster())
        }
        // no grey-goo! bad fakeplayer! bad!
        /* // just let them create fakeplayers up to 2 CreateFakeplayers down from the player, as a treat...
        if (caster is EntityPlayerMPFake) {
            // JavaMishapThrower.throwMishap(MishapBadCaster())
        }
         */

        // val username: String = FakeplayerUtils.getFakeName(FakeplayerUtils.getUsernameString(caster as ServerPlayer?))
        val server = env.getWorld().getServer()
        /*
        // fail early if the player exists already
        // val player = server.getPlayerList().getPlayerByName(username)
        if (player != null) {
            // JavaMishapThrower.throwMishap(MishapOthersName(player))
        }
         */


        return SpellAction.Result(
            OpCreateFakeplayer.Spell(pos, caster),
            MediaConstants.CRYSTAL_UNIT,
            listOf(burst(pos, 1.0, 10)),
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

    private class Spell(private val pos: Vec3, private val entity: Entity?) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            /*
            val server = env.getWorld().getServer()
            val sourceStack = server.createCommandSourceStack()
            var dim = env.getWorld().dimension().toString()
            dim = dim.substring(dim.lastIndexOf(' ') + 1, dim.indexOf(']'))
            val gamemode: String?
            if (env.getCaster()!!.gameMode.isCreative()) gamemode = "creative"
            else gamemode = "survival"
            server.getCommands().performPrefixedCommand(
                sourceStack, ("player " + name + " spawn at " + pos.x + " " + pos.y + " " + pos.z
                        + " facing 0 0 in " + dim + " in " + gamemode)
            )
            // set the origin of a player if it's installed (now suppressed from logs)
            if (FabricLoader.getInstance().isModLoaded("origins")) server.getCommands().performPrefixedCommand(
                sourceStack.withSuppressedOutput(),
                "origin set " + name + " origins:origin origins:human"
            )
            // do the same for other lesser origins mods (thanks ashdew-derg!)
            if (FabricLoader.getInstance().isModLoaded("tinkerers_statures")) server.getCommands()
                .performPrefixedCommand(
                    sourceStack.withSuppressedOutput(),
                    "origin set " + name + " origins:stature tinkerer:unaffected"
                )
            if (FabricLoader.getInstance().isModLoaded("origins-classes")) server.getCommands().performPrefixedCommand(
                sourceStack.withSuppressedOutput(),
                "origin set " + name + " origins-classes:class origins-classes:nitwit"
            )
            if (FabricLoader.getInstance().isModLoaded("aspects")) {
                server.getCommands().performPrefixedCommand(
                    sourceStack.withSuppressedOutput(),
                    "origin set " + name + " aspects:elements aspects:vacuos"
                )
                server.getCommands().performPrefixedCommand(
                    sourceStack.withSuppressedOutput(),
                    "origin set " + name + " aspects:origins aspects:imp"
                )
            }
             */
            // Wow, did not know origins compatibility existed in that, nice.
            // TODO: attach player camera to player, and make keybinds work as expected
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
        return operate(this, castingEnvironment, castingImage, spellContinuation)
    }
    */

}