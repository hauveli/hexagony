package hauveli.hexagony.features.freecam

import at.petrak.hexcasting.common.lib.HexAttributes
import hauveli.hexagony.Hexagony
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier

// https://docs.neoforged.net/docs/items/mobeffects/
class FreeCameraMobEffect(category: MobEffectCategory, color: Int) : MobEffect(category, color) {
    fun whenGained(livingEntity: LivingEntity) {
        if (livingEntity.level().isClientSide) {
            if (!FreeCameraEntity.active) {
                FreeCameraEntity.detachCamera()
            }
        }
    }

    fun whenExpired(livingEntity: LivingEntity) {
        if (livingEntity.level().isClientSide) {
            if (FreeCameraEntity.active) {
                FreeCameraEntity.reattachCamera()
            }
        } else {
            // I might as well be nice to people who apply this to non-players by not erroring....
            // I do this so I can have consistent behavior on the first few frames if the client is lagging,
            // but I really don't like what I'm doing anyway.
            // I understand there's not much I can do to control latency related issues but this is the best I could think of
            // with the knowledge and effort I put in.
            if (livingEntity is ServerPlayer) {
                FreeCameraServerData.clearData(livingEntity)
            }
        }
    }

    override fun onMobRemoved(livingEntity: LivingEntity, p1: Int, p2: Entity.RemovalReason) {
        super.onMobRemoved(livingEntity, p1, p2)
        whenExpired(livingEntity)
    }

    override fun applyEffectTick(livingEntity: LivingEntity, amplifier: Int): Boolean {
        whenGained(livingEntity)
        // whenExpired(livingEntity)
        return true
    }

    override fun shouldApplyEffectTickThisTick(tickCount: Int, amplifier: Int): Boolean {
        return true
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