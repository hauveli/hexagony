package hauveli.hexagony.casting.actions.spells.control

import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import hauveli.hexagony.common.bilocation.FakeServerPlayer.Companion.spawnFakeClone
import hauveli.hexagony.common.control.PlayerControlData
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.UUID

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

            val server = env.getWorld().getServer()
            val caster = env.castingEntity

            // Wow, did not know origins compatibility existed in that, nice.
            // TODO: attach player camera to player, and make keybinds work as expected
            // TODO: if player location matches self, ATTACH
            if (entity?.uuid == null) return
            (entity as ServerPlayer)
            pos.subtract(entity.position())?.lengthSqr()?.let {
                val data = PlayerControlData.get(server)
                if (it < 0.001) {
                    println("Reattached!")
                    data.getOrCreate(entity.uuid).reattach(entity)
                } else {
                    println("Spawned fake!")
                    val fakePlayer = spawnFakeClone(entity, pos, UUID.randomUUID())
                    val fakePlayerEntry = data.getOrCreate(fakePlayer.uuid)
                    fakePlayerEntry.setFake(true)
                    fakePlayerEntry.setOwner(entity.uuid)
                    fakePlayer.addTag(entity.uuid.toString()) // hmm.... Might not be needed if persistent?
                    data.setDirty()
                }
            }
            //PlayerControlData.get(server).getOrCreate(entity.uuid).sprint(doesSprint)
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