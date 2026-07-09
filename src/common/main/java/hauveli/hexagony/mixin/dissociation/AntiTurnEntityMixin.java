package hauveli.hexagony.mixin.dissociation;

import hauveli.hexagony.features.freecam.FreeCam;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static hauveli.hexagony.features.freecam.FreeCam.ClientSideData.playerEntityInputsDisabled;

@Mixin(Entity.class)
public abstract class AntiTurnEntityMixin {

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void hexagony$redirectTurn(double yaw, double pitch, CallbackInfo ci) {
        if ((Object)this == Minecraft.getInstance().player) {
            /*
            if (playerEntityInputsDisabled) {
                FreeCam.ClientSideData.turn(yaw, pitch);
                ci.cancel();
            }

             */
        }
    }
}