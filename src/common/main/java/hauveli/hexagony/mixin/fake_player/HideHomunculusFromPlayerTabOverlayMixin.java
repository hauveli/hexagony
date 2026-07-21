package hauveli.hexagony.mixin.fake_player;

import hauveli.hexagony.features.fake_player.FakeServerPlayerUtils;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerTabOverlay.class)
public class HideHomunculusFromPlayerTabOverlayMixin {
    // For future consideration: make the tablist behavior consistent in singleplayer? this is such a non-issue I dont feel like caring right now

    @Inject(method = "getPlayerInfos", at = @At("RETURN"), cancellable = true)
    private void hideHomunculusFromTablist(CallbackInfoReturnable<List<PlayerInfo>> cir) {
        List<PlayerInfo> playerList = cir.getReturnValue();
        List<PlayerInfo> playerListReal = new ArrayList<>();
        for (PlayerInfo entry : playerList) {
            if (!hexagony$isHomunculus(entry)) {
                playerListReal.add(entry);
            }
        }

        cir.setReturnValue(playerListReal);
    }

    @Unique
    private boolean hexagony$isHomunculus(PlayerInfo entry) {
        if (entry.getTeam() == null) return false;
        return entry.getTeam().getName().equals(FakeServerPlayerUtils.TEAM_NAME);
    }
}