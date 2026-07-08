package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import hauveli.hexagony.mixin.enlightenment.ClientAdvancementsProgressAccessor
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementProgress
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientAdvancements
import net.minecraft.resources.ResourceLocation
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
        val advancement: AdvancementHolder = serverPlayer.server.advancements.get(advancementResLoc) ?: return
        val progress = serverPlayer.advancements.getOrStartProgress(advancement)
        for (criterion in progress.remainingCriteria) {
            serverPlayer.advancements.revoke(advancement, criterion)
        }
    }

    fun getAdvancement(serverPlayer: ServerPlayer, advancement: ResourceLocation): AdvancementHolder? {
        return serverPlayer.getServer()?.advancements?.get(advancement)
    }

    fun getAdvancementProgress(serverPlayer: ServerPlayer, advancementHolder: AdvancementHolder): AdvancementProgress {
        return serverPlayer.advancements.getOrStartProgress(advancementHolder)
    }

    @JvmStatic
    fun hasAdvancement(serverPlayer: ServerPlayer, advancement: ResourceLocation): Boolean {
        val advancementHolder = getAdvancement(serverPlayer, advancement) ?: return false
        return getAdvancementProgress(serverPlayer, advancementHolder ).isDone
    }

    fun getAdvancement(advancement: ResourceLocation): AdvancementHolder? {
        val mc = Minecraft.getInstance()
        val advancements: ClientAdvancements = mc.connection!!.advancements
        return advancements.get(advancement)
    }

    fun getAdvancementProgress(advancementHolder: AdvancementHolder): AdvancementProgress? {
        val mc = Minecraft.getInstance()
        val advancements: ClientAdvancements = mc.connection!!.advancements
        val progress: MutableMap<AdvancementHolder?, AdvancementProgress?>? =
            (advancements as ClientAdvancementsProgressAccessor).getProgress()
        return progress?.get(advancementHolder)
    }

    @JvmStatic
    fun hasAdvancement(advancement: ResourceLocation): Boolean {
        val advancementHolder = getAdvancement(advancement) ?: return false
        return getAdvancementProgress(advancementHolder)?.isDone ?: return false
    }

    @JvmStatic
    fun isTrepanned(serverPlayer: ServerPlayer): Boolean {
        return hasAdvancement(serverPlayer, TREPANNED)
    }

    // I don't like this, but it's not called often so whatever, but I should maybe cache the stuff as bools...
    private const val SCROLL_ADVANCEMENT_PREFIX: String = "hexagony:gated/"
    private fun spellLocToAdvancementLoc(resourceKey: String): ResourceLocation {
        val advancementResLoc = SCROLL_ADVANCEMENT_PREFIX +
            resourceKey
                .replace("/", "_")
                .replace(":", "/")
        return ResourceLocation.parse(advancementResLoc)
    }

    @JvmStatic
    fun hasHeldScroll(resourceKey: String): Boolean {
        return hasAdvancement(spellLocToAdvancementLoc(resourceKey))
    }

    @JvmStatic
    fun hasHeldScroll(serverPlayer: ServerPlayer, resourceKey: String): Boolean {
        Hexagony.LOGGER.info("server: {}", resourceKey)
        return hasAdvancement(serverPlayer, spellLocToAdvancementLoc(resourceKey))
    }
}