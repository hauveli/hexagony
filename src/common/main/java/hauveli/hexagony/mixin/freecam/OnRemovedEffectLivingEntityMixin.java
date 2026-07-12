package hauveli.hexagony.mixin.freecam;

import hauveli.hexagony.features.freecam.FreeCameraEntity;
import hauveli.hexagony.registry.HexagonyMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class OnRemovedEffectLivingEntityMixin {
    @Inject(method = "removeEffectNoUpdate", at = @At("HEAD"))
    private void hexagony$removeFreeCam(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
        LivingEntity le = (LivingEntity) (Object) this;
        if (effect.value().equals(HexagonyMobEffects.FREECAM.getValue()) && le.level().isClientSide) {
            FreeCameraEntity.Companion.reattachCamera();
        }
    }
}