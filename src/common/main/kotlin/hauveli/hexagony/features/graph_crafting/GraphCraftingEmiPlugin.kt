package hauveli.hexagony.features.graph_crafting

import at.petrak.hexcasting.api.HexAPI.modLoc
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import hauveli.hexagony.interop.HexagonyEMIPlugin
import net.minecraft.resources.ResourceLocation
import java.util.List


class EmiGraphCraftingRecipe(
    blockInput: EmiIngredient?,
    villagerInput: EmiIngredient?,
    output: EmiStack?,
    id: ResourceLocation?
) : EmiRecipe {
    val category: EmiRecipeCategory
        get() = HexagonyEMIPlugin.CRAFT

    val inputs: MutableList<EmiIngredient>
        get() = List.of<EmiIngredient?>(blockInput, villagerInput)

    val outputs: MutableList<EmiStack>
        get() = List.of<EmiStack?>(output)

    val displayWidth: Int
        get() = 118

    val displayHeight: Int
        get() = 85

    public override fun addWidgets(widgets: WidgetHolder) {
        widgets.addTexture(
            OVERLAY, 0, 0,
            this.displayWidth,
            this.displayHeight, 0, 0,
            this.displayWidth,
            this.displayHeight, 128, 128
        )
        widgets.addSlot(blockInput, 11, 34).drawBack(false).customBackground(null, 0, 0, 19, 19)

        widgets.add(TheCoolerSlotWidget(villagerInput, 37, 19, 2.75f).useOffset(false).customShift(-8.5f, 2.485f))
            .drawBack(false).customBackground(null, 0, 0, 27, 49)

        widgets.addSlot(output, 86, 34).drawBack(false).large(true).recipeContext(this)
            .customBackground(null, 0, 0, 19, 19)
    }

    val blockInput: EmiIngredient?
    val villagerInput: EmiIngredient?
    val output: EmiStack?

    val id: ResourceLocation?

    init {
        this.blockInput = blockInput
        this.villagerInput = villagerInput
        this.output = output
        this.id = id
    }

    companion object {
        private val OVERLAY: ResourceLocation? = modLoc("textures/gui/brainsweep_jei.png")
    }
}