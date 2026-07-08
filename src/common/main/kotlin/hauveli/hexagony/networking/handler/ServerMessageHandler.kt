package hauveli.hexagony.networking.handler

import at.petrak.hexcasting.server.ScrungledPatternsSave
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.enlightenment.ScrungledPatternSending
import hauveli.hexagony.networking.msg.*
import io.wispforest.owo.network.ServerAccess
import net.minecraft.resources.ResourceKey

fun HexagonyMessageC2S.applyOnServer(access: ServerAccess) = access.player().server.execute {
    when (this) {
        is PerWorldPatternPacketC2S -> {
            ScrungledPatternSending.doesThisPlayerHavePermission(resourceKey, access.player())
        }
    }
}