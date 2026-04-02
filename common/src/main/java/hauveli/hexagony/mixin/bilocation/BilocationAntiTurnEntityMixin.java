package hauveli.hexagony.mixin.bilocation;

import hauveli.hexagony.common.bilocation.FreeCameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Entity.class)
public abstract class BilocationAntiTurnEntityMixin {

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void hexagony$redirectTurn(double yaw, double pitch, CallbackInfo ci) {
        if ((Object)this == Minecraft.getInstance().player) {
            if (FreeCameraEntity.Companion.getActive()) {
                Objects.requireNonNull(FreeCameraEntity.Companion.getFreeCam()).turn(yaw, pitch);
                ci.cancel();
            }
        }
    }
}