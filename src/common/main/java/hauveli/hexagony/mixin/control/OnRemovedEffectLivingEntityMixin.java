package hauveli.hexagony.mixin.control;

import hauveli.hexagony.Hexagony;
import hauveli.hexagony.features.control.ControlledMobEffect;
import hauveli.hexagony.features.freecam.FreeCameraEntity;
import hauveli.hexagony.registry.HexagonyMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class OnRemovedEffectLivingEntityMixin {
    @Inject(method = "onEffectRemoved", at = @At("HEAD"))
    private void hexagony$stopControllingMobEffectInstantly(MobEffectInstance effect, CallbackInfo ci) {
        if (effect.getEffect().value() instanceof ControlledMobEffect cme) {
            LivingEntity le = (LivingEntity) (Object) this;
            cme.getActionStopped().invoke(le);
        }
    }
}