package hauveli.hexagony.mixin.mindanchor;

import hauveli.hexagony.mind_anchor.MindAnchorManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class MindAnchorPickupItemEntityPlayerTouchMixin {

    @Inject(
            method = "playerTouch",
            at = @At("HEAD")
    )
    private void onPlayerTouch(Player player, CallbackInfo ci) {


        // I would really REALLY like a better method than this because I do not like the idea of
        // injecting this on touching every single entity.............
        // Fortunately, players dropping and picking up items is not so common that it should be an issue, I think...
        if (!player.level().isClientSide) {
            ItemEntity self = (ItemEntity)(Object)this;
            ItemStack stack = self.getItem();
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.hasUUID("MindUUID"))
                return;
            Level level = player.level();
            if (level == null)
                return;
            MinecraftServer server = level.getServer();
            if (server == null)
                return;

            UUID mindUUID = tag.getUUID("MindUUID");

            MindAnchorManager.INSTANCE.trackItemStack(
                    server,
                    mindUUID,
                    player
            );
        }
    }
}