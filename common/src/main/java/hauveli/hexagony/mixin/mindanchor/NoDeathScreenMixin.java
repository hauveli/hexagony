package hauveli.hexagony.mixin.mindanchor;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import hauveli.hexagony.common.control.PlayerActionAPI;
import hauveli.hexagony.common.control.PlayerControlData;
import hauveli.hexagony.mind_anchor.MindAnchorData;
import hauveli.hexagony.mind_anchor.MindAnchorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
