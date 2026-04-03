package hauveli.hexagony.mixin.bilocation;

import hauveli.hexagony.common.bilocation.FreeCameraEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(Minecraft.class)
public abstract class BilocationNoInputMinecraftMixin {

    @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
    private void hexagony$disableInputs(CallbackInfo ci) {
        if (FreeCameraEntity.Companion.getActive()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.keyDrop.isDown()) {
                mc.options.keyDrop.consumeClick();
            }
            if (mc.options.keyAttack.isDown()) {
                mc.options.keyAttack.consumeClick();
            }
            if (mc.options.keyUse.isDown()) {
                mc.options.keyUse.consumeClick();
            }
            Arrays.stream(mc.options.keyHotbarSlots).forEach(
                    keyMapping -> {
                        if (keyMapping.isDown()) {
                            keyMapping.consumeClick();
                        }
                    }
            );
            if (mc.options.keyChat.isDown()) return;
            ci.cancel();
        }
    }
}