package hauveli.hexagony.features.freecam

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.iota.Iota
import hauveli.hexagony.networking.HexagonyNetworking.CHANNEL
import hauveli.hexagony.networking.msg.FreeCamDataPacketC2S
import net.minecraft.world.entity.Entity


// Just stuff to process and send data to the server I guess
object FreeCameraClientData {

    fun sync() {
        val currentFreecam = FreeCameraEntity.freeCam!!
        CHANNEL.clientHandle().send(FreeCamDataPacketC2S(
            currentFreecam.eyePosition,
            HexAPI.instance().getEntityLookDirSpecial(currentFreecam)))
    }
}
