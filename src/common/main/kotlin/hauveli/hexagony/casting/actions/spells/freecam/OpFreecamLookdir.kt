package hauveli.hexagony.casting.actions.spells.freecam

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapInternalException
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.freecam.FreeCameraServerData
import hauveli.hexagony.registry.HexagonyMobEffects
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

object OpFreecamLookdir : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = args.getEntity(env.world, 0, argc)
        // Uhhhh I forget if ConstMediaAction does anything for me or not for Mishaps
        env.assertEntityInRange(target)

        if (target !is ServerPlayer) {
            throw MishapBadEntity.of(target, "player")
        }

        if (!target.hasEffect(HexagonyMobEffects.FREECAM.holder())) {
            throw MishapNotFreecam()
        }

        val obtainedData = FreeCameraServerData.getLookDir(target)
        // to check if the player is in freecam or not, we check if they have the freecam mob effect because that is server-sided
        // I considered adding a bool to the data sync but that's an extra bit for no reason when this method is called so rarely
        // so there's no real use-case benefit to it afaict, aside from maybe some niche use for solving the below I haven't thought of
        if (obtainedData.isNullOrEmpty()) {
            // I gotta let the player know that something went wrong somehow but I'm not sure how to do it....................
            // Hexagony.LOGGER.warn("${Hexagony.MODID} encountered a problem: {} did not have an entry, but should have had one.", target.name)
            // return listOf(NullIota()) // uhhh I kind of wanna do this better and I'm not sure how the player ended up here if not because the client was too slow to sync data
            // For the first few frames on high-latency servers (> 50 ms), the player may not have valid data available on the server yet.
            // I can't think of a better way than to assume the player's eye position is where it is almost garuanteed to be (eyes) for the first few ticks
            // until the player has sent data and the server has stored it...
            return HexAPI.instance().getEntityLookDirSpecial(target).asActionResult
        }
        // In conclusion, I think to solve the issue here I would need to do a lot more work than I currently am to solve what will affect ~0 to ~5 ticks of data by <1 block and ? lookangle...
        // If I for example add a server sided entity, that still won't solve the issue because the client-server desync would make it either unberaable to play if I sync it in that direction
        // or it'll have the exact same issue as described above.
        // Please, if you can think of a solution which resolves all of these, let me know.
        // (or if my hastily written estimation of the problem here is wrong, let me know, too...)

        return obtainedData
    }
}
