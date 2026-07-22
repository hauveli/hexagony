package hauveli.hexagony.registry

import at.petrak.hexcasting.common.lib.HexAttributes
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.control.ControlledMobEffect
import hauveli.hexagony.features.control.ControlledMobEffects
import hauveli.hexagony.features.control.ControlledMobEffects.makeControlEffect
import hauveli.hexagony.features.control.FakePlayerActions
import hauveli.hexagony.features.control.FakePlayerActions.look
import hauveli.hexagony.features.freecam.FreeCameraMobEffect
import hauveli.hexagony.features.mind_anchor.item.ItemMindAnchor
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.core.Registry
import net.minecraft.network.chat.TextColor
import net.minecraft.resources.ResourceKey
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.Rarity

object HexagonyMobEffects : HexagonyRegistrar<MobEffect>(
    BuiltInRegistries.MOB_EFFECT.key() as ResourceKey<Registry<MobEffect>>,
    { BuiltInRegistries.MOB_EFFECT }
) {
    init {
        ControlledMobEffects.init()
    }

    // for dissociation
    @JvmField
    val FREECAM = make("freecam") {
        FreeCameraMobEffect(MobEffectCategory.NEUTRAL, 0)
            .addAttributeModifier(HexAttributes.FEEBLE_MIND,
                Hexagony.id("effect.freecam"), 2.0, AttributeModifier.Operation.ADD_VALUE)
            // .withSoundOnAdded()
    }

    // todo: make this less bad, I want to give it just the MobEffect...?
    fun <T : MobEffect> getInstance(effect: HexagonyRegistrar<MobEffect>.Entry<T>, durationInTicks: Int, amplifier: Int): MobEffectInstance {
        return MobEffectInstance(
            effect.holder(),
            durationInTicks,
            amplifier,
            false,  // ambient effect?
            false,  // visible in inventory?
            false // visible in top right corner?
        )
    }

    fun <T : MobEffect> make(name: String, builder: () -> T): HexagonyRegistrar<MobEffect>.Entry<T> =
        register(Hexagony.id(name), builder)
}