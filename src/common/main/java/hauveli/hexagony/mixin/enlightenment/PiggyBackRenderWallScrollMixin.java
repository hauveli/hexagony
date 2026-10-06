package hauveli.hexagony.mixin.enlightenment;

import at.petrak.hexcasting.client.entity.WallScrollRenderer;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.interop.patchouli.LookupPatternComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import hauveli.hexagony.Hexagony;
import hauveli.hexagony.features.enlightenment.ScrungledPatternSending;
import hauveli.hexagony.registry.HexagonyAdvancements;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WallScrollRenderer.class)
public abstract class PiggyBackRenderWallScrollMixin {
    @Inject(
            method = "render(Lat/petrak/hexcasting/common/entities/EntityWallScroll;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void replacePatterns(
            EntityWallScroll wallScroll,
            float yaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource bufferSource,
            int packedLight, CallbackInfo ci
    ) {
        if (!wallScroll.isAncient || !wallScroll.getShowsStrokeOrder())
            return;
        ResourceLocation resLoc = wallScroll.scroll.get(HexDataComponents.ACTION.get()).location();
        if(!HexagonyAdvancements.hasPatternKnowledge(resLoc.toString())) {
            // HexagonyCommonConfig conf = HexagonyConfigs.INSTANCE.getCOMMON_CONFIG();
            // if (!conf.getRequireScrollForAllGatedSpells().get()) return; // what was I thinking? if I comment this out, the feature will work just fine anyway.

            // hmm if I just check the rendered position on the client, then ask the server if the UUID of the wall scroll exists
            // and matches the given pattern, then I can check the relative distance and if they are within render distance I can just approve it
            // this leaves the only "cheat" that can be done as: client scans for loaded entities -> parses ancient scroll -> sends the packet -> server acknowledges if it is within render distance
            // and I really don't know who would go to those lengths to cheat for something like that... but it's something to consider for the future if it's a real issue, since I mean,
            // at that point they ALREADY have the information loaded on the client and I can't fucking doa nything about that so I really don't feel like the burden falls on me at the point the client
            // has already received the information from the server by base hex.... this rant brought to you by past me to whoever might try to nitpick this implementation

            // request the thing to be rendered, try asking the server once
            String uuidString = wallScroll.getStringUUID();
            ScrungledPatternSending.fromClientThatJustSawWallScroll(resLoc.toString(), uuidString);
        }
    }
}