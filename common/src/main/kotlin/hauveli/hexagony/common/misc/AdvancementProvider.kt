package hauveli.hexagony.common.misc;

import hauveli.hexagony.Hexagony
import net.minecraft.advancements.Advancement
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object AdvancementProvider {
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

}