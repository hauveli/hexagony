package hauveli.hexagony.mixin.freecam;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import hauveli.hexagony.features.control.ControlledMobEffects;
import hauveli.hexagony.features.freecam.FreeCameraClientData;
import hauveli.hexagony.features.freecam.FreeCameraEntity;
import hauveli.hexagony.registry.HexagonyMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
        LocalPlayer localPlayer = (LocalPlayer) (Object) this;
        if (FreeCameraEntity.Companion.getActive()
                && !localPlayer.hasEffect(ControlledMobEffects.INSTANCE.getSNEAK().getReal().holder())) {
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
    private boolean hexagony$letServerKnowImHere(boolean previous) {
        if (FreeCameraEntity.Companion.getActive()) {
            FreeCameraClientData.INSTANCE.sync(); // piggybacking hehehehehe.... I hope I won't forget this in the future...!
            // for future self: I'm just syncing the lookdir and pos from here, I think my reasoning was that it's for the freecam anyway so many as well
            return true;
        }
        return previous;
    }

    @Unique
    private Boolean hexagony$timedToggle = false;

    @Unique
    private Vec3 hexagony$pos = Vec3.ZERO;
    @Unique
    private Vec3 hexagony$lookdir = Vec3.ZERO;

}
