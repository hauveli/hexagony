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
import at.petrak.hexcasting.common.lib.HexDamageTypes;
import at.petrak.hexcasting.mixin.accessor.AccessorLivingEntity;
import hauveli.hexagony.Hexagony;
import hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor;
import hauveli.hexagony.registry.HexagonyBlocks;
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
import net.minecraft.world.damagesource.DamageSources;
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


            // Regardless of if it is valid or not, apply one damage, same as villagers take from it?
            Holder<DamageType> holder = serverPlayer.level().registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(HexDamageTypes.OVERCAST);;
            serverPlayer.hurt(
                    new DamageSource(holder, castingEnvironment.getCastingEntity()),
                    1);

            // Out of range
            if (!castingEnvironment.canEditBlockAt(pos)) {
                throw new MishapBadLocation(vecPos, "forbidden");
            }

            // I forget what this one prints but I'm sure it's intelligible
            if (!hexagony$isTargetingSelf(sacrifice, castingEnvironment))  {
                throw new MishapImmuneEntity(sacrifice);
            }

            // Mind has already been used
            if (hexagony$isGrafted(serverPlayer)) {
                ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
                Villager villager = new Villager(
                        EntityType.VILLAGER,
                        serverLevel,
                        VillagerType.PLAINS
                );
                villager.setCustomName(Component.literal("Bob"));
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
                villager.setCustomName(Component.literal("Bob"));
                throw new MishapBadBrainsweep(villager, pos);
            }
            // Actually, once it's filled, it becomes type BlockFullMindAnchor... No need to check or even have this property...
            // if (state.getValue(BlockFullMindAnchor.Companion.getFILLED())) return; // if filled, return.
            // currently set to redstone powered
            // world.setBlock(pos, block.defaultBlockState().setValue(FILLED, true), 3)

            // Should I simulate after all?
            long remainingToCast = castingEnvironment.extractMedia(MIND_GRAFT_COST, true);

            // TODO: what error here?
            // No error?
            if (remainingToCast > 0) return;

            grantAdvancement(serverPlayer, "graft_attempted");
            // remove stack entirely?
            // Hmm... this seems anti-Hexcasting though...
            // TODO: remove only relevant parts of stack based on how cast went
            clearEntireStack(castingEnvironment);

            // use a less flashy spell?
            SpellAction.Result fakeResult = OpLightning.INSTANCE.execute(List.of(new Vec3Iota(vecPos)), castingEnvironment);
            cir.setReturnValue(fakeResult);

            // todo: make this cleaner
            remainingToCast = castingEnvironment.extractMedia(MIND_GRAFT_COST, false);
            if (serverPlayer.getHealth() > 0) {
                cir.cancel();
                return;
            }

            hexagony$theatrics(castingEnvironment, sacrifice, pos);

            hexagony$mindAnchorServerPlayer(serverPlayer, serverPlayer.serverLevel(), pos);

            grantAdvancement(serverPlayer, "graft_succeeded");
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
            serverPlayer.sendSystemMessage(Component.nullToEmpty("Block WAS instance of, should be OK!!"));
            ((BlockEntityFullMindAnchor) be)
                    .setPlayer(
                        serverPlayer.getGameProfile(),
                        serverPlayer.getUUID()
                    );
            be.setChanged(); // mark dirty so it saves
        }
        serverPlayer.sendSystemMessage(Component.nullToEmpty(be.toString()));

        serverPlayer.sendSystemMessage(Component.nullToEmpty("POST setting block entity"));
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