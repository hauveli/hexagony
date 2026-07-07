package hauveli.hexagony.mixin.enlightenment;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidPattern;
import at.petrak.hexcasting.api.casting.mishaps.MishapUnenlightened;
import at.petrak.hexcasting.api.casting.iota.Iota;
import hauveli.hexagony.config.HexagonyCommonConfig;
import hauveli.hexagony.config.HexagonyConfigs;

import hauveli.hexagony.registry.HexagonyAdvancements;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = MishapUnenlightened.class)
public class RequireScrollMishapUnenlightenedMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true
    )
    private void execute(
            CastingEnvironment env,
            Mishap.Context errorCtx,
            List<Iota> iotaList, // if stack is empty and last iota is greater spell, then what? (I forget what I was yapping about but I think the concern is that this list may be empty somehow some way (not true?))
            CallbackInfo ci) {
        HexagonyCommonConfig conf = HexagonyConfigs.INSTANCE.getCOMMON_CONFIG();
        if (!conf.getRequireScrollForEnlightenment().get()) return;
        LivingEntity caster = env.getCastingEntity();
        if (caster.level().isClientSide) return;
        try {
            if (caster instanceof ServerPlayer player) {
                if (hexagony$hasHeldScroll(player, errorCtx)) return;
                // if we don't have what we need, cancel the enlightenment
                ci.cancel();
                throw new MishapInvalidPattern(errorCtx.getPattern());
            }
        } catch (Mishap mishap) {
            // TODO: push garbage onto stack and play bad sound
            // todo: did I do the above already?
            Mishap.Context fakeContext = new Mishap.Context(errorCtx.getPattern(), null);
            caster.sendSystemMessage(mishap.errorMessageWithName(env, fakeContext));
        }
    }
    
    @Unique
    private static final String hexagony$advancementTemplate = "hexagony:gated/";

    @Unique
    private static boolean hexagony$hasHeldScroll(ServerPlayer player, Mishap.Context errorCtx) {
        if (errorCtx.getName() == null) return true; // I don't fucking know
        if (errorCtx.getName().getContents() instanceof TranslatableContents translatableIota) {
            String iotaTranslationKey = hexagony$advancementTemplate
                    + translatableIota.getKey()
                    // I don't like this
                    .replace("hexcasting.action.", "")
                    .replace("/", "_")
                    .replace(":", "/");
            return HexagonyAdvancements.hasAdvancement(player, ResourceLocation.parse(iotaTranslationKey));
        }
        return false;
    }
}