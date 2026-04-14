package hauveli.hexagony.mixin.mindanchor;

import hauveli.hexagony.common.mind_anchor.MindAnchorManager;
import net.minecraft.client.gui.screens.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(DeathScreen.class)
public abstract class NoDeathScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void grafted$disableDeathScreen(CallbackInfo ci) {
        Float media = MindAnchorManager.getLocalMedia();
        if (media != null && media > 0f) {
            ci.cancel();
        }
    }
}
