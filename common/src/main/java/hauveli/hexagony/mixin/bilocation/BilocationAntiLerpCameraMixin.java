package hauveli.hexagony.mixin.bilocation;

import hauveli.hexagony.common.bilocation.CameraExtension;
import hauveli.hexagony.common.bilocation.FreeCameraEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class BilocationAntiLerpCameraMixin  implements CameraExtension {

    @Shadow
    protected abstract void setPosition(Vec3 pos);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "setup", at = @At("TAIL"))
    private void cameraUnLerper(
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean thirdPersonReverse,
            float partialTick,
            CallbackInfo ci) {
        if (!FreeCameraEntity.Companion.getActive()) {
            return;
        }
        Entity fc = FreeCameraEntity.Companion.getFreeCam();
        assert fc != null;
        setPosition(fc.position());
        setRotation(fc.getYRot(), fc.getXRot());
    }

    @Unique
    public void hexagony$bilocationSetCameraPosition(@NotNull Vec3 position) {
        setPosition(position);
    }

    @Unique
    public void hexagony$bilocationSetCameraRotation(float rotY, float rotX) {
        setRotation(rotY, rotX);
    }
}