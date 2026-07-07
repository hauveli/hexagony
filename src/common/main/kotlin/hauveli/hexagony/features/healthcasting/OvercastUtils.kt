package hauveli.hexagony.features.healthcasting

import hauveli.hexagony.Hexagony
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes

object OvercastUtils {

    // todo in future: abstract and move stuff around so that it's not hardcoded to overcast penalty
    // (iff I need it)

    val HEALTHCAST_PENALTY_ID = Hexagony.id("overcast_penalty")

    private fun newModifier(penalty: Double): AttributeModifier {
        return AttributeModifier(
            HEALTHCAST_PENALTY_ID,
            penalty,
            AttributeModifier.Operation.ADD_VALUE)
    }

    private fun getModifier(entity: LivingEntity): AttributeModifier {
        val maxHealth = getAttribute(entity)
        if (maxHealth != null) {
            val modifier = maxHealth.getModifier(HEALTHCAST_PENALTY_ID)
            if (modifier != null) {
                return modifier
            }
        }

        return newModifier(0.0)
    }

    private fun getAttribute(entity: LivingEntity): AttributeInstance? {
        return entity.getAttribute(Attributes.MAX_HEALTH)
    }

    private fun removeModifier(entity: LivingEntity) {
        val maxHealth = getAttribute(entity)
        maxHealth?.removeModifier(HEALTHCAST_PENALTY_ID)
    }

    private fun addModifier(entity: LivingEntity, modifier: AttributeModifier) {
        val maxHealth = getAttribute(entity)
        maxHealth!!.addPermanentModifier(modifier)
    }


    @JvmStatic
    fun getModifierValue(entity: LivingEntity): Double {
        return getModifier(entity).amount()
    }

    @JvmStatic
    fun setModifierValue(entity: LivingEntity, modifierValue: Double) {
        removeModifier(entity)
        addModifier(entity, newModifier(modifierValue))
    }

    @JvmStatic
    fun addModifierValue(entity: LivingEntity, value: Double) {
        val penalty = value + getModifierValue(entity)
        setModifierValue(entity, penalty)
    }
}
