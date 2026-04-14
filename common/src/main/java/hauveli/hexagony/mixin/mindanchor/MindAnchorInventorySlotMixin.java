package hauveli.hexagony.mixin.mindanchor;

import hauveli.hexagony.common.mind_anchor.MindAnchorManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static hauveli.hexagony.common.blocks.anchors.MindAnchor.TAG_STORED_PLAYER;

// Oh god I don't want to mixin this but I could not figure out another way to track it...
// I wish the inventory system was done in a different way but I don't know how I would want it done

@Mixin(Slot.class)
public abstract class MindAnchorInventorySlotMixin {

    @Final
    @Shadow
    public Container container;
    @Shadow public int index;

    @Inject(method = "set", at = @At("TAIL"))
    private void onSet(ItemStack stack, CallbackInfo ci) {
        // I really, really hope that doing this will make the performance impact negligible, but you never know...
        if (!stack.hasTag() || stack.getTag() == null) return;
        CompoundTag tag = stack.getTag().getCompound("BlockEntityTag");
        if (!tag.hasUUID(TAG_STORED_PLAYER)) return;
        UUID mindUUID = tag.getUUID(TAG_STORED_PLAYER);
        Container container = this.container;

        if (container instanceof Inventory playerInv) {
            Player player = playerInv.player ;
            Level level = player.level();
            MinecraftServer server = level.getServer();
            if (server == null) return;
            MindAnchorManager.INSTANCE.trackItemStack(
                    server,
                    mindUUID,
                    player,
                    stack);
            System.out.println("Player inventory change: " + player.getName().getString());
        }

        if (container instanceof BlockEntity blockEntity) {
            Level level = blockEntity.getLevel();
            BlockPos pos = blockEntity.getBlockPos();
            MinecraftServer server = level.getServer();
            if (server == null) return;
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) return;
            MindAnchorManager.INSTANCE.trackBlock(
                    server,
                    mindUUID,
                    be);

            System.out.println("BlockEntity inventory change at: " + pos);
        }

        System.out.println("Simple container");
        System.out.println( container.toString() );
        if (container instanceof Entity entity) {
            Level level = entity.level();
            MinecraftServer server = level.getServer();
            if (server == null) return;
            MindAnchorManager.INSTANCE.trackItemStack(
                    server,
                    mindUUID,
                    entity,
                    stack);
            System.out.println("Simple container size: ");
        }
        System.out.println("Slot index: " + index);
    }
}