package hauveli.hexagony.mixin.mindanchor;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.*;

import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

@Mixin(MishapInvalidIota.class)
public abstract class HijackBrainsweepMishapInvalidIotaMixin {

    @Shadow @Final private Iota perpetrator;
    @Shadow @Final private Component expected;
    @Shadow @Final private int reverseIdx;

    @Inject(
            method = "errorMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void allowBrainsweepPlayer(
            CastingEnvironment castingEnvironment,
            Mishap.Context errorCtx,
            CallbackInfoReturnable<Component> cir
    ) {
        // We need to ensure that the stack was of form [PlayerEntity(self), Vec3, Brainsweep] at the end
        // The first check (ismob) could be removed, probably? but would need to make some changes
        castingEnvironment.getCastingEntity().sendSystemMessage(Component.nullToEmpty("Entered"));
        if (!hexagony$expectedIsMob(expected)) return;
        castingEnvironment.getCastingEntity().sendSystemMessage(Component.nullToEmpty("Example text"));
        if (!hexagony$perpetratorIsCaster(perpetrator, castingEnvironment)) return;
        if (!hexagony$spellIsBrainsweep(errorCtx, castingEnvironment)) return;

        // https://github.com/YukkuriC/HexOverpowered/blob/9e3789713236056f73f5398ba0f3f7c60dc9cce7/common/src/main/java/io/yukkuric/hexop/actions/mind_env/OpMindStackEdit.kt#L13
        // Massively helpful reference for obtaining stack with CastingEnvironment
        var block = hexagony$blockFromCastingEnv(castingEnvironment);
        // TODO: can remove serverPlayer probably?
        if (hexagony$blockIsValidGraftTarget(block)) {
            if (castingEnvironment.getCastingEntity() instanceof ServerPlayer serverPlayer) {
                // TODO: get media cost from somewhere? 1000000 is the correct cost, at least...?
                // TODO: ensure that the target is a valid block that we want to target...
                long remainingToCast = castingEnvironment.extractMedia(1000000, false);
                serverPlayer.sendSystemMessage(Component.empty().append(String.valueOf(remainingToCast)));
                if (remainingToCast == 0) {
                    if (serverPlayer.getHealth() > 0) {
                        cir.setReturnValue(Component.literal("Your mind resists the spell..."));
                        castingEnvironment.getMishapEnvironment().blind(1000);
                        grantAdvancement(serverPlayer, "graft_attempted");
                    } else {
                        cir.setReturnValue(Component.literal("Your mind is torn"));
                        grantAdvancement(serverPlayer, "graft_attempted"); // Just in case they haven't succeeded already...?
                        grantAdvancement(serverPlayer, "graft_succeeded");
                        // TODO: visuals
                        // castingEnvironment.getMishapEnvironment().dropHeldItems();
                        //SendPacket.toPlayer(serverPlayer, SendPacket.FREEZE_PACKET);
                    }
                    cir.cancel();
                }
            } return;
        }
        // I don't think we can ever end up here without going into the if statement with ServerPlayer...
    }

    @Unique
    private boolean hexagony$blockIsValidGraftTarget(BlockState block) {
        if (block.hasBlockEntity()) return true;
    /*
    if (block.hasBlockEntity() &&
            block. instanceof BlockMindAnchor )
        return false;

     */
        return true;
    }

    @Unique
    private BlockState hexagony$blockFromCastingEnv(CastingEnvironment castingEnvironment) {
        // yes, again, because the number of times this can happen is very low.
        ServerPlayer serverPlayer = (ServerPlayer) castingEnvironment.getCastingEntity();
        var image = IXplatAbstractions.INSTANCE.getStaffcastVM(serverPlayer, castingEnvironment.getCastingHand()).getImage();
        var stack = image.getStack();
        var blockPosIota = stack.get(1);
        Vec3Iota vecIota = (Vec3Iota) blockPosIota;
        BlockPos blockPos = BlockPos.containing(vecIota.getVec3());
        BlockState block = serverPlayer.level().getBlockState(blockPos);
        return block; // This will always succeed I think...
    }

    @Unique
    private boolean hexagony$spellIsBrainsweep(Mishap.Context errorCtx, CastingEnvironment castingEnvironment) {
        if (errorCtx.getName() == null) return false;
        if (errorCtx.getName().getContents() instanceof TranslatableContents translatableErrorContext) {
            return !translatableErrorContext.getKey().equals("hexcasting.action.hexcasting:brainsweep");
        }
        return false;
    }

    @Unique
    private boolean hexagony$expectedIsMob(Component expected) {
        if (expected.getContents() instanceof TranslatableContents translatableExpected) {
            String expectedKey = translatableExpected.getKey();
            return expectedKey.equals("hexcasting.mishap.invalid_value.class.entity.mob");
        }
        return false;
        // TODO: obtain the hexcasting.mishap and hexcasting.action stuff in a cleaner way?
    }

    @Unique
    private boolean hexagony$perpetratorIsCaster(Iota perpetrator, CastingEnvironment castingEnvironment) {
        // check if the caster is also the player
        if (perpetrator instanceof EntityIota) {
            Object payload = ((InterfaceIotaGetPayloadMixin) perpetrator).hex$getPayload();
            if (payload instanceof Entity entityFromIota) {
                if (entityFromIota instanceof ServerPlayer playerFromIota) {
                    var entity = castingEnvironment.getCastingEntity();
                    if (entity instanceof ServerPlayer) {
                        return playerFromIota.equals(entity);
                    }
                }
            }
        }
        return false;
    }

    // Checks if enough health is present to cast.
// Note: Unlike all the other cases, casting this one self HURTS.
// Deals 100 damage. 101 with raycasts.
    @Unique
    private  boolean hexagony$overcastPerfectlyConsumesHealth(ServerPlayer player) {
        if (player.getHealth() == 101) return false;
        return true;
    }
}
        /*
@Mixin(PatternIota.class)
public abstract class PatternIotaMixin {


    @Inject(
            method = "execute",
            at = @At("RETURN")
    )
    private void afterExecute(
            CastingVM vm,
            ServerLevel world,
            SpellContinuation continuation,
            CallbackInfoReturnable<CastResult> cir
    ) {

        // return value at castresult is AFTER the player has died.
        CastResult result = cir.getReturnValue();

        LivingEntity entity = vm.getEnv().getCastingEntity();
        if (entity == null) return;
        entity.sendSystemMessage(Component.empty().append(String.valueOf(entity.getHealth())));

        if (result.getResolutionType() != ResolvedPatternType.EVALUATED)
            return;

        for (OperatorSideEffect effect : result.getSideEffects()) {

            if (effect instanceof OperatorSideEffect.AttemptSpell attempt) {

                Spell spell = attempt.getSpell();

                // Replace with actual brainsweep spell class
                if (spell instanceof OpBrainsweep.Spell brainsweep) {

                    var sacrifice = brainsweep.getSacrifice();

                    if (sacrifice instanceof ServerPlayer player) {

                        if (player.getHealth() <= 0.0f) {

                            hauveli.hexharderhealthcasting.MindAnchorHooks
                                    .graftInsteadOfDeath(player);
                        }
                    }
                }
            }
        }
    }
}
*/