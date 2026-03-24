package hauveli.hexagony.mixin.mindanchor;

import hauveli.hexagony.mind_anchor.MindAnchorManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import static hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor.TAG_STORED_PLAYER;

@Mixin(Player.class)
public abstract class MindAnchorPlayerDropMixin {

    @Inject(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("RETURN")
    )
    private void onDrop(
            ItemStack stack,
            boolean throwRandomly,
            boolean retainOwnership,
            CallbackInfoReturnable<ItemEntity> cir
    ) {
        Player self = (Player)(Object)this;

        if (self.level().isClientSide) return;
        if (!(self instanceof ServerPlayer serverPlayer)) return;

        ItemEntity itemEntity = cir.getReturnValue();
        if (itemEntity == null) return;

        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains("BlockEntityTag")) return;
        CompoundTag deeperTag = tag.getCompound("BlockEntityTag");

        if (deeperTag == null || !deeperTag.contains(TAG_STORED_PLAYER)) return;
        UUID mindUUID = deeperTag.getUUID(TAG_STORED_PLAYER);


        serverPlayer.sendSystemMessage(Component.nullToEmpty(tag.toString()));
        MinecraftServer server = serverPlayer.server;

        MindAnchorManager.INSTANCE.trackItemEntity(
                server,
                mindUUID,
                itemEntity
        );
    }
}