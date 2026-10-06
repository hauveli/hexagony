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
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WallScrollRenderer.class)
public abstract class PiggyBackRenderWallScrollMixin {

    /*
        hmm.................
        I think I need to, in order to do this right,
        get the corners of the pattern
        check if the corners are all (at least mostly) within frame

        but I'm not sure quite how to do this.....

        Is it worth the extra effort? Probably for niche cases, but I tested playing this and it's harder not to get it than not
        especially when playing normally, it is difficult to imagine a player would want to try to

        can I somehow get what the difference between two passes looks like?
        can I compare z depth and figure out occlusion that way?
        getting a perfect solution sounds like learning a good bit about how it works...
        Would require checking if at most the convex hull of the flat shape is rendered in its entieety,
        or at least if each individual dot is visible... which might be really easy actually? hmm....
        that's on my todo, at least

        Well, since there is some interest in having this in main hex I'll make an effort to continue the PR fork
     */

    @Unique
    private static boolean hexagony$playerIsFacingCorrectScrollFace(EntityWallScroll entityWallScroll) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        Vec3 scrollPos = entityWallScroll.getEyePosition(); // I think this is the right spot for the center?

        Vec3 toCamera = camPos.subtract(scrollPos).normalize();
        Vec3 toScroll = scrollPos.subtract(camPos).normalize();

        Vec3 normal = entityWallScroll.getLookAngle();

        // <0 is behind, >0 is in front
        double onCorrectSideOfScroll = normal.dot(toCamera);
        // can't see it for shit
        if (onCorrectSideOfScroll < 0.01)
            return false;

        // 0 is 90 deg, 1 is head on, note to future self: this is relative to the center of the scroll!!!!!!!
        double lookingAtTheScrollIsh = camera.getLookVector().dot(toScroll.toVector3f());

        Hexagony.LOGGER.info("facing of the thing: {}, {}", onCorrectSideOfScroll, lookingAtTheScrollIsh);

        return lookingAtTheScrollIsh > 0.6; // this felt okay-ish in game
    }

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
        if(HexagonyAdvancements.hasPatternKnowledge(resLoc.toString()))
            return;

        // It does make sense to check if the player is in line of sight able to see it though, I think...
        // todo: a more optimized way of checking this? renderer poseStack relative to camera maybe? hmmm...
        if (!hexagony$playerIsFacingCorrectScrollFace(wallScroll))
            return;
        // if (!conf.getRequireScrollForAllGatedSpells().get()) return; // what was I thinking? if I comment this out, the feature will work just fine anyway.

        /*
            from future self 1 hour later: I think there's no point in checking the position, if the client already know:
            uuid, pattern
            of the EntityWallScroll, they would already be able to cheat by just reading it, which is possible with base HexMod
            and is not really something I can circumvent. Especially because this gives the Client 0 knowledge about the pattern
            they wouldn't already have with base mod, I think this is ok without a distance check and more validation?
            I'll still do the "actuall in line os sight" on client check, but anything more than that seems a little extreme...

            hmm if I just check the rendered position on the client, then ask the server if the UUID of the wall scroll exists
            and matches the given pattern, then I can check the relative distance and if they are within render distance I can just approve it
            this leaves the only "cheat" that can be done as: client scans for loaded entities -> parses ancient scroll -> sends the packet -> server acknowledges if it is within render distance
            and I really don't know who would go to those lengths to cheat for something like that... but it's something to consider for the future if it's a real issue, since I mean,
            at that point they ALREADY have the information loaded on the client and I can't fucking doa nything about that so I really don't feel like the burden falls on me at the point the client
            has already received the information from the server by base hex.... this rant brought to you by past me to whoever might try to nitpick this implementation

         */

        // request the thing to be rendered, try asking the server once
        String uuidString = wallScroll.getStringUUID();
        ScrungledPatternSending.fromClientThatJustSawWallScroll(resLoc.toString(), uuidString);
    }
}