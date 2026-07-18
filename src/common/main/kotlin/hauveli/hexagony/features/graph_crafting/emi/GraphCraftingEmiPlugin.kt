package hauveli.hexagony.features.graph_crafting.emi

import at.petrak.hexcasting.api.HexAPI.modLoc
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff
import hauveli.hexagony.interop.HexagonyEMIPlugin
import net.minecraft.resources.ResourceLocation

class GraphCraftingEmiPlugin(
    val nodeVanilla: GraphCraftingRecipeStuff.ItemNodeVanilla,
    val itemInputs: List<EmiIngredient>,
    val inputEntities: List<GraphCraftingEmiStack>,
    val itemOuput: EmiStack,
    val recipeId: ResourceLocation
) : EmiRecipe {

    override fun getCategory(): EmiRecipeCategory {
        return HexagonyEMIPlugin.CRAFT
    }

    override fun getId(): ResourceLocation {
        return recipeId
    }

    override fun getInputs(): List<EmiIngredient?> {
        return itemInputs
    }

    override fun getOutputs(): List<EmiStack?> {
        return listOf(itemOuput)
    }

    override fun getDisplayWidth(): Int {
        return 118
    }

    override fun getDisplayHeight(): Int {
        return 85
    }

    public override fun addWidgets(widgets: WidgetHolder) {
        /*
        widgets.addTexture(
            OVERLAY, 0, 0,
            this.displayWidth,
            this.displayHeight, 0, 0,
            this.displayWidth,
            this.displayHeight, 128, 128
        )
         */

        SpringyBoingBoing.boing(
            widgets,
            nodeVanilla,
            inputEntities
        )

        // widgets.addSlot(itemOuput, 100, 70).drawBack(false).large(true).recipeContext(this)
        //    .customBackground(null, 0, 0, 19, 19)

        widgets.addSlot(itemOuput, 100, 100).drawBack(false).large(true).recipeContext(this)
            .customBackground(OVERLAY, 0, 0, 19, 19)
    }

    companion object {
        private val OVERLAY: ResourceLocation? = modLoc("textures/gui/brainsweep_jei.png")
    }
}