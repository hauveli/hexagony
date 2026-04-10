package hauveli.hexagony.common.misc;

import hauveli.hexagony.Hexagony
import net.minecraft.advancements.Advancement
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object AdvancementProvider {
    val TREPIDATION = Hexagony.id("graft_attempted")
    val TREPANNED = Hexagony.id("graft_succeeded")


    @JvmStatic
    fun grantAdvancement(player: ServerPlayer, advancementId: String) {
        val server: MinecraftServer = player.server ?: return
        val advancement: Advancement = server.advancements.getAdvancement(
            ResourceLocation(Hexagony.MODID, advancementId)
        ) ?: return
        val progress = player.advancements.getOrStartProgress(advancement)
        if (!progress.isDone) {
            for (criterion in progress.remainingCriteria) {
                player.advancements.award(advancement, criterion)
            }
        }
    }

    @JvmStatic
    fun grantAdvancement(player: ServerPlayer, advancement: ResourceLocation) {
        val server: MinecraftServer = player.server ?: return
        val advancement: Advancement = server.advancements.getAdvancement(advancement) ?: return
        val progress = player.advancements.getOrStartProgress(advancement)
        if (!progress.isDone) {
            for (criterion in progress.remainingCriteria) {
                player.advancements.award(advancement, criterion)
            }
        }
    }



    fun hasAdvancement(serverPlayer: ServerPlayer, advancement: ResourceLocation): Boolean {
        val adv = serverPlayer.getServer()?.advancements?.getAdvancement(advancement) ?: return false

        return serverPlayer.advancements.getOrStartProgress(adv).isDone
    }

    @JvmStatic
    fun isTrepanned(serverPlayer: ServerPlayer): Boolean {
        return hasAdvancement(serverPlayer, TREPANNED)
    }
}