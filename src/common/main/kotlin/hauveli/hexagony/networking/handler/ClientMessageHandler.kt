package hauveli.hexagony.networking.handler

import hauveli.hexagony.features.enlightenment.ScrungledPatternSending
import hauveli.hexagony.networking.msg.*
import net.minecraft.client.Minecraft
import io.wispforest.owo.network.ClientAccess

fun HexagonyMessageS2C.applyOnClient(access: ClientAccess) = Minecraft.getInstance().execute {
    // NOTE: this is commented out because otherwise it fails to compile if there's nothing inside of the when expression
    when (this) {
        is PerWorldPatternPacketS2C -> {
            ScrungledPatternSending.clientRenderThisNow(resourceKey, angles, startDir)
        }
        else -> {

        }
    }
}