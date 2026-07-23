package hauveli.hexagony.features.control

import at.petrak.hexcasting.common.lib.HexAttributes
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.freecam.FreeCameraEntity
import hauveli.hexagony.features.freecam.FreeCameraServerData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier

// why is there no duration hits zero function?
// https://docs.neoforged.net/docs/items/mobeffects/
class ControlledMobEffect(
    private val actionTaken: ((LivingEntity, Int) -> Unit),
    val actionStopped: ((LivingEntity) -> Unit),
    private val amplifierIsNotInterval: Boolean,
    color: Int)
    : MobEffect(MobEffectCategory.NEUTRAL, color) {

    // Hmmm, todo: always remove conflicting instances of this effect when applying it via a spell
    // best to do it there to avoid a bunch of extra junk here?
    // I should make a helper function, though...
    // or maybe an onEffectAdded method? would need yet ANOTHER argument for it, but it would make things a little less headachey...
    // plus, not many of the effects have conflicts anyway....
    // main issue is when amplifier is wrong... actually that's like all of them...
    // so maybe I just make a generic "remove same", and then make another that does "remove conflicting"?
    // alt. make walk forward/backward the same, and use amplifier 0,1,2
    // that sounds like it would be simplest...
    //
    // todo actually:
    // actually I think I'll just remove the effect via the spell, and use a helper method for it if anything after all...

    override fun onMobRemoved(livingEntity: LivingEntity, p1: Int, p2: Entity.RemovalReason) {
        super.onMobRemoved(livingEntity, p1, p2)
        // whenExpired(livingEntity)
    }

    override fun applyEffectTick(livingEntity: LivingEntity, amplifier: Int): Boolean {
        // whenGained(livingEntity)
        // whenExpired(livingEntity)
        actionTaken(livingEntity, amplifier)
        return true
    }

    override fun shouldApplyEffectTickThisTick(tickCount: Int, amplifier: Int): Boolean {
        if (amplifierIsNotInterval) return true
        return tickCount % (amplifier + 1) == 0
    }

    // Utility method that is called when the effect is first added to the entity.
    // This does not get called again until all instances of this effect have been removed from the entity.
    override fun onEffectAdded(entity: LivingEntity, amplifier: Int) {
        super.onEffectAdded(entity, amplifier)
        // todo: remove same effects from this entity... how?
        if (false) {

        }
    }

    // Utility method that is called when the effect is added to the entity.
    // This gets called every time this effect is added to the entity.
    override fun onEffectStarted(entity: LivingEntity, amplifier: Int) {
    }
}