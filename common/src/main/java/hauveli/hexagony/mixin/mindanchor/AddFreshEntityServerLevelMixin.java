package hauveli.hexagony.mixin.mindanchor;

import hauveli.hexagony.mind_anchor.MindAnchorManager;
import hauveli.hexagony.registry.HexagonyItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

import static hauveli.hexagony.common.blocks.anchors.MindAnchor.TAG_STORED_PLAYER;

@Mixin(ServerLevel.class)
public abstract class AddFreshEntityServerLevelMixin {

    @Inject(
            method = "addFreshEntity",
            at = @At("HEAD")
    )
    private void onAddFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // God forgive me for mixin in to a ServerLevel method like this, I can only pray the check makes it fast
        // Thankfully, I think most of these should fail on the item type comparison...
        if (entity instanceof ItemEntity itemEntity && itemEntity.getItem().getItem() == HexagonyItems.MIND_ANCHOR_FULL.getValue()) {
            if (itemEntity.getServer() != null) {
                //UUID mindUUID = itemEntity.getItem().getTag().getCompound("BlockEntityTag").getUUID(TAG_STORED_PLAYER);
                itemEntity.setNoGravity(true);
                itemEntity.setInvulnerable(true);
                itemEntity.setUnlimitedLifetime();
                MindAnchorManager.INSTANCE.trackItemEntity(
                        itemEntity.getServer(),
                        Objects.requireNonNull(itemEntity.getItem().getTag()).getCompound("BlockEntityTag").getUUID(TAG_STORED_PLAYER),
                        itemEntity
                );
            }
        }
    }
}