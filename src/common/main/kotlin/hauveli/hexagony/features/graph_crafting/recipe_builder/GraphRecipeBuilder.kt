package hauveli.hexagony.features.graph_crafting.recipe_builder

import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff
import net.minecraft.advancements.AdvancementRequirements
import net.minecraft.advancements.AdvancementRewards
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.state.BlockState

/*
    TODO:
    write a builder which outputs the json correctly, reminder for rough structure below

        [
            {
                itemAlias = {
                    item: itemB,
                    neighbors: [
                        itemAliasB // inferred that the relationship is bidirected
                    ]
                },
                itemAliasB = {
                    item: itemB
                },
                itemAliasC = {
                    item: itemB,
                    neighbors: [
                        itemAliasB // inferred that the relationship is bidirected
                    ]
                },
                itemAliasD = {
                    item: itemB,
                    neighbors: [
                        itemAliasC // inferred that the relationship is bidirected
                    ]
                },
                itemAliasE = {
                    item: itemB
                    neighbors: [
                        itemAliasD, // inferred that the relationship is bidirected
                        itemAliasB // inferred that the relationship is bidirected
                    ]
                }
            },
            {
                itemAlias = {
                    item: itemB,
                    neighbors: [
                        itemAliasB // inferred that the relationship is bidirected
                    ]
                },
                itemAliasB = {
                    item: itemB
                }
            }
        ]

 */

/*
// Since we have exactly one of each input, we pass them to the constructor.
// Builders for recipe serializers that have ingredient lists of some sort would usually
// initialize an empty list and have #addIngredient or similar methods instead.
class GraphRecipeBuilder(result: ItemStack) :
    SimpleGraphRecipeBuilder(result) {
    // Saves a recipe using the given RecipeOutput and id. This method is defined in the RecipeBuilder interface.
    public override fun save(output: RecipeOutput, id: ResourceLocation) {
        // Build the advancement.
        val advancement = output.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .requirements(AdvancementRequirements.Strategy.OR)
        this.criteria.forEach(advancement::addCriterion)
        // Our factory parameters are the result, the block state, and the ingredient.
        val recipe: GraphRecipeBuilder = GraphCraftingRecipeStuff.
        // Pass the id, the recipe, and the recipe advancement into the RecipeOutput.
        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")))
    }

}
*/