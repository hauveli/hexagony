package hauveli.hexagony.casting.actions.spells.craft

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import net.minecraft.world.entity.item.ItemEntity

object OpCraft : ConstMediaAction  {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val targets = args.getList(0, argc)
        for (target in targets) {
            (target as EntityIota)
            val ent = target.entity
            (ent as ItemEntity)
            val itemEntity = ent
            env.assertEntityInRange(itemEntity)
            // Todo:
            /*
                Get list of itemEntities
                check the position of each
                calculate the distance to each other itemEntity
                select itemEntity with lowest maximum distance
                set this as the central itemEntity
                The facing direction of this itemEntity, OR gravity
                determine which direction is "UP" for the sake of crafting
             */

        }
        /*
        val pos = getPosition(targets.uuid)
        if (pos == null) {
            return listOf(GarbageIota())
        } else {
            return listOf(Vec3Iota(pos))
        }
        */
        return listOf(GarbageIota()) // so it compiles while I do other things
    }
}
