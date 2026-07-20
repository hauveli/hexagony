package hauveli.hexagony.mixin.handholding;

import hauveli.hexagony.features.fake_player.DummyConnection;
import hauveli.hexagony.features.fake_player.FakeServerPlayer;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Why is neoforge fucking me
// what the fuck? don't fuck with shit like this, put that shit in some api oh my god
@Mixin(ICommonPacketListener.class)
interface HasChannelICommonPacketListenerMixin {
    @Inject(method = "hasChannel*", at = @At(value = "HEAD"), cancellable = true)
    private void unfuck(CallbackInfo ci) {
        ICommonPacketListener packetListener = (ICommonPacketListener) (Object) this;
        if (packetListener.getConnection() instanceof DummyConnection) {
            ci.cancel();
        }
    }
}