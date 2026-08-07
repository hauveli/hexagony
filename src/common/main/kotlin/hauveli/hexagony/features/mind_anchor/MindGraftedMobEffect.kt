package hauveli.hexagony.features.mind_anchor

import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.freecam.FreeCameraEntity
import hauveli.hexagony.features.freecam.FreeCameraServerData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity

// https://docs.neoforged.net/docs/items/mobeffects/
class MindGraftedMobEffect(category: MobEffectCategory, color: Int) : MobEffect(category, color) {

    override fun onMobRemoved(livingEntity: LivingEntity, p1: Int, p2: Entity.RemovalReason) {
        super.onMobRemoved(livingEntity, p1, p2)
        // todo: in a nice and kind and friendly world, I can just check if conditions are met in this function
        // I sure hope so at least....
        Hexagony.LOGGER.info("ERM did I just die??? FUCK")
        // whenExpired(livingEntity)
    }

    override fun applyEffectTick(livingEntity: LivingEntity, amplifier: Int): Boolean {
        // whenGained(livingEntity)
        // whenExpired(livingEntity)
        MindAnchorManager.perSecond(livingEntity, amplifier)
        return true
    }

    override fun shouldApplyEffectTickThisTick(tickCount: Int, amplifier: Int): Boolean {
        return tickCount % 20 == 0
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