package hauveli.hexagony.features.enlightenment

import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.client.render.PatternRenderer
import at.petrak.hexcasting.common.lib.hex.HexActions
import at.petrak.hexcasting.interop.patchouli.LookupPatternComponent
import at.petrak.hexcasting.server.ScrungledPatternsSave
import hauveli.hexagony.Hexagony
import hauveli.hexagony.networking.HexagonyNetworking.CHANNEL
import hauveli.hexagony.networking.msg.PerWorldPatternPacketC2S
import hauveli.hexagony.networking.msg.PerWorldPatternPacketS2C
import hauveli.hexagony.registry.HexagonyAdvancements.hasHeldScroll
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer


object ScrungledPatternSending {
    @JvmField
    var storedPatterns: MutableMap<LookupPatternComponent, String> = mutableMapOf<LookupPatternComponent, String>()

    @JvmField
    var previousKeyRequest: String = ""

    @JvmField
    var currentKey: String = ""
    @JvmField
    var currentAngles: String = ""
    @JvmField
    var currentStartDir: HexDir = HexDir.NORTH_EAST
    @JvmField
    var currentHexPattern: HexPattern? = null

    @JvmStatic
    fun fromClient(resourceKey: String) {
        CHANNEL.clientHandle().send(PerWorldPatternPacketC2S(resourceKey))
        previousKeyRequest = resourceKey
    }

    fun doesThisPlayerHavePermission(resourceKey: String, serverPlayer: ServerPlayer) {
        if (hasHeldScroll(serverPlayer, resourceKey)) {
            val perWorldPattern = ScrungledPatternsSave.open(serverPlayer.serverLevel()).lookupReverse(
                HexActions.REGISTRY.getHolder(ResourceLocation.parse(resourceKey)).get().key()
            )
            if (perWorldPattern == null) return
            CHANNEL.serverHandle(serverPlayer).send(PerWorldPatternPacketS2C(resourceKey, perWorldPattern.first, perWorldPattern.second.canonicalStartDir().toString()))
        }
    }

    fun clientRenderThisNow(resourceKey: String, angles: String, startDir: String) {
        currentKey = resourceKey
        currentAngles = angles
        currentStartDir = HexDir.fromString(startDir)
        currentHexPattern = HexPattern.fromAngles(currentAngles, currentStartDir)

        //storedPatterns[currentKey]?.render()
    }
}