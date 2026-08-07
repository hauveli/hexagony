package hauveli.hexagony.mixin.mind_anchor;

import at.petrak.hexcasting.api.misc.MediaConstants;
import hauveli.hexagony.features.mind_anchor.MindAnchorManager;
import hauveli.hexagony.registry.HexagonyAdvancements;
import hauveli.hexagony.registry.HexagonyMobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class GraftedPlayerEntityDieMixin {

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void grafted$preventDeath(DamageSource source, CallbackInfo ci) {
        if (true) {
            return;
        }
        ServerPlayer self = (ServerPlayer)(Object)this;
        self.sendSystemMessage(Component.nullToEmpty(self.toString()));
        if (!HexagonyAdvancements.isTrepanned(self)
                || (self.hasEffect(HexagonyMobEffects.FREECAM.holder())
                && self.getEffect(HexagonyMobEffects.FREECAM.holder()).getAmplifier() == 0)) {
            return;
        }
        self.sendSystemMessage(Component.nullToEmpty("past trepanned"));

        Vec3 mindAnchorPos = MindAnchorManager.INSTANCE.getPosition(self);
        Long mindAnchorMedia = MindAnchorManager.INSTANCE.getMedia(self);
        // self.sendSystemMessage(Component.nullToEmpty(mindAnchorPos.toString()));

        // somehow letting the player know why they didn't detach would be good...


        long deathCost = MediaConstants.CRYSTAL_UNIT * 1_000;
        if (deathCost > mindAnchorMedia) {
            MindAnchorManager.fuckingExplodeAndDie(self);
            return; // die
        }
        Long punishmentCost = mindAnchorMedia / 2 + deathCost;

        if (!self.hasEffect(HexagonyMobEffects.FREECAM.holder())) {
            self.sendSystemMessage(Component.nullToEmpty("Player was NOT detached already!"));

            if (mindAnchorMedia > punishmentCost) {
                self.sendSystemMessage(Component.nullToEmpty("Player had enough mana!!!"));

                MindAnchorManager.INSTANCE.subtractMedia(
                        self,
                        punishmentCost
                );
                self.teleportTo(mindAnchorPos.x, mindAnchorPos.y, mindAnchorPos.z);
                self.addEffect(
                        HexagonyMobEffects.INSTANCE.getInstance(
                                HexagonyMobEffects.FREECAM,
                                Integer.MAX_VALUE,
                                1
                        )
                );
                // Might need to let the player get further into the die method?
                if (self.getHealth() < 0f) {
                    self.setHealth(1f);
                }
                // has to be intangible, need to undo this after...
                self.clearFire();
                self.fallDistance = 0;
                self.noPhysics = true;
                self.setInvisible(true);
                Level level = self.level();
                if (level == null) return;
                level.playSound(
                        self,
                        self,
                        SoundEvents.GLASS_BREAK,
                        SoundSource.PLAYERS,
                        1.0f,
                        1.0f
                );
            } else {
                self.sendSystemMessage(Component.nullToEmpty("NO MEDIA, BROKE!!!!!!"));
                return; // player's fault
            }
        } else {
            self.sendSystemMessage(Component.nullToEmpty("Player being charge for the death"));
            MindAnchorManager.INSTANCE.subtractMedia(
                    self,
                    deathCost
            );
        }
        ci.cancel();

        self.sendSystemMessage(Component.nullToEmpty(self.toString()));
    }
}