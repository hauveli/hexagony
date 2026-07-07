package hauveli.hexagony.mixin.enlightenment;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.mishaps.MishapUnenlightened;
import com.llamalad7.mixinextras.sugar.Local;
import hauveli.hexagony.config.HexagonyCommonConfig;
import hauveli.hexagony.config.HexagonyConfigs;
import hauveli.hexagony.registry.HexagonyAdvancements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PatternIota.class)
public class RequireScrollPatternIotaMixin {

    @Inject(
            method = "lookupAndOperate",
            at = @At(
                    value = "INVOKE",
                    target = "Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;isEnlightened()Z"
            )
    )
    private void lookupAndOperate(
            CastingVM castingVM,
            SpellContinuation spellContinuation,
            boolean inParens, CallbackInfoReturnable<CastResult> cir,
            @Local(ordinal = 0) ResourceKey<ActionRegistryEntry> key,
            @Local(ordinal = 1) boolean reqsEnlightenment
    ) {
        if (!reqsEnlightenment) return;
        HexagonyCommonConfig conf = HexagonyConfigs.INSTANCE.getCOMMON_CONFIG();
        if (!conf.getRequireScrollForAllGatedSpells().get()) return;
        LivingEntity caster = castingVM.getEnv().getCastingEntity();
        if (caster.level().isClientSide) return;
        if (caster instanceof ServerPlayer player) {
            if (hexagony$hasHeldScroll(player, key)) return;
            throw new MishapUnenlightened();
        }
    }

    @Unique
    private static final String hexagony$advancementTemplate = "hexagony:gated/";

    @Unique
    private static boolean hexagony$hasHeldScroll(ServerPlayer player, ResourceKey<ActionRegistryEntry> resourceKey) {
        String iotaTranslationKey = hexagony$advancementTemplate
                + resourceKey.location().toString()
                .replace("/", "_")
                .replace(":", "/");
        return HexagonyAdvancements.hasAdvancement(player, ResourceLocation.parse(iotaTranslationKey));
    }
}