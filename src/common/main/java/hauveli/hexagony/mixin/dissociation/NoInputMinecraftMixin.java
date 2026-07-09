package hauveli.hexagony.mixin.dissociation;

import hauveli.hexagony.features.freecam.FreeCam;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

import static hauveli.hexagony.features.freecam.FreeCam.ClientSideData.playerEntityInputsDisabled;

@Mixin(Minecraft.class)
public abstract class NoInputMinecraftMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
    private void hexagony$disableInputs(CallbackInfo ci) {
        if (playerEntityInputsDisabled) {
            /*
            Minecraft mc = Minecraft.getInstance();
            if (allowedKeys == null) {
                allowedKeys = List.of(
                        mc.options.keyChat,
                        mc.options.keyFullscreen,
                        mc.options.keySocialInteractions,
                        mc.options.keyScreenshot,
                        mc.options.keySmoothCamera,
                        mc.options.keyPlayerList
                );
            }
            /*
            if (disallowedKeys == null) {
                disallowedKeys = List.of(
                        mc.options.keyUse,
                        mc.options.keyAttack,
                        mc.options.keyJump,
                        mc.options.keyUp,
                        mc.options.keyDown,
                        mc.options.keyLeft,
                        mc.options.keyRight
                );
            }
             */
            /*
            Arrays.stream(mc.options.keyHotbarSlots).forEach(
                    keyMapping -> {
                        if (keyMapping.isDown()) {
                            keyMapping.consumeClick();
                        }
                    }
            );
            Arrays.stream(mc.options.keyMappings).forEach(
                    keyMapping -> {
                        FreeCam.ClientSideData.keyToMovement(keyMapping);
                        if (!allowedKeys.contains(keyMapping)) {
                            keyMapping.consumeClick();
                            keyMapping.setDown(false);
                        }
                    }
            );
             */
        }

    }

    @Unique
    private static List<KeyMapping> disallowedKeys = null;

    @Unique
    private static List<KeyMapping> allowedKeys = null;
}