package hauveli.hexagony.mixin.mindanchor;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.mishaps.*;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.casting.actions.spells.great.OpLightning;
import at.petrak.hexcasting.common.casting.actions.spells.great.OpBrainsweep;
import at.petrak.hexcasting.mixin.accessor.AccessorLivingEntity;
import hauveli.hexagony.Hexagony;
import hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor;
import hauveli.hexagony.registry.HexagonyBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


import at.petrak.hexcasting.api.casting.OperatorUtils;

import static hauveli.hexagony.common.lib.AdvancementProvider.grantAdvancement;

// import static hauveli.hexagony.common.lib.AdvancementProvider.grantAdvancement;

/*
 * I'm injecting into MishapInvalidIota because it's quite unlikely to cost performance for anyone who
 * is making a Hex, but if it is and you have a better implementation please please please PR
 * this is only my second mod
 * I think it's really funny that the mind graft mod is grafted into an error though, personally.
 * This should also never interfere with any other mods that modify Brainsweep, UNLESS they modify
 * the original mod's method to accept player entities.
 */



@Mixin(value = OpBrainsweep.class, remap = false)
public abstract class PlayerEntityOpBrainsweepMixin {

    private static int FAKE_CAST_COST = 150_000; // cost of the spell that gets rid of the extra junk and adds flair
    private static int MIND_GRAFT_COST = 1_000_000;
    private static int MIND_GRAFT_DAMAGE = 10; // base damage it deals to self if no overcast

    @Shadow @Final private static int argc;

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onExecute(
            List <Iota> args,
            CastingEnvironment castingEnvironment,
            CallbackInfoReturnable<SpellAction.Result> cir
    ) {

        // Can check ServerPlayer another way which won't error?
        LivingEntity sacrifice = OperatorUtils.getLivingEntityButNotArmorStand(args, 0, argc);
        if (sacrifice instanceof Mob) return;
        if (sacrifice instanceof ServerPlayer serverPlayer) {
            Vec3 vecPos = OperatorUtils.getVec3(args, 1, argc);
            BlockPos pos = BlockPos.containing(vecPos);

            // Ensure Ambit stuff
            castingEnvironment.assertVecInRange(vecPos);
            castingEnvironment.assertEntityInRange(sacrifice); // You never know

            if (!castingEnvironment.canEditBlockAt(pos)) {
                throw new MishapBadLocation(vecPos, "forbidden");
            }

            if (!hexagony$isTargetingSelf(sacrifice, castingEnvironment)) return;
            // Todo: error message on fail *because* already grafted?
            if (hexagony$isGrafted(serverPlayer)) return; // maybe I should allow it? but display a message like for villagers...
            /*
            if (IXplatAbstractions.Companion.getINSTANCE().isBrainswept(serverPlayer)) {
                serverPlayer.sendSystemMessage(Component.nullToEmpty( "Player already brainswept" ));
                // Why on earth am I not just checking advancements?
                IXplatAbstractions.Companion.getINSTANCE().setBrainsweepAddlData(sacrifice, false);
                return;
            }
            */

            BlockState state = castingEnvironment.getWorld().getBlockState(pos);
            // well, No more brainsweep recipe use!
            // TODO: verify that BlockState is correct block << HERE
            // TODO: add custom error messages using fake error contexts and such?
            // Might be as easy as setting the cir.returnvalue with a simple fake error?
            // alternatively, Print error myself, and return a successful cast (which wont print an error)
            if (!state.is(HexagonyBlocks.INSTANCE.getMIND_ANCHOR_EMPTY().getValue())) return;
            // Actually, once it's filled, it becomes type BlockFullMindAnchor... No need to check or even have this property...
            // if (state.getValue(BlockFullMindAnchor.Companion.getFILLED())) return; // if filled, return.
            // currently set to redstone powered
            // world.setBlock(pos, block.defaultBlockState().setValue(FILLED, true), 3)

            // Should I simulate after all?
            long remainingToCast = castingEnvironment.extractMedia(MIND_GRAFT_COST, true);
            serverPlayer.sendSystemMessage(Component.nullToEmpty(String.valueOf(remainingToCast)));

            if (remainingToCast > 0) return;

            grantAdvancement(serverPlayer, "graft_attempted");
            // remove stack entirely?
            // Hmm... this seems anti-Hexcasting though...
            clearEntireStack(castingEnvironment);

            SpellAction.Result fakeResult = OpLightning.INSTANCE.execute(List.of(new Vec3Iota(vecPos)), castingEnvironment);
            cir.setReturnValue(fakeResult);

            // todo: make this cleaner
            remainingToCast = castingEnvironment.extractMedia(MIND_GRAFT_COST, false);
            serverPlayer.sendSystemMessage(Component.nullToEmpty(String.valueOf(remainingToCast)));
            // Do we for real need time inbetween these two events? hmm...
            if (serverPlayer.getHealth() > 0) {
                serverPlayer.sendSystemMessage(Component.nullToEmpty(String.valueOf(serverPlayer.getHealth())));
                serverPlayer.sendSystemMessage(Component.nullToEmpty(String.valueOf(serverPlayer.getMaxHealth())));
                serverPlayer.sendSystemMessage(Component.nullToEmpty(String.valueOf("Too much health!")));
                cir.cancel();
                return;
            }
            serverPlayer.sendSystemMessage(Component.nullToEmpty(String.valueOf("Success! Grafted!!")));
            grantAdvancement(serverPlayer, "graft_succeeded");
            serverPlayer.sendSystemMessage(Component.nullToEmpty(String.valueOf("Theatrics next!")));
            hexagony$theatrics(castingEnvironment, sacrifice, pos);
            serverPlayer.sendSystemMessage(Component.nullToEmpty(String.valueOf("Now mind anchoring!")));
            hexagony$mindAnchorServerPlayer(serverPlayer, serverPlayer.serverLevel(), pos);

            // serverPlayer.sendSystemMessage(Component.nullToEmpty( castingImg.toString() ));
            // TODO:
            // ESCAPE!!!!!
            // Result.getCost()
            // is there a reason not to just return?
            // If we are already dead anyway...
            cir.cancel();
            // I don't think we can ever end up here without going into the if statement with ServerPlayer...
        }
    }


