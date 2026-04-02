package hauveli.hexagony.mixin.bilocation;

import hauveli.hexagony.common.bilocation.FreeCameraEntity;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class BilocationAntiSneakLocalPlayerMixin {

    @Inject(method = "isShiftKeyDown", at = @At("HEAD"), cancellable = true)
    private void hexagony$disableSneak(CallbackInfoReturnable<Boolean> cir) {
        if (FreeCameraEntity.Companion.getActive()) {
            cir.cancel();
        }
    }
}
