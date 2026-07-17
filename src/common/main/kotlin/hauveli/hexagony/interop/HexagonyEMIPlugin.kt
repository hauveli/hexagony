package hauveli.hexagony.interop

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry
import at.petrak.hexcasting.interop.utils.PhialRecipeStackBuilder
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import hauveli.hexagony.features.graph_crafting.EmiGraphCraftingRecipe
import hauveli.hexagony.features.graph_crafting.GraphCraftingEmiStack
import hauveli.hexagony.features.graph_crafting.GraphRecipe
import hauveli.hexagony.registry.HexagonyActions
import hauveli.hexagony.registry.HexagonyRecipeTypes
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeInput

class HexagonyEMIPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) {
        registry.addCategory(CRAFT)
        registry.addWorkstation(CRAFT, EmiIngredient.of<Item?>(HexTags.Items.STAVES))


        for (recipe in registry.getRecipeManager()
            .getAllRecipesFor(HexagonyRecipeTypes.GRAPH_TYPE.value)) {
            val nodes = recipe.value().centerNode.nodeList
            val validIngredientsPerNode = nodes.first().validIngredients // change this to be every validIngredient, or at least the first one to start with
            val inputItems: EmiIngredient? = EmiIngredient.of(
                recipe.value().centerNode.getDisplayedStacks().stream()
                    .map<EmiStack?> { stack: ItemStack? -> EmiStack.of(stack) }.toList()
            )
            val inputEntity: GraphCraftingEmiStack = GraphCraftingEmiStack(recipe.value().centerNode.nodeList)
            val output: EmiStack? = EmiStack.of(recipe.value().resultInner)
            registry.addRecipe(EmiGraphCraftingRecipe(inputBlocks, inputEntity, output, recipe.getId()))
        }
        /*

        if (PhialRecipeStackBuilder.shouldAddRecipe()) {
            registry.addRecipe(EmiPhialRecipe())
        }

        registry.addRecipe(EmiEdifyRecipe())

         */
    }

    companion object {

        private val SIMPLIFIED_ICON_BRAINSWEEP: ResourceLocation? = HexAPI.modLoc("textures/gui/brainsweep_emi.png")

        val CRAFT: EmiRecipeCategory = EmiRecipeCategory(
            HexagonyActions.CRAFT.id,
            PatternRendererEMI(HexagonyActions.CRAFT.id, 16, 16),
            EmiTexture(SIMPLIFIED_ICON_BRAINSWEEP, 0, 0, 16, 16, 16, 16, 16, 16)
        )
    }
}