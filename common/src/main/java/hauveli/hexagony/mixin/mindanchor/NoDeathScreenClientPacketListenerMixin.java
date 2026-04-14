package hauveli.hexagony.mixin.mindanchor;

import hauveli.hexagony.common.control.PlayerActionAPI;
import hauveli.hexagony.common.control.PlayerControlData;
import hauveli.hexagony.mind_anchor.MindAnchorData;
import hauveli.hexagony.mind_anchor.MindAnchorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class NoDeathScreenClientPacketListenerMixin {

    @Inject(method = "handlePlayerCombatKill", at = @At("HEAD"), cancellable = true)
    private void grafted$disableDeathScreen(ClientboundPlayerCombatKillPacket packet,
                                            CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null
                && PlayerControlData.Companion.getMyEntry().isDetached()) {
            Level level = player.level();
            MinecraftServer server = level.getServer();
            assert server != null;
            ServerPlayer sp = server.getPlayerList().getPlayer(player.getUUID());
            assert sp != null;
            Long media = MindAnchorManager.INSTANCE.getMedia(sp);
            if (media != null && media > 0L) {
                ci.cancel();
            }
        }
    }
}
