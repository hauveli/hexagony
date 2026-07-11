package hauveli.hexagony.mixin.dissociation;

import hauveli.hexagony.features.freecam.FreeCameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public abstract class AntiTurnEntityMixin {

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void hexagony$redirectTurn(double yaw, double pitch, CallbackInfo ci) {
        if ((Object)this == Minecraft.getInstance().player) {
            if (FreeCameraEntity.Companion.getActive()) {
                FreeCameraEntity.Companion.getFreeCam().turn(yaw, pitch);
                ci.cancel();
            }

        }
    }
}