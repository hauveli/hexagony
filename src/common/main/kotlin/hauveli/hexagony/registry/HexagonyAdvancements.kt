package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementProgress
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object HexagonyAdvancements {
    val TREPIDATION = Hexagony.id("graft_attempted")
    val TREPANNED = Hexagony.id("graft_succeeded")
    val GRAPHTING = Hexagony.id("craft")

    @JvmStatic
    fun tryGrantingAdvancement(serverPlayer: ServerPlayer, advancementResLoc: ResourceLocation) {
        val advancement: AdvancementHolder = serverPlayer.server.advancements.get(advancementResLoc) ?: return
        val progress = serverPlayer.advancements.getOrStartProgress(advancement)
        if (progress.isDone) return
        for (criterion in progress.remainingCriteria) {
            serverPlayer.advancements.award(advancement, criterion)
        }
    }

    @JvmStatic
    fun tryGrantingAdvancement(serverPlayer: ServerPlayer, advancementId: String) {
        val advancementResLoc = Hexagony.id(advancementId)
        val advancement: AdvancementHolder = serverPlayer.server.advancements.get(advancementResLoc) ?: return
        val progress = serverPlayer.advancements.getOrStartProgress(advancement)
        if (progress.isDone) return
        for (criterion in progress.remainingCriteria) {
            serverPlayer.advancements.award(advancement, criterion)
        }
    }

    @JvmStatic
    fun tryRevokingAdvancement(serverPlayer: ServerPlayer, advancementResLoc: ResourceLocation) {
        val advancement: AdvancementHolder = checkNotNull(serverPlayer.server.advancements.get(advancementResLoc))
        val progress = serverPlayer.advancements.getOrStartProgress(advancement)
        for (criterion in progress.remainingCriteria) {
            serverPlayer.advancements.revoke(advancement, criterion)
        }
    }

    fun getAdvancement(serverPlayer: ServerPlayer, advancement: ResourceLocation): Advancement? {
        return serverPlayer.getServer()?.advancements?.get(advancement)?.value()
    }

    fun getAdvancementProgress(serverPlayer: ServerPlayer, advancementResLoc: ResourceLocation): AdvancementProgress? {
        val advancement: AdvancementHolder = checkNotNull(serverPlayer.server.advancements.get(advancementResLoc))
        return serverPlayer.advancements.getOrStartProgress(advancement)
    }

    fun hasAdvancement(serverPlayer: ServerPlayer, advancement: ResourceLocation): Boolean {
        return getAdvancementProgress(serverPlayer, advancement)?.isDone ?: false
    }

    @JvmStatic
    fun isTrepanned(serverPlayer: ServerPlayer): Boolean {
        return hasAdvancement(serverPlayer, TREPANNED)
    }
}