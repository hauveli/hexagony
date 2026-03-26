package hauveli.hexagony.mixin.astralproject;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
class AstralProjectTickCameraMixin {
    @Shadow
    protected void setRotation(float yaw, float pitch) {

    }

    @Shadow
    protected void setPosition(Vec3 pos) {

    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void doFreeCamStuff(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        boolean freecamDisabled = false;
        if (freecamDisabled || client.player == null) {
            return;
        }

        // TODO: lerp?
        // what interpolation is used?
        // could use funky lerp if I wanted to....
        this.setRotation(0,0);
        this.setPosition(new Vec3(0,10,0));
    }
}