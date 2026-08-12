package hauveli.hexagony.mixin.control;

import hauveli.hexagony.features.control.ControlledMobEffect;
import hauveli.hexagony.features.fake_player.FakeServerPlayer;
import hauveli.hexagony.registry.HexagonyMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffectInstance.class)
public class UncapAmplifierMobEffectInstanceMixin {
    @Shadow
    private int amplifier;

    @Inject(method = "<init>(Lnet/minecraft/core/Holder;IIZZZLnet/minecraft/world/effect/MobEffectInstance;)V", at = @At("TAIL"))
    private void afterInit(Holder effect, int par2, int par3, boolean par4, boolean par5, boolean par6, MobEffectInstance par7, CallbackInfo ci) {
        if (effect.value() instanceof ControlledMobEffect) {
            this.amplifier = amplifier;
        }
    }
}