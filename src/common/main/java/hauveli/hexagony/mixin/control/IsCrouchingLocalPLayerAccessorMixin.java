package hauveli.hexagony.mixin.control;


import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface IsCrouchingLocalPLayerAccessorMixin {
    @Accessor("crouching")
    boolean getCrouching();

    @Mutable
    @Accessor("crouching")
    void setCrouching(boolean value);

    @Accessor("wasShiftKeyDown")
    boolean getWasShiftKeyDown();

    @Mutable
    @Accessor("wasShiftKeyDown")
    void setWasShiftKeyDown(boolean value);
}