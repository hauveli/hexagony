package hauveli.hexagony.mixin.mindanchor;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.RenderedSpell;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.mishaps.*;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.casting.actions.spells.great.OpBrainsweep;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry;
import at.petrak.hexcasting.mixin.accessor.AccessorLivingEntity;
import com.llamalad7.mixinextras.sugar.Local;
import hauveli.hexagony.xplat.IXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;


import at.petrak.hexcasting.api.casting.OperatorUtils;

import static hauveli.hexagony.common.lib.AdvancementProvider.grantAdvancement;
import static org.spongepowered.asm.mixin.injection.selectors.ElementNode.listOf;

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

    @Shadow @Final private static int argc;

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onExecute(
            List <Iota> args,
            CastingEnvironment castingEnvironment,
            CallbackInfoReturnable<Component> cir
    ) {

        // Can check ServerPlayer another way which won't error?
        LivingEntity sacrifice = OperatorUtils.getLivingEntityButNotArmorStand(args, 0, argc);
        if (sacrifice instanceof Mob) {
            return; // Go back to the usual brainsweep
        } else if (sacrifice instanceof ServerPlayer serverPlayer) {
            Vec3 vecPos = OperatorUtils.getVec3(args, 1, argc);
            BlockPos pos = BlockPos.containing(vecPos);

            castingEnvironment.assertVecInRange(vecPos);
            castingEnvironment.assertEntityInRange(sacrifice);

            if (!castingEnvironment.canEditBlockAt(pos)) {
                throw new MishapBadLocation(vecPos, "forbidden");
            }

            if (IXplatAbstractions.Companion.getINSTANCE().isBrainswept(serverPlayer)) {
                serverPlayer.sendSystemMessage(Component.nullToEmpty( "Player already brainswept" ));
                //
                //throw new MishapAlreadyBrainswept(sacrifice);
                return;
            }
            serverPlayer.sendSystemMessage(Component.nullToEmpty( "Player not brainswept" ));

            BlockState state = castingEnvironment.getWorld().getBlockState(pos);

            RecipeManager recman = castingEnvironment.getWorld().getRecipeManager();
            List<BrainsweepRecipe> recipes = recman.getAllRecipesFor(HexRecipeStuffRegistry.BRAINSWEEP_TYPE);
            BrainsweepRecipe result = recipes.stream()
                    .filter(r -> r.matches(state, sacrifice, castingEnvironment.getWorld()))
                    .findFirst()
                    .orElse(null);
            if (result == null) return;

            serverPlayer.sendSystemMessage(Component.nullToEmpty( "Recipe found" ));

            SpellAction.Result spellResult = new SpellAction.Result(
                    new Spell(pos, state, sacrifice, result),
                    result.mediaCost(),
                    List.of(
                            ParticleSpray.cloud(sacrifice.position(), 1.0, 100),
                            ParticleSpray.burst(Vec3.atCenterOf(pos), 0.3, 100)
                    ),
                    1
            );
            serverPlayer.sendSystemMessage(Component.nullToEmpty( "Spell casted?" ));
            serverPlayer.sendSystemMessage(Component.nullToEmpty( spellResult.toString() ));
            IXplatAbstractions.Companion.getINSTANCE().setBrainsweepAddlData(serverPlayer);
            // serverPlayer.sendSystemMessage(Component.nullToEmpty( castingImg.toString() ));
            // TODO:
            // ESCAPE!!!!!
            cir.cancel();
            // I don't think we can ever end up here without going into the if statement with ServerPlayer...
        }
    }
}

class Spell implements RenderedSpell {

    private final BlockPos pos;
    private final BlockState state;
    private final LivingEntity sacrifice;
    private final BrainsweepRecipe recipe;

    public Spell(BlockPos pos, BlockState state, LivingEntity sacrifice, BrainsweepRecipe recipe) {
        this.pos = pos;
        this.state = state;
        this.sacrifice = sacrifice;
        this.recipe = recipe;
    }

    @Override
    public @Nullable CastingImage cast(@NotNull CastingEnvironment castingEnvironment, @NotNull CastingImage castingImage) {
        // Replace block
        castingEnvironment.getWorld().setBlockAndUpdate(
                pos,
                BrainsweepRecipe.copyProperties(state, recipe.result())
        );

        // Brainsweep
        // HexAPI.instance().brainsweep(sacrifice);
        // Set
        mindAnchorLivingEntity(sacrifice);

        // Death sound via accessor
        SoundEvent sound =
                ((AccessorLivingEntity) sacrifice).hex$getDeathSound();

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
                0.5f,
                0.8f
        );
        return castingImage;
    }

    static private void mindAnchorLivingEntity(LivingEntity entity) {
        IXplatAbstractions xplatAbstractions = IXplatAbstractions.Companion.getINSTANCE();
        if (xplatAbstractions != null) {
            xplatAbstractions.setBrainsweepAddlData(entity);
        }
        entity.sendSystemMessage(Component.nullToEmpty("Helo!!!!! Mind broken!!!"));
    }

    @Override
    public void cast(@NotNull CastingEnvironment castingEnvironment) {
        CastingEnvironment _dummy =  (castingEnvironment);
    }
}