package hauveli.hexagony.mixin.dissociation;

import hauveli.hexagony.features.freecam.CameraExtension;
import hauveli.hexagony.features.freecam.FreeCam;
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

import static hauveli.hexagony.features.freecam.FreeCam.ClientSideData.playerEntityInputsDisabled;


@Mixin(Camera.class)
abstract class SetPositionCameraMixin implements CameraExtension {

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
        if (!playerEntityInputsDisabled) {
            return;
        }

        /*
        setPosition(FreeCam.ClientSideData.position());
        setRotation((float) FreeCam.ClientSideData.getYRot(), (float) FreeCam.ClientSideData.getXRot());

         */
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