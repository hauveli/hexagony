package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.enlightenment.HasHeldPatternTrigger
import me.fzzyhmstrs.fzzy_config.util.FcText.description
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.advancements.critereon.SimpleCriterionTrigger
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack


object HexagonyCriterions {
    fun <T> register(path: String, trigger: T): T
            where T : SimpleCriterionTrigger<*> {
        return CriteriaTriggers.register(path, trigger)
    }

    val HAS_HELD_PATTERN: HasHeldPatternTrigger = CriteriaTriggers.register(
        Hexagony.id("has_held_pattern").toString(), // um... why?
        HasHeldPatternTrigger()
    )
    fun onInventoryChange(serverPlayer: ServerPlayer, itemStack: ItemStack) {
        HAS_HELD_PATTERN.trigger(serverPlayer, itemStack)
    }

    // fabric docs say I need to run this on init to make things work even if its blank, to load the class, which makes sense
    fun init() {}
}