    @Unique
    static private void clearEntireStack(CastingEnvironment castingEnvironment) {
        List<Iota> stack = at.petrak.hexcasting.xplat.IXplatAbstractions.INSTANCE
                .getStaffcastVM(
                        (ServerPlayer) castingEnvironment.getCastingEntity(),
                        castingEnvironment.getCastingHand()
                ).getImage().getStack();
        // remove stack entirely
        while(!stack.isEmpty()) {
            stack.remove(0);
        }
    }

    @Unique
    public boolean hexagony$isTargetingSelf(LivingEntity sacrifice, CastingEnvironment castingEnvironment) {
        return sacrifice.equals(castingEnvironment.getCastingEntity());
    }

    @Unique
    public boolean hexagony$isGrafted(ServerPlayer serverPlayer) {
        var adv = serverPlayer.getServer().getAdvancements().getAdvancement(
                new ResourceLocation(Hexagony.MODID, "graft_succeeded"));
        if (adv == null)
            return false;

        return serverPlayer.getAdvancements().getOrStartProgress(adv).isDone();
    }

    @Unique
    static private void hexagony$mindAnchorServerPlayer(ServerPlayer serverPlayer, ServerLevel level, BlockPos pos) {
        // IXplatAbstractions.Companion.getINSTANCE().setBrainsweepAddlData(entity, true);
        serverPlayer.sendSystemMessage(Component.nullToEmpty("Helo!!!!! Mind broken!!!"));

        // world.setBlock(pos, block.defaultBlockState().setValue(FILLED, true), 3);
        Block myBlock = HexagonyBlocks.INSTANCE.getMIND_ANCHOR_FULL().getValue();

        BlockState state = myBlock.defaultBlockState();
        level.setBlock(pos, state, 3);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlockEntityFullMindAnchor) {
            ((BlockEntityFullMindAnchor) be)
                    .setPlayer(
                        serverPlayer.getGameProfile(),
                        serverPlayer.getUUID()
                    );
            be.setChanged(); // mark dirty so it saves
        }
    }

    @Unique
    static private void hexagony$theatrics(CastingEnvironment castingEnvironment, LivingEntity sacrifice, Vec3i pos) {

        // Death sound via accessor
        SoundEvent sound =
                ((AccessorLivingEntity) sacrifice).hex$getDeathSound();

        ParticleSpray.cloud(sacrifice.position(), 3.0, 100).sprayParticles(
                ((ServerPlayer) sacrifice).serverLevel(),
                FrozenPigment.ANCIENT.get()); // TODO: should I respect the player's custom particle choice?

        ParticleSpray.burst(Vec3.atCenterOf(pos), 0.9, 100).sprayParticles(
                ((ServerPlayer) sacrifice).serverLevel(),
                FrozenPigment.ANCIENT.get()); // TODO: should I respect the player's custom particle choice?

        if (sound != null) {
            castingEnvironment.getWorld().playSound(
                    null,
                    sacrifice,
                    sound,
                    SoundSource.AMBIENT,
                    0.8f,
                    1f
            );
        }

        // world shatters
        castingEnvironment.getWorld().playSound(
                null,
                sacrifice,
                SoundEvents.GLASS_BREAK, // spooky
                SoundSource.AMBIENT,
                1.5f,
                0.5f
        );
    }



}