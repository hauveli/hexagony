package hauveli.hexagony.mixin.craft;

import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(RecipeManager.class)
abstract class OnReloadRecipeManagerMixin {

    // I've got no fucking clue which method to target. My decompiler in intellij isn't cooperating and reinstalling intellij doesn't help.
    /*
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    private void addGraphRecipes(
            Map<ResourceLocation, JsonElement> jsons,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo ci
    ) {

    }
     */
}