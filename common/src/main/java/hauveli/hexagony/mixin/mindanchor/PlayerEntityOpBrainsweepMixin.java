package hauveli.hexagony.mixin.mindanchor;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.*;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.casting.actions.spells.OpFlight;
import at.petrak.hexcasting.common.casting.actions.spells.great.OpBrainsweep;
import at.petrak.hexcasting.mixin.accessor.AccessorLivingEntity;
import hauveli.hexagony.Hexagony;
import hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor;
import hauveli.hexagony.registry.HexagonyBlocks;
import hauveli.hexagony.registry.HexagonyDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
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

    @Unique
    private static final long MIND_GRAFT_COST =  10 * MediaConstants.CRYSTAL_UNIT; // 1 MILLION
    @Unique
    private static final long MIND_GRAFT_DAMAGE = 1; // base damage it deals to self if no overcast

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


            // TODO:
            // if DamgeSource(holder) replaced with custom named temporary mob, could make
            // the mob name be so ex. "... mind was subsumed into energy while fighting <it>
            // Regardless of if it is valid or not, apply one damage, same as villagers take from it?
            /*
            Holder<DamageType> holder = serverPlayer.level().registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(HexDamageTypes.OVERCAST);
            */
            Holder<DamageType> holder = serverPlayer.level().registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(HexagonyDamageTypes.HIDDEN);
            serverPlayer.hurt(
                    new DamageSource(holder),
                    MIND_GRAFT_DAMAGE);

            // Out of range
            if (!castingEnvironment.canEditBlockAt(pos)) {
                throw new MishapBadLocation(vecPos, "forbidden");
            }

            // I forget what this one prints but I'm sure it's intelligible
            if (!hexagony$isTargetingSelf(sacrifice, castingEnvironment))  {
                throw new MishapImmuneEntity(sacrifice);
            }

            // TODO:
            // Peek at my unenlightened mixin and take the mishap context and error message code from there?

            // Mind has already been used
            if (hexagony$isGrafted(serverPlayer)) {
                ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
                Villager villager = new Villager(
                        EntityType.VILLAGER,
                        serverLevel,
                        VillagerType.PLAINS
                );
                // villager.setCustomName(Component.literal("Bob"));
                throw new MishapAlreadyBrainswept(villager);
            }

            BlockState state = castingEnvironment.getWorld().getBlockState(pos);
            // The X rejected the being's mind error?
            if (!state.is(HexagonyBlocks.INSTANCE.getMIND_ANCHOR_EMPTY().getValue())) {
                ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
                Villager villager = new Villager(
                        EntityType.VILLAGER,
                        serverLevel,
                        VillagerType.PLAINS
                );
                // TODO: do I need to add a name?
                // villager.setCustomName(Component.literal("Bob"));
                throw new MishapBadBrainsweep(villager, pos);
            }

            // remove stack entirely?
            // Hmm... this seems anti-Hexcasting though...
            // TODO: remove only relevant parts of stack based on how cast went
            hexagony$clearEntireStack(castingEnvironment);


            // I couldn't think of a better way to get a fake SpellAction.Result to feed to cir.setReturnValue
            cir.setReturnValue(hexagony$consumeMediaGetResult(serverPlayer, 0));
            cir.cancel();

            // slurp up all mana, do this here because I want to consume Flay Mind?
            // Simulate it instead of casting.
            long simulatedRemainingMedia = castingEnvironment.extractMedia(MIND_GRAFT_COST, true);

            // Only grant attempt if the caster survived in the first place
            if (simulatedRemainingMedia > 0) {
                grantAdvancement(serverPlayer, "graft_attempted");
            }

            // Kill or don't kill player, it doesn't matter.
            if (simulatedRemainingMedia != 0) {
                castingEnvironment.extractMedia(MIND_GRAFT_COST, false);
                return;
            }

            // Do not know what to do but to start, lets move the players position into the target position
            serverPlayer.moveTo(vecPos.add(0.5,0.5,0.5));

            hexagony$theatrics(castingEnvironment, sacrifice, pos);

            graftPlayer(serverPlayer, state, pos);
            grantAdvancement(serverPlayer, "graft_succeeded");
            // I don't think we can ever end up here without going into the if statement with ServerPlayer...
        }
    }

    static private void graftPlayer(ServerPlayer serverPlayer, BlockState state, BlockPos pos) {
        ServerLevel serverLevel = serverPlayer.serverLevel();
        // Create the anchor, todo: get reference to the blockentity?
        hexagony$mindAnchorServerPlayer(serverPlayer, pos);

    }

    @Unique
    static private SpellAction.Result hexagony$consumeMediaGetResult(ServerPlayer serverPlayer, long cost) {
        return new SpellAction.Result(
                new OpFlight.Spell(
                        OpFlight.Type.LimitTime,
                        serverPlayer,
                        0),
                0,
                List.of(),
                0

        );
    }


    @Unique
    static private void hexagony$clearEntireStack(CastingEnvironment castingEnvironment) {
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
    static private void hexagony$mindAnchorServerPlayer(ServerPlayer serverPlayer, BlockPos pos) {
        // world.setBlock(pos, block.defaultBlockState().setValue(FILLED, true), 3);
        ServerLevel serverLevel = serverPlayer.serverLevel();
        Block myBlock = HexagonyBlocks.INSTANCE.getMIND_ANCHOR_FULL().getValue();

        BlockState state = myBlock.defaultBlockState();
        serverLevel.setBlock(pos, state, 3);
        BlockEntity be = serverLevel.getBlockEntity(pos);
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
        ServerLevel serverLevel = ((ServerPlayer) sacrifice).serverLevel();
        for (ServerPlayer player : serverLevel.players()) {
            castingEnvironment.getWorld().playSound(
                    null,
                    player,
                    SoundEvents.GLASS_BREAK, // spooky
                    SoundSource.AMBIENT,
                    1.5f,
                    0.5f
            );
        }
    }



}