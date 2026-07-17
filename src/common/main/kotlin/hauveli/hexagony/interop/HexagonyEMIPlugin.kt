package hauveli.hexagony.interop

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry
import at.petrak.hexcasting.interop.utils.PhialRecipeStackBuilder
import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.graph_crafting.GraphCraftingEmiPlugin
import hauveli.hexagony.features.graph_crafting.GraphCraftingEmiStack
import hauveli.hexagony.features.graph_crafting.GraphRecipe
import hauveli.hexagony.registry.HexagonyActions
import hauveli.hexagony.registry.HexagonyRecipeTypes
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeInput

@EmiEntrypoint
class HexagonyEMIPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) {
        registry.addCategory(CRAFT)
        registry.addWorkstation(CRAFT, EmiIngredient.of<Item?>(HexTags.Items.STAVES))


        for (recipe in registry.recipeManager
            .getAllRecipesFor(HexagonyRecipeTypes.GRAPH_TYPE.value)) {
            val nodes = recipe.value().centerNode.nodeList
            // map(node, EmiIngredient(stack))
            val inputItems = nodes.map { node ->
                EmiIngredient.of(node.validIngredients.map(EmiStack::of))
            }

            val inputEntities = nodes.map { node ->
                GraphCraftingEmiStack(node)
            }
            val output: EmiStack = EmiStack.of(recipe.value().resultInner)

            val syntheticId = Hexagony.id("/graph_crafting/" + recipe.id().path)
            val recipeToRegister = GraphCraftingEmiPlugin(inputItems, inputEntities, output, syntheticId)
            registry.addRecipe(recipeToRegister)
        }
    }

    companion object {

        private val SIMPLIFIED_ICON_BRAINSWEEP: ResourceLocation? = HexAPI.modLoc("textures/gui/brainsweep_emi.png")

        val CRAFT: EmiRecipeCategory = EmiRecipeCategory(
            HexagonyActions.CRAFT.id,
            PatternRendererEMI(HexagonyActions.CRAFT.id, 16, 16),
            EmiTexture(SIMPLIFIED_ICON_BRAINSWEEP, 0, 0, 16, 16, 16, 16, 16, 16)
        )

        fun init() {}
    }
}