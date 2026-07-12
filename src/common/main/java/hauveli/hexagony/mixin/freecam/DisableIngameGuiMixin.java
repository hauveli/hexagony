package hauveli.hexagony.mixin.freecam;

import hauveli.hexagony.features.freecam.FreeCameraEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class DisableIngameGuiMixin {

    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void hideHotbar(GuiGraphics par1, DeltaTracker par2, CallbackInfo ci) {
        if (FreeCameraEntity.Companion.getActive()) {
            ci.cancel(); // prevents vanilla hotbar from drawing
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void hideXpBar(GuiGraphics guiGraphics, int x, CallbackInfo ci) {
        if (FreeCameraEntity.Companion.getActive()) {
            ci.cancel(); // prevents XP bar
        }
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void hideHearts(GuiGraphics guiGraphics, Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci) {
        if (FreeCameraEntity.Companion.getActive()) {
            ci.cancel(); // prevents hearts/armor
        }
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void hideFood(GuiGraphics par1, Player par2, int par3, int par4, CallbackInfo ci) {
        if (FreeCameraEntity.Companion.getActive()) {
            ci.cancel(); // prevents hearts/armor
        }
    }

    @Inject(method = "renderJumpMeter", at = @At("HEAD"), cancellable = true)
    private void hideFood(PlayerRideableJumping rideable, GuiGraphics guiGraphics, int x, CallbackInfo ci) {
        if (FreeCameraEntity.Companion.getActive()) {
            ci.cancel(); // prevents hearts/armor
        }
    }
    /*
    */

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void hidePlayerHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (FreeCameraEntity.Companion.getActive()) {
            ci.cancel(); // prevents hearts/armor (what the fuck why isn't it doing this anymore?
        }
    }
}