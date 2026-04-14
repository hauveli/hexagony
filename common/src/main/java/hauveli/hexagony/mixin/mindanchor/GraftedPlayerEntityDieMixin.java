package hauveli.hexagony.mixin.mindanchor;

import hauveli.hexagony.common.control.PlayerControlData;
import hauveli.hexagony.common.control.PlayerControlEntry;
import hauveli.hexagony.common.misc.AdvancementProvider;
import hauveli.hexagony.mind_anchor.MindAnchorManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class GraftedPlayerEntityDieMixin {

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void grafted$preventDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer)(Object)this;
        self.sendSystemMessage(Component.nullToEmpty("Test: ${self}"));
        if (!AdvancementProvider.isTrepanned(self)) {
            return;
        }

        Vec3 mindAnchorPos = MindAnchorManager.INSTANCE.getPosition(self);
        if (mindAnchorPos == null) return; // just die instead, maybe also play a sound?
        self.sendSystemMessage(Component.nullToEmpty(mindAnchorPos.toString()));
        ci.cancel();
        // somehow letting the player know why they didn't detach would be good...
        self.teleportTo(mindAnchorPos.x, mindAnchorPos.y, mindAnchorPos.z);

        PlayerControlEntry e = PlayerControlData.Companion.get(self.server).getOrCreate(self.getUUID());
        if (!e.isDetached()) {
            e.detach(self);
        }
        if (self.getHealth() < 0f) {
            self.setHealth(1f);
        }

        self.sendSystemMessage(Component.nullToEmpty(e.toString()));

        self.clearFire();
        self.fallDistance = 0;

        // has to be intangible, need to undo this after...
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
    }
}