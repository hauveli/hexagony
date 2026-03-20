package hauveli.hexagony.mixin.mindanchor;

// import hauveli.hexagony.common.lib.HexagonyItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class MindAnchorIndestructibleAtItemEntityHurtMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void makeIndestructible(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity entity = (ItemEntity)(Object)this;
        ItemStack stack = entity.getItem();

        // TODO: uncomment after making the item
        /*
        // Example: Only make Netherite items indestructible
        if (stack.is(HexagonyItems.MIND_ANCHOR)) {
            // Cancel the damage so it never takes damage
            cir.setReturnValue(false);
        }
        */

        // OR: Make ALL items indestructible
        // cir.setReturnValue(false);
    }
}