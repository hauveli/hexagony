package hauveli.hexagony.mixin.bilocation;

import hauveli.hexagony.common.bilocation.FreeCameraEntity;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class BilocationNoInputMinecraftMixin {

    @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
    private void hexagony$disableInputs(CallbackInfo ci) {
        if (FreeCameraEntity.Companion.getActive()) {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.options.keyChat.isDown())
                ci.cancel();
        }
    }
}