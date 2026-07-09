package hauveli.hexagony.mixin.enlightenment;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.interop.patchouli.AbstractPatternComponent;
import at.petrak.hexcasting.interop.patchouli.LookupPatternComponent;
import hauveli.hexagony.config.HexagonyCommonConfig;
import hauveli.hexagony.config.HexagonyConfigs;
import hauveli.hexagony.features.enlightenment.ScrungledPatternSending;
import hauveli.hexagony.registry.HexagonyAdvancements;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.patchouli.api.IComponentRenderContext;

import java.util.List;
@Mixin(AbstractPatternComponent.class)
public abstract class RequireScrollPatchouliAbstractPatternComponentMixin {

    @Shadow
    private List<HexPattern> patterns;

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void replacePatterns(
            GuiGraphics graphics,
            IComponentRenderContext context,
            float pticks,
            int mouseX,
            int mouseY,
            CallbackInfo ci
    ) {
        if (!HexagonyConfigs.INSTANCE.getCLIENT_CONFIG().getRevealGreatSpellsOnHeldInBook().get()) return;
        if ((Object) this instanceof LookupPatternComponent lookupPatternComponent) {
            HexagonyCommonConfig conf = HexagonyConfigs.INSTANCE.getCOMMON_CONFIG();
            if (!conf.getRequireScrollForAllGatedSpells().get()) return;
            String key = ScrungledPatternSending.storedPatterns.get(lookupPatternComponent);
            if (!HexagonyAdvancements.hasHeldScroll(key)) return;
            if (!key.equals(ScrungledPatternSending.currentKey)) {
                if (key.equals(ScrungledPatternSending.previousKeyRequest)) return;
                // request the thing to be rendered, try asking the server once
                ScrungledPatternSending.fromClient(key);
            } else {
                this.patterns = List.of(ScrungledPatternSending.currentHexPattern);
            }
        }
    }
}