package hauveli.hexagony.registry

import at.petrak.hexcasting.common.lib.HexAttributes
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.freecam.FreeCameraMobEffect
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.ai.attributes.AttributeModifier

object HexagonySounds : HexagonyRegistrar<SoundEvent>(
    BuiltInRegistries.SOUND_EVENT.key() as ResourceKey<Registry<SoundEvent>>,
    { BuiltInRegistries.SOUND_EVENT }
) {
    // for dissociation, whatever some code duplication won't kill me even if I'm mad about it, I can fix that later
    @JvmField
    val FREECAM_BOUNCE = make("freecam.bounce") {
        SoundEvent.createVariableRangeEvent(Hexagony.id("freecam.bounce"))
    }

    private fun <T : SoundEvent> make(name: String, builder: () -> T): HexagonyRegistrar<SoundEvent>.Entry<T> =
        register(Hexagony.id(name), builder)
}