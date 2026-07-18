package hauveli.hexagony.interop

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.mod.HexTags
import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.graph_crafting.emi.GraphCraftingEmiPlugin
import hauveli.hexagony.features.graph_crafting.emi.GraphCraftingEmiStack
import hauveli.hexagony.registry.HexagonyActions
import hauveli.hexagony.registry.HexagonyRecipeTypes
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

@EmiEntrypoint
class HexagonyEMIPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) {
        registry.addCategory(CRAFT)
        registry.addWorkstation(CRAFT, EmiIngredient.of(HexTags.Items.STAVES))

        // Uhhhhhhhhhh I'm not sure how to get all the recipes registered here.... My datapack ones are ok.
        // The rest are just converted at runtime, though...
        // and I can't really datagen every mods recipes ever and have it work nicely...
        // should I move when I load the recipes to an earlier point? I should probably figure out how to do it less jankily...
        for (recipe in registry.recipeManager
            .getAllRecipesFor(HexagonyRecipeTypes.GRAPH_TYPE.value)) {
            val nodes = recipe.value().centerNode.nodeList
            // I don't really need inputItems after testing but I'm keeping it for now just in case I think of something
            val inputItems = nodes.map { node ->
                EmiIngredient.of(node.validIngredients.map(EmiStack::of))
            }

            val inputEntities = nodes.map { node ->
                GraphCraftingEmiStack(node)
            }
            val output: EmiStack = EmiStack.of(recipe.value().resultInner)

            val syntheticId = Hexagony.id("/graph_crafting/" + recipe.id().path)
            val recipeToRegister = GraphCraftingEmiPlugin(recipe.value().centerNode, inputItems, inputEntities, output, syntheticId)
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