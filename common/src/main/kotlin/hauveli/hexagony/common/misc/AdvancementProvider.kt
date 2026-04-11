package hauveli.hexagony.common.misc;

import hauveli.hexagony.Hexagony
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementProgress
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object AdvancementProvider {
    val TREPIDATION = Hexagony.id("graft_attempted")
    val TREPANNED = Hexagony.id("graft_succeeded")
    val GRAPHTING = Hexagony.id("craft")


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
    fun grantAdvancement(player: ServerPlayer, advancementId: ResourceLocation) {
        val server: MinecraftServer = player.server ?: return
        val advancement: Advancement = server.advancements.getAdvancement(advancementId) ?: return
        val progress = player.advancements.getOrStartProgress(advancement)
        if (!progress.isDone) {
            for (criterion in progress.remainingCriteria) {
                player.advancements.award(advancement, criterion)
            }
        }
    }

    fun getAdvancement(serverPlayer: ServerPlayer, advancement: ResourceLocation): Advancement? {
        return serverPlayer.getServer()?.advancements?.getAdvancement(advancement)
    }

    fun getAdvancementProgress(serverPlayer: ServerPlayer, advancement: ResourceLocation): AdvancementProgress? {
        val adv = getAdvancement(serverPlayer, advancement) ?: return null
        return serverPlayer.advancements.getOrStartProgress(adv)
    }

    fun hasAdvancement(serverPlayer: ServerPlayer, advancement: ResourceLocation): Boolean {
        return getAdvancementProgress(serverPlayer, advancement)?.isDone ?: false
    }

    // hmmm todo: make this less messy?

    @JvmStatic
    fun isTrepanned(serverPlayer: ServerPlayer): Boolean {
        return hasAdvancement(serverPlayer, TREPANNED)
    }
}