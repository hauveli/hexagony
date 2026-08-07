package hauveli.hexagony.registry

import at.petrak.hexcasting.common.lib.HexBrainsweepeeIngredients
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredientType
import hauveli.hexagony.Hexagony.id
import hauveli.hexagony.features.mind_anchor.MindgraftPlayerEntityTypeIngredient
import net.minecraft.resources.ResourceLocation
import java.util.function.BiConsumer


object HexagonyBrainsweepeeIngredients : HexBrainsweepeeIngredients() {
    val MINDGRAFT_ENTITY_TYPE: BrainsweepeeIngredientType<MindgraftPlayerEntityTypeIngredient?> = MindgraftPlayerEntityTypeIngredient.Type()

    @JvmStatic
    fun registerBrainsweepeeIngredients(r: BiConsumer<BrainsweepeeIngredientType<*>, ResourceLocation>) {
        r.accept(MINDGRAFT_ENTITY_TYPE, id("mindgraft_brainsweep"))
    }
}