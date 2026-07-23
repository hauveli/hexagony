package hauveli.hexagony.features.control

import hauveli.hexagony.features.control.ControlledMobEffects.makeControlEffect
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

object SynchedPlayerData {

    // It is feasible to make one effect for drop stack vs drop single, and one for each of hotbar slot.
    // it is infeasible to do so for look x and look y.
    // I have to store these somehow hmm....
    // wait a minute, the part of float [0,1] minecraft uses for angles (the size) <<<<< Integer.MAX_VALUE
    // I can fit the data I need to fit into the amplifier... hehehe.....
    /*
            look x
            look y
            Drop stack
            hotbar slot
     */
}