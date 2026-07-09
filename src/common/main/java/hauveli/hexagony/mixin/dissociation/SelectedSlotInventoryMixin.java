package hauveli.hexagony.mixin.dissociation;

import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static hauveli.hexagony.features.freecam.FreeCam.ClientSideData.playerEntityInputsDisabled;

@Mixin(Inventory.class)
public abstract class SelectedSlotInventoryMixin {

    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void hexagony$preventSlotChange(double direction, CallbackInfo ci) {
        if (playerEntityInputsDisabled) {
            ci.cancel();
        }
    }

}
