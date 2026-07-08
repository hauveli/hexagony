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

@Mixin (value = Mishap.class, remap = false)
public abstract class ApplyPenaltyAtHeadTrulyHurtMixin {

    @Inject(method = "trulyHurt", at = @At("HEAD"))
    private static void onTrulyHurt(LivingEntity entity, DamageSource source, float amount, CallbackInfo ci) {
        if (entity.level().isClientSide) return;

        HexagonyCommonConfig conf = HexagonyConfigs.INSTANCE.getCOMMON_CONFIG();
        HexagonyCommonConfig.HealthcastingOption penalty = conf.getHealthcastingPenalty();
        HexagonyCommonConfig.HealthcastingOption damage = conf.getHealthcastingDamage();

        double healthLossAfterSettings = OvercastUtils.resolveValuePenalty(entity, amount, penalty.getPerInstance());
        OvercastUtils.addModifierValue(entity, -healthLossAfterSettings - penalty.getAdditional());

        double damageAfterSettings = OvercastUtils.resolveValueDamage(entity, amount, damage.getPerInstance());
        entity.hurt(source, (float) (damageAfterSettings + damage.getAdditional()));

        entity.setHealth(Math.min(Math.max(0, entity.getMaxHealth()), entity.getHealth()));

        // previous line(s) may have killed the player
        if (entity.getMaxHealth() <= 0 && !entity.isDeadOrDying()) {
            entity.die(source);
        }
    }
}