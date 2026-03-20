package hauveli.hexagony.mixin.mindanchor;

import at.petrak.hexcasting.api.casting.iota.Iota;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Iota.class)
public interface InterfaceIotaGetPayloadMixin {
    @Accessor("payload")
    Object hex$getPayload();
}