package hauveli.hexagony.mixin.freecam;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import hauveli.hexagony.features.freecam.FreeCameraClientData;
import hauveli.hexagony.features.freecam.FreeCameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class AntiSneakLocalPlayerMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Inject(method = "isShiftKeyDown", at = @At("HEAD"), cancellable = true)
    private void hexagony$disableSneak(CallbackInfoReturnable<Boolean> cir) {
        if (FreeCameraEntity.Companion.getActive()) {
            cir.cancel();
        }
    }

    /*
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isFlyingLocked()Z"))
    private boolean lockFlyingForCamera(boolean previous) {
        if (playerEntityInputsDisabled) return true;
        return previous;
    }
     */

    @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isControlledCamera()Z"))
    private boolean letServerKnowImHere(boolean previous) {
        if (FreeCameraEntity.Companion.getActive()) {
            FreeCameraClientData.INSTANCE.sync(); // piggybacking hehehehehe.... I hope I won't forget this in the future...!
            return true;
        }
        return previous;
    }

    private Boolean timedToggle = false;

    private Vec3 pos = Vec3.ZERO;
    private Vec3 lookdir = Vec3.ZERO;

}
