package hauveli.hexagony.mixin.handholding;

import hauveli.hexagony.features.fake_player.FakeServerPlayer;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// what the fuck? don't fuck with shit like this, put that shit in some api oh my god
@Mixin(NeoForgeEventHandler.class)
class OnDpSyncNeoForgeEventHandlerMixin {
    @Inject(method = "onDpSync", at = @At(value = "HEAD"), cancellable = true)
    private void unfuck(OnDatapackSyncEvent odpse, CallbackInfo ci) {
        if (odpse.getPlayer() instanceof FakeServerPlayer) {
            ci.cancel();
        }
    }
}