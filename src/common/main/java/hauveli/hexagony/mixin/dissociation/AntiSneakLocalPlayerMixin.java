package hauveli.hexagony.mixin.dissociation;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import hauveli.hexagony.Hexagony;
import hauveli.hexagony.features.freecam.FreeCameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static hauveli.hexagony.Hexagony.MINECRAFT;

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
        assert MINECRAFT != null;
        if (MINECRAFT.level.getGameTime() % 200 == 0) {
            timedToggle = !timedToggle;
            Hexagony.LOGGER.info("Swapping: {}", timedToggle);
            if (timedToggle) {
                FreeCameraEntity.Companion.detachCamera();
            } else {
                FreeCameraEntity.Companion.reattachCamera();
            }
        }
        if (FreeCameraEntity.Companion.getActive()) return true;
        return previous;
    }

    private Boolean timedToggle = false;

    private Vec3 pos = Vec3.ZERO;
    private Vec3 lookdir = Vec3.ZERO;

}
