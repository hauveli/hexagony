package hauveli.hexagony.mixin.dissociation;

import hauveli.hexagony.features.freecam.FreeCameraEntity;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class SelectedSlotInventoryMixin {

    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void hexagony$preventSlotChange(double direction, CallbackInfo ci) {
        if (FreeCameraEntity.Companion.getActive()) {
            ci.cancel();
        }
    }

}
