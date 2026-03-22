package hauveli.hexagony.datagen.recipe

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexTags

import at.petrak.hexcasting.common.blocks.decoration.BlockAkashicLog
import at.petrak.hexcasting.common.items.ItemStaff
import at.petrak.hexcasting.common.items.pigment.ItemDyePigment
import at.petrak.hexcasting.common.items.pigment.ItemPridePigment
import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.recipe.SealThingsRecipe
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredient
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.EntityTypeIngredient
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.VillagerIngredient
import at.petrak.hexcasting.datagen.HexAdvancements
import at.petrak.hexcasting.datagen.IXplatConditionsBuilder
import at.petrak.hexcasting.datagen.IXplatIngredients
import at.petrak.hexcasting.datagen.recipe.builders.BrainsweepRecipeBuilder
import at.petrak.hexcasting.datagen.recipe.builders.CompatIngredientValue
import at.petrak.hexcasting.datagen.recipe.builders.CreateCrushingRecipeBuilder
import at.petrak.hexcasting.datagen.recipe.builders.FarmersDelightCuttingRecipeBuilder
import hauveli.hexagony.registry.HexagonyBlocks
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.ServerInterface
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.DyeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import org.apache.commons.codec.binary.Hex
import java.util.List
import java.util.Map
import java.util.function.Consumer
import java.util.function.Function

class HexplatRecipes {
}


// TODO: need to do a big refactor of this class cause it's giant and unwieldy, probably as part of #360
class HexagonyXplatRecipes (
    output: PackOutput, ingredients: IXplatIngredients,
    conditions: Function<RecipeBuilder?, IXplatConditionsBuilder?>
) {
    private val ingredients: IXplatIngredients
    private val conditions: Function<RecipeBuilder?, IXplatConditionsBuilder?>

    init {
        this.ingredients = ingredients
        this.conditions = conditions
    }

    public fun buildRecipes(recipes: Consumer<FinishedRecipe?>) {
        /*
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, HexItems.ARTIFACT)
            .define('F', ingredients.goldIngot())
            .define(
                'A',
                HexItems.CHARGED_AMETHYST
            ) // why in god's name does minecraft have two different places for item tags
            .define('D', ItemTags.MUSIC_DISCS)
            .pattern(" F ")
            .pattern("FAF")
            .pattern(" D ")
            .unlockedBy("has_item", at.petrak.paucal.api.datagen.PaucalRecipeProvider.hasItem(HexTags.Items.STAVES))
            .save(recipes)
        */

        /*
        // val enlightenment = HexAdvancements.ENLIGHTEN
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HexagonyBlocks.MIND_ANCHOR as ItemLike)
            .define('B', Items.IRON_BARS)
            .define('A', HexItems.CHARGED_AMETHYST)
            .define('S', HexBlocks.SLATE_BLOCK)
            .define('P', Items.PURPUR_BLOCK)
            .pattern("PSS")
            .pattern("BAB")
            .pattern("SSP")
            // .unlockedBy("enlightenment", enlightenment).
            .save(recipes)

        // TODO: fix this, but honestly?
        // Unless it's done in HexMod I really can't be bothered,
        // I personally am only going to use this for one method I think...
        BrainsweepRecipeBuilder(
            StateIngredientHelper.of(HexBlocks.IMPETUS_EMPTY),
            EntityIngredient(ServerPlayer, 1),
            Blocks.BUDDING_AMETHYST.defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10
        )
            .unlockedBy("enlightenment", enlightenment) // todo: custom criterion
            .save(recipes, ResourceLocation("hexagony", "brainsweep/budding_amethyst"))
        */

        // todo: Make the Mind Anchor be a block that starts a countdown to remove itself upon placing?
        // Would incentivize using spells...?

        fun specialRecipe(consumer: Consumer<FinishedRecipe?>, serializer: SimpleCraftingRecipeSerializer<*>) {
            val name = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer)
            SpecialRecipeBuilder.special(serializer).save(consumer, HexAPI.MOD_ID + ":dynamic" + name!!.getPath())
        }
    }
}