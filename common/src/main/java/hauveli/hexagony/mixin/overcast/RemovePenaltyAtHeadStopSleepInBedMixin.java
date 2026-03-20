package hauveli.hexagony.mixin.overcast;

import hauveli.hexagony.config.HexagonyServerConfig;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
// import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;

/*
 * This mixin /tries/ to detect when a player has rested and then removes one penalty counter
 * from the player(s) that have slept. I hope this works in multiplayer...
 * Amount recovered per night is stored in the config in the integer recoveryPerRest
 */
@Mixin(ServerPlayer.class)
public abstract class RemovePenaltyAtHeadStopSleepInBedMixin {

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void stopSleeping(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        // Todo: make level() calls safer?
        if (player.level().isClientSide) return;

        // I dont understand why I need Stats.CUSTOM.get(Stat)
        // int restStat = player.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        // player successfully slept, is there a better detection method?
        // can this be abused if the player gets in and out of bed somehow?
        // restStat == 0 &&
        if (!hexagony$playerSleptEnough(player)) return;
        hexagony$removeHexcastingOvercastPenalty(player);
    }

    /*
     * tries to check if enough players are asleep to pass the night, and if the player
     * waking up got enough rest, and the night is about to pass.
     */
    @Unique
    private boolean hexagony$playerSleptEnough(ServerPlayer player) {
        if (player.isSleepingLongEnough()) {
            int percentage_required = player.level().getGameRules().getInt(
                    net.minecraft.world.level.GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
            int totalPlayers = player.level().players().size();
            int sleepingPlayers = (int) player.level().players().stream().filter(LivingEntity::isSleeping).count();
            int percentage_sleeping = (int) (double) (sleepingPlayers / totalPlayers) * 100;
            return percentage_sleeping >= percentage_required;
        }
        return false;

    }

    /**
     * removes recoveryPerRest max health modifier(s) with the name "hexcasting_overcast_penalty"
     * note: these are modifiers, NOT raw health
     * Each counter may be more or less than 1 health, ex 0.5 or 3.14 health.
     * recoveryPerRest will remove one such modifier per rest
     */
    @Unique
    private void hexagony$removeHexcastingOvercastPenalty(ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return; // can maxHealth be null?

        Collection<AttributeModifier> modifiers = maxHealth.getModifiers();

        int recoveryCounter = 0;
        for (AttributeModifier modifier : modifiers.stream().toList()) {
            if (HexagonyServerConfig.getConfig().getOvercastAttributeName().equals(modifier.getName())) {
                // checking against name, not uuid
                // It might be better to construct a list of uuids and store those
                // but frankly, chance of hexcasting_overcast_penalty
                // being used for anything but this is so exceedingly low I dont care
                // to implement the cleaner solution, this simple one is ok for now
                maxHealth.removeModifier(modifier);
                recoveryCounter++;
                if (recoveryCounter >= HexagonyServerConfig.getConfig().getRecoveryPerRest()) {
                    return; // return on the first instance, penalty removed once per call (in this case rest)
                }
            }
        }
    }
}