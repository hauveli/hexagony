package hauveli.hexagony.mixin.handholding;

import hauveli.hexagony.features.fake_player.DummyConnection;
import hauveli.hexagony.features.fake_player.FakeServerPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// classifying neoforge as a second-class citizen for doing this
// what the fuck? don't fuck with shit like this, put that shit in some api oh my god
@Mixin(NetworkRegistry.class)
class HasChannelNetworkRegistryMixin {
    @Inject(method = "hasChannel(Lnet/minecraft/network/Connection;Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/resources/ResourceLocation;)Z", at = @At(value = "HEAD"), cancellable = true)
    private static void unfuckConnection(Connection connection, ConnectionProtocol protocol, ResourceLocation payloadId, CallbackInfoReturnable<Boolean> cir) {
        if (connection instanceof DummyConnection) {
            cir.cancel();
        }
    }

    @Inject(method = "hasChannel(Lnet/neoforged/neoforge/common/extensions/ICommonPacketListener;Lnet/minecraft/resources/ResourceLocation;)Z", at = @At(value = "HEAD"), cancellable = true)
    private static void unfuckPacketListener(ICommonPacketListener listener, ResourceLocation payloadId, CallbackInfoReturnable<Boolean> cir) {
        if (listener.getConnection() instanceof DummyConnection) {
            cir.cancel();
        }
    }

    @Inject(method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;)V", at = @At(value = "HEAD"), cancellable = true)
    private static void unfuckCheckPacketToClient(Packet<?> packet, ClientCommonPacketListener listener, CallbackInfo ci) {
        if (listener.getConnection() instanceof DummyConnection) {
            ci.cancel();
        }
    }

    @Inject(method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ServerCommonPacketListener;)V", at = @At(value = "HEAD"), cancellable = true)
    private static void unfuckCheckPacketToServer(Packet<?> packet, ServerCommonPacketListener listener, CallbackInfo ci) {
        if (listener.getConnection() instanceof DummyConnection) {
            ci.cancel();
        }
    }
}