package hauveli.hexagony.mixin.freecam;

import hauveli.hexagony.features.freecam.FreeCameraEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

import static hauveli.hexagony.client.HexagonyClient.MINECRAFT;

@Mixin(Minecraft.class)
public abstract class NoInputMinecraftMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
    private void hexagony$disableInputs(CallbackInfo ci) {
        if (FreeCameraEntity.Companion.getActive()) {
            assert MINECRAFT != null;
            if (allowedKeys == null) {
                allowedKeys = List.of(
                        MINECRAFT.options.keyChat,
                        MINECRAFT.options.keyFullscreen,
                        MINECRAFT.options.keySocialInteractions,
                        MINECRAFT.options.keyScreenshot,
                        MINECRAFT.options.keySmoothCamera,
                        MINECRAFT.options.keyPlayerList,
                        MINECRAFT.options.keyJump,
                        MINECRAFT.options.keyShift,
                        MINECRAFT.options.keyUp,
                        MINECRAFT.options.keyDown,
                        MINECRAFT.options.keyLeft,
                        MINECRAFT.options.keyRight
                );
            }
            if (disallowedKeys == null) {
                disallowedKeys = List.of(
                        MINECRAFT.options.keyUse,
                        MINECRAFT.options.keyAttack
                );
            }
            /*
            Arrays.stream(mc.options.keyHotbarSlots).forEach(
                    keyMapping -> {
                        if (keyMapping.isDown()) {
                            keyMapping.consumeClick();
                        }
                    }
            );
             */
            Arrays.stream(MINECRAFT.options.keyMappings).forEach(
                    keyMapping -> {
                        if (!allowedKeys.contains(keyMapping)) {
                            keyMapping.consumeClick();
                            keyMapping.setDown(false);
                        }
                    }
            );

        }

    }


    @Inject(method = "disconnect()V", at = @At("HEAD"))
    private void hexagony$fixCameraOnLeave(CallbackInfo ci) {
        FreeCameraEntity.Companion.onLeave();
    }

    @Unique
    private static List<KeyMapping> disallowedKeys = null;

    @Unique
    private static List<KeyMapping> allowedKeys = null;
}