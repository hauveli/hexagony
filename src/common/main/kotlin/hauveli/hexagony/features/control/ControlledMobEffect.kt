package hauveli.hexagony.features.control

import at.petrak.hexcasting.common.lib.HexAttributes
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.freecam.FreeCameraEntity
import hauveli.hexagony.features.freecam.FreeCameraServerData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier

// https://docs.neoforged.net/docs/items/mobeffects/
class ControlledMobEffect(private val actionTaken: ((LivingEntity) -> Unit), private val intervalInTicks: Int, category: MobEffectCategory, color: Int) : MobEffect(category, color) {
    override fun onMobRemoved(livingEntity: LivingEntity, p1: Int, p2: Entity.RemovalReason) {
        super.onMobRemoved(livingEntity, p1, p2)
        // whenExpired(livingEntity)
    }

    override fun applyEffectTick(livingEntity: LivingEntity, amplifier: Int): Boolean {
        // whenGained(livingEntity)
        // whenExpired(livingEntity)
        actionTaken(livingEntity)
        return true
    }

    override fun shouldApplyEffectTickThisTick(tickCount: Int, amplifier: Int): Boolean {
        return tickCount % amplifier == 0
    }

    // Utility method that is called when the effect is first added to the entity.
    // This does not get called again until all instances of this effect have been removed from the entity.
    override fun onEffectAdded(entity: LivingEntity, amplifier: Int) {
        super.onEffectAdded(entity, amplifier)
    }

    // Utility method that is called when the effect is added to the entity.
    // This gets called every time this effect is added to the entity.
    override fun onEffectStarted(entity: LivingEntity, amplifier: Int) {
    }
}