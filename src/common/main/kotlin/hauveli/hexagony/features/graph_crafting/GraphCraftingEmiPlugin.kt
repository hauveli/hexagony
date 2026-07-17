package hauveli.hexagony.features.graph_crafting

import at.petrak.hexcasting.api.HexAPI.modLoc
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import hauveli.hexagony.interop.HexagonyEMIPlugin
import hauveli.hexagony.interop.TheCoolerSlotWidget
import net.minecraft.resources.ResourceLocation

class GraphCraftingEmiPlugin(
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
        widgets.addTexture(
            OVERLAY, 0, 0,
            this.displayWidth,
            this.displayHeight, 0, 0,
            this.displayWidth,
            this.displayHeight, 128, 128
        )

        // arbitrary placeholders based on code from hexmod, I'm not sure how to best approach the issue here
        // because I have no clue how to use emi
        /*
        inputs.forEachIndexed { index, ingredient ->
            val x = 11 + (index % 3) * 20
            val y = 34 + (index / 3) * 20

            widgets.addSlot(ingredient, x, y)
                .drawBack(false)
                .customBackground(null, 0, 0, 19, 19)
        }
        */

        val centerX = 69
        val centerY = 69
        val radius = 69f

        inputEntities.forEachIndexed { index, entity ->
            val angle = (Math.PI * 2 * index / inputEntities.size) - Math.PI / 2

            val x = centerX + (kotlin.math.cos(angle) * radius).toInt()
            val y = centerY + (kotlin.math.sin(angle) * radius).toInt()

            widgets.add(
                TheCoolerSlotWidget(entity, x, y, 2.5f)
                    .useOffset(false)
                    .customShift(-2.5f, 2.5f)
            )
                .drawBack(false)
                .customBackground(null, 0, 0, 69, 69)
        }

        widgets.addSlot(itemOuput, 86, 34).drawBack(false).large(true).recipeContext(this)
            .customBackground(null, 0, 0, 19, 19)
    }

    companion object {
        private val OVERLAY: ResourceLocation? = modLoc("textures/gui/brainsweep_jei.png")
    }
}