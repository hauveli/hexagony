package hauveli.hexagony.mixin.enlightenment;

import hauveli.hexagony.registry.HexagonyCriterions;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// note to self possibly: I think (from testing) this is called only once per inventory update? which is quite good! even if it runs n checks where n is the number of gated spells...
@Mixin(value = InventoryChangeTrigger.class)
public class PiggyBackInventoryChangeTriggerMixin {

    @Inject(
            method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void execute(ServerPlayer serverPlayer, Inventory par2, ItemStack itemStack, CallbackInfo ci) {
        HexagonyCriterions.INSTANCE.onInventoryChange(serverPlayer, itemStack);
    }
}