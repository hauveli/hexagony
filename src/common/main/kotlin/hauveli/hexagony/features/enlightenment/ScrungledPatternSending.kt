package hauveli.hexagony.features.enlightenment

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.entities.EntityWallScroll
import at.petrak.hexcasting.common.lib.HexDataComponents
import at.petrak.hexcasting.common.lib.hex.HexActions
import at.petrak.hexcasting.interop.patchouli.LookupPatternComponent
import at.petrak.hexcasting.server.ScrungledPatternsSave
import hauveli.hexagony.Hexagony
import hauveli.hexagony.networking.HexagonyNetworking.CHANNEL
import hauveli.hexagony.networking.msg.PerWorldPatternPacketC2S
import hauveli.hexagony.networking.msg.PerWorldPatternPacketS2C
import hauveli.hexagony.networking.msg.SawWallScrollPatternPacketC2S
import hauveli.hexagony.registry.HexagonyAdvancements
import hauveli.hexagony.registry.HexagonyAdvancements.hasPatternKnowledge
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.*


object ScrungledPatternSending {
    @JvmField
    var storedPatterns: MutableMap<LookupPatternComponent, String> = mutableMapOf<LookupPatternComponent, String>()

    @JvmField
    var previousKeyRequest: String = "glorp"

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

    @JvmStatic
    fun fromClientThatJustSawWallScroll(resourceKey: String, uuid: String) {
        CHANNEL.clientHandle().send(SawWallScrollPatternPacketC2S(resourceKey, uuid))
    }

    fun doesThisPlayerHavePermission(resourceKey: String, serverPlayer: ServerPlayer) {
        if (hasPatternKnowledge(serverPlayer, resourceKey)) {
            val perWorldPattern = ScrungledPatternsSave.open(serverPlayer.serverLevel()).lookupReverse(
                HexActions.REGISTRY.getHolder(ResourceLocation.parse(resourceKey)).get().key()
            )
            if (perWorldPattern == null) return
            CHANNEL.serverHandle(serverPlayer).send(
                PerWorldPatternPacketS2C(resourceKey,
                    perWorldPattern.first.toAnglesString(),
                    perWorldPattern.second.canonicalStartDir().toString()))
        }
    }

    fun clientRenderThisNow(resourceKey: String, angles: String, startDir: String) {
        currentKey = resourceKey
        currentAngles = angles
        currentStartDir = HexDir.fromString(startDir)
        currentHexPattern = HexPattern.fromAngleString(currentAngles, currentStartDir)

        //storedPatterns[currentKey]?.render()
    }

    @JvmStatic
    fun singlePlayerHelper(resourceKey: String) {
        val scrungle = ScrungledPatternsSave
            .open(Minecraft.getInstance().singleplayerServer!!.overworld())
            .lookupReverse(HexActions.REGISTRY.getHolder(ResourceLocation.parse(resourceKey)).get().key())
        if (scrungle == null) return
        previousKeyRequest = resourceKey
        clientRenderThisNow(resourceKey, scrungle.first.toAnglesString(), scrungle.second.canonicalStartDir().toString())
    }

    fun getPatternResourceLocationStringFromEntityWallScroll(entityWallScroll: EntityWallScroll): String? {
        val resLoc =
            entityWallScroll.scroll.get(HexDataComponents.ACTION.get())?.location() ?: return null
        return resLoc.toString()
    }

    fun getWallScrollPatternButOnlyIfVisible(entityWallScroll: Entity?): String? {
        if (entityWallScroll !is EntityWallScroll || !entityWallScroll.showsStrokeOrder)
            return null
        val maybePattern = getPatternResourceLocationStringFromEntityWallScroll(entityWallScroll) ?: return null
        return maybePattern
    }

    fun didThisPlayerSeeSomeRealWallScroll(serverPlayer: ServerPlayer, resourceKey: String, uuid: String) {
        val maybeEntity = serverPlayer.serverLevel().getEntity(UUID.fromString(uuid))
        val maybePattern = getWallScrollPatternButOnlyIfVisible(maybeEntity) ?: return
        if (maybePattern != resourceKey)
            return
        // so the reason I decided not to check distance is that the player would already have to know the UUID of the wall scroll entity
        // this is such a ridiculous scenario to be able to cheat for that I think it's not worth the extra cpu time, likely ever...
        // (how would they know the UUID AND the pattern, but not have their client ever know the stroke order???)
        /*
        val entityTrackingDistance = serverPlayer.server.getScaledTrackingDistance(1)
        if (maybeEntity.distanceToSqr(serverPlayer) > entityTrackingDistance * entityTrackingDistance)
            return

         */
        HexagonyAdvancements.grantPatternKnowledge(serverPlayer, maybePattern)
    }
}