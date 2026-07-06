package hauveli.hexagony.mixin.overcast;

import hauveli.hexagony.config.HexagonyConfigs;
import hauveli.hexagony.features.healthcasting.OvercastUtils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;

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
        // make level() calls safer?
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
            Level level = player.level();
            int percentage_required = level.getGameRules().getInt(
                    GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
            int totalPlayers = level.players().size();
            int sleepingPlayers = (int) level.players().stream().filter(LivingEntity::isSleeping).count();
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
        OvercastUtils.addModifierValue(player, HexagonyConfigs.INSTANCE.getCOMMON_CONFIG().getRecoveryPerRest().get());
        if (OvercastUtils.getModifierValue(player) > 0) {
            OvercastUtils.setModifierValue(player, 0);
        }
    }
}