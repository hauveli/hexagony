package hauveli.hexagony.casting.actions.spells.control

import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import hauveli.hexagony.common.bilocation.FakeServerPlayer.Companion.copyPlayerDataFrom
import hauveli.hexagony.common.bilocation.FakeServerPlayer.Companion.copyPlayerDataFromTo
import hauveli.hexagony.common.bilocation.FakeServerPlayer.Companion.spawnFakeClone
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.common.misc.AdvancementProvider.isTrepanned
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.pow

object OpCreateFakeplayer : SpellAction {
    override val argc: Int
        get() = 2 // number of arguments should be 1 if the input is EntityIota

    override fun executeWithUserdata(
        args: List<Iota>,
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

        val duration = args.getInt(1, argc)
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
            Spell(pos, caster, duration.toLong()),
            (MediaConstants.CRYSTAL_UNIT +
                    MediaConstants.DUST_UNIT * (1 + duration / 3600).toDouble().pow(2.0) // power 2, can not be negative
                    ).toLong(),
            listOf(burst(pos, 1.0, 10)),
            2
        )
    }

    override fun hasCastingSound(castingEnvironment: CastingEnvironment): Boolean {
        return true
    }

    override fun awardsCastingStat(castingEnvironment: CastingEnvironment): Boolean {
        return true
    }

    override fun execute(
        args: List<Iota>,
        castingEnvironment: CastingEnvironment
    ): SpellAction.Result {
        throw IllegalStateException()
    }

    private val GREATEST_DISTANCE_TO_SWAPPEE_SQUARED = 1.0

    fun lookForBodyAtPosition(server: MinecraftServer, caster: ServerPlayer, pos: Vec3): ServerPlayer? {
        // check all players
        var minimum = GREATEST_DISTANCE_TO_SWAPPEE_SQUARED // I should use a better number than this and better approach but whatever
        var nearestPlayer: ServerPlayer? = null
        for (player in server.playerList.players) {
            pos.subtract(player.eyePosition)?.lengthSqr()?.let {
                if (it < minimum) {
                    minimum = it
                    if (player.uuid == caster.uuid) {
                        return player
                    } else if (player.tags.contains(caster.uuid.toString())) {
                        // check if caster is allowed to swap before selecting
                        nearestPlayer = player
                    }
                }
            }
        }
        return nearestPlayer
    }

    fun swapHomunculusAndPlayer(realPlayer: ServerPlayer, homunculus: ServerPlayer) {
        val dummyRealplayer = copyPlayerDataFrom(realPlayer)
        val dummyHomunculus = copyPlayerDataFrom(homunculus)
        // This is gonna cause some obvious issues, level is not accessible so need to fix that most likely...
        copyPlayerDataFromTo(homunculus, dummyHomunculus, realPlayer)
        copyPlayerDataFromTo(realPlayer, dummyRealplayer, homunculus)
    }

    private class Spell(private val pos: Vec3, private val entity: Entity?, val duration: Long) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {

            val server = env.getWorld().server
            val caster = env.castingEntity

            // Wow, did not know origins compatibility existed in that, nice.
            // TODO: attach player camera to player, and make keybinds work as expected
            // TODO: if player location matches self, ATTACH
            if (entity?.uuid == null) return
            // (entity as ServerPlayer)
            (caster as ServerPlayer)
            val potentialSwappee = lookForBodyAtPosition(server, caster, pos)
            val data = PlayerControlData.get(server)
            if (potentialSwappee != null && isTrepanned(caster)) {
                println("Reattached!")
                if (potentialSwappee != caster) { // if this was self it wouldnt even do anything, do I want to check anyway?
                    swapHomunculusAndPlayer(caster, potentialSwappee)
                }
                data.getOrCreate(caster.uuid).reattach(caster)
            } else {
                println("Spawned fake!")
                val fakePlayer = spawnFakeClone(caster, pos, UUID.randomUUID())
                val fakePlayerEntry = data.getOrCreate(fakePlayer.uuid)
                fakePlayerEntry.setFake(true)
                fakePlayerEntry.setOwner(caster.uuid)
                fakePlayer.addTag(caster.uuid.toString()) // hmm.... Might not be needed if persistent?
                fakePlayerEntry.duration(duration)
                data.setDirty()
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