package hauveli.hexagony.mixin.mindanchor;

import hauveli.hexagony.mind_anchor.MindAnchorManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

import static hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor.TAG_STORED_PLAYER;

@Mixin(Player.class)
public abstract class MindAnchorPlayerAddItemMixin {

    @Inject(method = "addItem", at = @At("HEAD"))
    private void onAddItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player)(Object)this;
        if (self.level().isClientSide) return;
        if (!(self instanceof ServerPlayer serverPlayer)) return;

        CompoundTag tag = stack.getTag();
        if (!serverPlayer.level().isClientSide) {
            if (tag == null || !tag.contains("BlockEntityTag")) return;
            CompoundTag deeperTag = tag.getCompound("BlockEntityTag");
            if (deeperTag == null || !deeperTag.contains(TAG_STORED_PLAYER)) return;
            UUID mindUUID = deeperTag.getUUID(TAG_STORED_PLAYER);
            Level level = serverPlayer.level();
            if (level == null)
                return;
            MinecraftServer server = level.getServer();
            if (server == null)
                return;

            MindAnchorManager.INSTANCE.trackItemStack(
                    server,
                    mindUUID,
                    serverPlayer
            );
        }
    }
}