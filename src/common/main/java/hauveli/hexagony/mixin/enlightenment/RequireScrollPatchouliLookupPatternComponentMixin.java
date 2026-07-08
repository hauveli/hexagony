package hauveli.hexagony.mixin.enlightenment;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.interop.patchouli.LookupPatternComponent;
import hauveli.hexagony.config.HexagonyCommonConfig;
import hauveli.hexagony.config.HexagonyConfigs;
import hauveli.hexagony.features.enlightenment.ScrungledPatternSending;
import hauveli.hexagony.registry.HexagonyAdvancements;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = LookupPatternComponent.class)
public class RequireScrollPatchouliLookupPatternComponentMixin {

    @Shadow
    protected ResourceLocation opName;

    @Inject(
            method = "showStrokeOrder",
            at = @At("HEAD"),
            cancellable = true
    )
    private void showTheStrokes(
            CallbackInfoReturnable<Boolean> cir
    ) {
        HexagonyCommonConfig conf = HexagonyConfigs.INSTANCE.getCOMMON_CONFIG();
        if (!conf.getRequireScrollForAllGatedSpells().get()) return;
        if (!HexagonyAdvancements.hasHeldScroll(opName.toString())) return;
        cir.setReturnValue(true);
        cir.cancel();
    }

    @Unique
    private static final ResourceLocation hexagony$bookResLoc = HexAPI.modLoc("patchouli_book");

    @Inject(
            method = "getPatterns",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getTheWorldSpecificStrokes(
            CallbackInfoReturnable<List<HexPattern>> cir
    ) {
        ScrungledPatternSending.storedPatterns.put((LookupPatternComponent) (Object) this, opName.toString());
    }
}