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
        LocalPlayer player = Minecraft.getInstance().player;
        player.respawn();
        ci.cancel();
        player.sendSystemMessage(Component.nullToEmpty("hrmmmm"));
        if (player != null
                && PlayerControlData.Companion.getMyEntry().isDetached()) {
            player.sendSystemMessage(Component.nullToEmpty("erm erm erm"));
            Level level = player.level();
            MinecraftServer server = level.getServer();
            assert server != null;
            player.sendSystemMessage(Component.nullToEmpty("aaaaaaaaaaa"));
            ServerPlayer sp = server.getPlayerList().getPlayer(player.getUUID());
            assert sp != null;
            player.sendSystemMessage(Component.nullToEmpty("euuuuuuuuuuu"));
            Long media = MindAnchorManager.INSTANCE.getMedia(sp);
            if (media != null && media > 0L) {
                player.sendSystemMessage(Component.nullToEmpty("YPIIIII"));
                ci.cancel();
            }
        }
    }
}
