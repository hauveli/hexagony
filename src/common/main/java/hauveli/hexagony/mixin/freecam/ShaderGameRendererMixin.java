package hauveli.hexagony.mixin.freecam;


import hauveli.hexagony.Hexagony;
import hauveli.hexagony.features.freecam.FreeCameraEntity;
import hauveli.hexagony.features.freecam.ShaderRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// https://github.com/miyucomics/hexical/blob/8fb0ef85d8b3918d5fea6d62db90571ea35b7cb2/src/client/java/miyucomics/hexical/mixin/GameRendererMixin.java
@Mixin(GameRenderer.class)
public class ShaderGameRendererMixin {
    @Inject(method = "render", at = @At("RETURN"))
    public void renderShader(DeltaTracker deltaTracker, boolean tickSomehowIDK, CallbackInfo ci) {
        // idk which deltaTicks to use.... deltaTracker has several options...
        if (FreeCameraEntity.Companion.getActive()) {
            ShaderRenderer.render(deltaTracker.getGameTimeDeltaTicks());
        }
    }
}