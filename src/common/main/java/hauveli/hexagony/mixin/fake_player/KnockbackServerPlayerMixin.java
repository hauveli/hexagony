package hauveli.hexagony.mixin.fake_player;

import hauveli.hexagony.features.fake_player.FakeServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// https://github.com/gnembon/fabric-carpet/blob/1.20.2/src/main/java/carpet/mixins/Player_fakePlayersMixin.java
@Mixin(Player.class)
public class KnockbackServerPlayerMixin {

    // to make sure player attacks are able to knockback fake players
    @Redirect(
            method = "attack",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z",
                    ordinal = 0,
                    opcode = Opcodes.GETFIELD)
    )

    private boolean makePlayerGetKnockedBack(Entity target) {
        return target.hurtMarked && !(target instanceof FakeServerPlayer);
    }
}