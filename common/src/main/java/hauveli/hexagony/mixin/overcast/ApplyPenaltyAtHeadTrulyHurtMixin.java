package hauveli.hexagony.mixin.overcast;

import at.petrak.hexcasting.api.casting.mishaps.Mishap;

import hauveli.hexagony.config.HexagonyServerConfig;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/*
 * This mixin applies a penalty to players when they healthcast, lowering their maximum HP by an amount configured
 * via the variable maximumHealthLossMultiplier, which is a double.
 * 0 means disabled, 1 means 1 max health lost per damage taken, 3 means 3 max health lost per damage taken
 * Example: maximumHealthLossMultiplier = 1.75, damage taken 4, 4 * 1.75 = 7, so 7 max health lost.
 * I recommend keeping maximumHealthLossMultiplier <= overcastDamageMultiplier
 * overcastDamageMultiplier is added to the damage taken, and is not used for calculating maximum health loss.
 * As such, while overcastDamageMultiplier = 0 will work, the player will still lose maximum health, if configured.
 */
@Mixin (value = Mishap.class, remap = false)
public abstract class ApplyPenaltyAtHeadTrulyHurtMixin {

    @Inject(method = "trulyHurt", at = @At("HEAD"))
    private static void onTrulyHurt(LivingEntity entity, DamageSource source, float amount, CallbackInfo ci) {
        if (entity.level().isClientSide) return;
        double mhp = HexagonyServerConfig.getConfig().getMaximumHealthPenaltyMultiplier();
        double odp = HexagonyServerConfig.getConfig().getOvercastDamagePenaltyMultiplier();
        double adp = HexagonyServerConfig.getConfig().getOvercastDamagePenaltyAdditionalDamage();
        hexagony$applyModifier(entity, amount, mhp);

        // apply overcast penalty multiplier
        double extraDamageTaken = adp + (odp - 1) * amount;
        entity.hurt(source, (float) extraDamageTaken);

        // Set entity health to whatever it should be if needed
        if (odp > mhp) {
            entity.setHealth(Math.min(Math.max(0, entity.getMaxHealth()), entity.getHealth()));
        }

        // Previous
        if (entity.getMaxHealth() <= 0) {
            entity.die(source);
        }
    }

    @Unique
    static private void hexagony$applyModifier(LivingEntity entity, float healthToRemove, Double maxHealthPenaltyMultiplier) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;

        // add as many penalties as hearts in damage.
        for (int i = 0; i < healthToRemove; i++) {
            AttributeModifier modifier = new AttributeModifier(
                    UUID.randomUUID(),
                    HexagonyServerConfig.getConfig().getOvercastAttributeName(),
                    -maxHealthPenaltyMultiplier,
                    AttributeModifier.Operation.ADDITION
            );
            maxHealth.addPermanentModifier(modifier);
        }
    }
}