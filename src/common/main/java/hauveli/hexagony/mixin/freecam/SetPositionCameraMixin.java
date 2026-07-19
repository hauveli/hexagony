package hauveli.hexagony.mixin.freecam;

import hauveli.hexagony.features.freecam.FreeCameraEntity;
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

import static hauveli.hexagony.client.HexagonyClient.MINECRAFT;


@Mixin(Camera.class)
abstract class SetPositionCameraMixin {

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
        if (!FreeCameraEntity.Companion.getActive()) return;
        assert MINECRAFT != null;
        if (MINECRAFT.isPaused()) return;
        FreeCameraEntity fce = FreeCameraEntity.Companion.getFreeCam();
        assert fce != null;
        setPosition(fce.position());
        setRotation(fce.getYRot(), fce.getXRot());
        FreeCameraEntity.Companion.updateFreeCam(partialTick);
    }
}