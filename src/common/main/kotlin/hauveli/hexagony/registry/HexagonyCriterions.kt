package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.enlightenment.HasHeldPatternTrigger
import me.fzzyhmstrs.fzzy_config.util.FcText.description
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.advancements.CriterionTrigger
import net.minecraft.advancements.critereon.SimpleCriterionTrigger
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block

object HexagonyCriterions : HexagonyRegistrar<CriterionTrigger<*>>(
    BuiltInRegistries.TRIGGER_TYPES.key() as ResourceKey<Registry<CriterionTrigger<*>>>,
    { BuiltInRegistries.TRIGGER_TYPES }
) {
    fun <T> register(path: String, trigger: T): T
            where T : SimpleCriterionTrigger<*> {
        return CriteriaTriggers.register(path, trigger)
    }

    val HAS_HELD_PATTERN = make("has_held_pattern", {HasHeldPatternTrigger()})
    fun onInventoryChange(serverPlayer: ServerPlayer, itemStack: ItemStack) {
        HAS_HELD_PATTERN.value.trigger(serverPlayer, itemStack)
    }

    private fun <T : CriterionTrigger<*>> make(name: String, builder: () -> T): HexagonyRegistrar<CriterionTrigger<*>>.Entry<T> =
        register(Hexagony.id(name), builder)

    // fabric docs say I need to run this on init to make things work even if its blank, to load the class, which makes sense
    fun init() {}
}