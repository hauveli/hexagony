package hauveli.hexagony.mixin.enlightenment;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidPattern;
import at.petrak.hexcasting.api.casting.mishaps.MishapUnenlightened;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.items.storage.ItemScroll;

import com.google.common.collect.Iterables;

import hauveli.hexagony.config.HexagonyServerConfig;

import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// import javax.naming.Context; awesopme linting
import java.util.List;

import static at.petrak.hexcasting.api.utils.HexUtils.isOfTag;

//import static at.petrak.hexcasting.common.items.storage.ItemScroll.ANCIENT_PREDICATE;

// from https://github.com/FallingColors/HexMod/blob/532fe9a60138544112e096812c7aefb78b3d7364/Common/src/main/java/at/petrak/hexcasting/api/casting/iota/PatternIota.java

// It should be fine to have ineffecient code here, it is only called before we enlighten and surely, SURELY
// there are no HexCaster so insane they would willingly not enlighten, but also install this mod?

/*
 * This mixin prevents players from enlightening themselves via brute force.
 * An ancient scroll is required for enlightenment if requireScrollForEnlightenment is true.
 */
@Mixin(value = MishapUnenlightened.class, remap = false)
public class RequireScrollMishapUnenlightenedMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true
    )
    private void execute(
            CastingEnvironment env,
            Mishap.Context errorCtx,
            List<Iota> iotaList, // if stack is empty and last iota is greater spell, then what?
            CallbackInfo ci) {
        if (!HexagonyServerConfig.getConfig().getRequireScrollForEnlightenment()) return;
        LivingEntity caster = env.getCastingEntity();
        if (caster.level().isClientSide) return;
        // TODO: Can non-player entities enlighten a player?
        try {
            if (caster instanceof ServerPlayer player) {
                // todo: make level() calls be safe? idk...
                if (hexagony$hasRequiredScrolls(player, errorCtx)) {
                    return; // continue with enlightenment as usual
                }
                // if we don't have what we need, cancel the enlightenment
                ci.cancel();
                throw new MishapInvalidPattern(errorCtx.getPattern());
            }
        } catch (Mishap mishap) {
            Mishap.Context fakeContext = new Mishap.Context(errorCtx.getPattern(), null);
            caster.sendSystemMessage(mishap.errorMessageWithName(env, fakeContext));
        }
    }

    @Unique
    private static boolean hexagony$hasRequiredScrolls(ServerPlayer player, Mishap.Context errorCtx) {
        assert errorCtx.getName() != null;
        if (errorCtx.getName().getContents() instanceof TranslatableContents translatableIota) {
            String iotaTranslationKey = translatableIota.getKey();
            // Loop main inventory
            for (ItemStack stack : Iterables.concat(player.getInventory().items, player.getInventory().offhand, player.getInventory().armor) ) {
                if (stack.getItem() instanceof ItemScroll) {
                    if (stack.getItem().getName(stack).getContents() instanceof TranslatableContents translatableItem) {
                        Object[] itemTranslationArgs = translatableItem.getArgs();
                        for (Object arg : itemTranslationArgs) {
                            if (arg instanceof MutableComponent comp &&
                                    comp.getContents() instanceof TranslatableContents translatableArg) {
                                // This tells us exactly what the item `stack` is
                                String itemTranslationKey = translatableArg.getKey();
                                if (iotaTranslationKey.equals(itemTranslationKey)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}