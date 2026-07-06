package hauveli.hexagony.mixin.enlightenment;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidPattern;
import at.petrak.hexcasting.api.casting.mishaps.MishapUnenlightened;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import com.google.common.collect.Iterables;
import hauveli.hexagony.config.HexagonyCommonConfig;
import hauveli.hexagony.config.HexagonyConfigs;
import hauveli.hexagony.registry.HexagonyCriterions;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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