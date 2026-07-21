package hauveli.hexagony.mixin.fake_player;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class HideHomunculusFromLoginMessageClientPacketListenerMixin {

    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
    private void onSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        Component message = packet.content();

        if (message.getContents() instanceof TranslatableContents contents) {
            String key = contents.getKey();

            switch(key) {
                case "multiplayer.player.joined": {
                    if (message.getString().startsWith("Homunculus ")) { // sorry to the one player named this
                        ci.cancel();
                    }
                }
                case "multiplayer.player.left": {
                    if (message.getString().startsWith("Homunculus ")) { // sorry to the one player named this
                        ci.cancel();
                    }
                }
                default: {
                    if (key.startsWith("death.")) {
                        if (message.getString().startsWith("Homunculus ")) { // sorry to the one player named this
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }
}