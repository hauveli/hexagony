package hauveli.hexagony.mixin.overcast;

import at.petrak.hexcasting.api.casting.mishaps.Mishap;

import hauveli.hexagony.features.healthcasting.OvercastUtils;
import hauveli.hexagony.config.HexagonyCommonConfig;
import hauveli.hexagony.config.HexagonyConfigs;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * This mixin applies a penalty to players when they healthcast, lowering their maximum HP by an amount configured
 * via the variable maximumHealthPenaltyMultiplier, which is a double.
 * 0 means disabled, 1 means 1 max health lost per damage taken, 3 means 3 max health lost per damage taken
 * Example: maximumHealthPenaltyMultiplier = 1.75, damage taken 4, 4 * 1.75 = 7, so 7 max health lost.
 * I recommend keeping maximumHealthPenaltyMultiplier <= overcastDamagePenaltyMultiplier
 * overcastDamagePenaltyMultiplier is added to the damage taken, and is not used for calculating maximum health loss.
 * As such, while overcastDamagePenaltyMultiplier = 0 will work, the player will still lose maximum health, if configured.
 */
@Mixin (value = Mishap.class, remap = false)
public abstract class ApplyPenaltyAtHeadTrulyHurtMixin {

    @Inject(method = "trulyHurt", at = @At("HEAD"))
    private static void onTrulyHurt(LivingEntity entity, DamageSource source, float amount, CallbackInfo ci) {
        if (entity.level().isClientSide) return;
        HexagonyCommonConfig conf = HexagonyConfigs.INSTANCE.getCOMMON_CONFIG();
        // not making these static because it would make it way harder for me to keep track of later
        // also hoping that this compiles to just getX and not two accesses, I'm not sure how java or kotlin work but I am curious
        double mhp = conf.getMaximumHealthPenaltyMultiplier().get();
        double odp = conf.getOvercastDamagePenaltyMultiplier().get();
        double adp = conf.getOvercastDamagePenaltyAdditionalDamage().get();
        OvercastUtils.addModifierValue(entity, -mhp);

        // apply overcast penalty multiplier
        double extraDamageTaken = adp + (odp - 1) * amount;
        entity.hurt(source, (float) extraDamageTaken);

        // sets the entity health to whatever it should be if needed (ie. 0 if their max health is below 0, or their current health if it is higher than 0
        // hmmm, I think the sign is flipped here but I'm not sure
        if (odp > mhp) {
            entity.setHealth(Math.min(Math.max(0, entity.getMaxHealth()), entity.getHealth()));
        }

        // previous line(s) may have killed the player
        if (entity.getMaxHealth() <= 0 && !entity.isDeadOrDying()) {
            entity.die(source);
        }
    }

}