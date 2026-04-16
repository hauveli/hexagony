package hauveli.hexagony.mixin.mindanchor;

import hauveli.hexagony.common.mind_anchor.MindAnchorManager;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerTickDeathMixin {
    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void grafted$disableDeathScreen(CallbackInfo ci) {
        Float media = MindAnchorManager.getLocalMedia();
        // Server should control and inform the client of the amount of media left
        if (media == null) {
            // request data?

        } else if (media > 0f) {
            ci.cancel();
        }
    }
}
