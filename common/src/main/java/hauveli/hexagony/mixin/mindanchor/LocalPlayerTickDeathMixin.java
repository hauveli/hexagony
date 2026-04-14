package hauveli.hexagony.mixin.mindanchor;

import hauveli.hexagony.common.control.PlayerControlData;
import hauveli.hexagony.mind_anchor.MindAnchorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerTickDeathMixin {
    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void grafted$disableDeathScreen(CallbackInfo ci) {
        Float media = MindAnchorManager.getLocalMedia();
        if (media != null && media > 0f) {
            ci.cancel();
        }
    }
}
