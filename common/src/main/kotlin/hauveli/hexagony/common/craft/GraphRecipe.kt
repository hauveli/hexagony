package hauveli.hexagony.common.craft

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

class GraphRecipe(
    private val id: ResourceLocation, // dont need this for a recipe that doesnt exist but having it now might save me a headache
    val centerNode: GraphCraftingRecipes.ItemNodeVanilla,
    val resultId: ResourceLocation
) : Recipe<Container> {

    override fun getId(): ResourceLocation = id
    override fun getSerializer(): RecipeSerializer<*>? {
        TODO("Not yet implemented") // Not gonna bother implementing these at this time
    }

    override fun getType(): RecipeType<*>? {
        TODO("Not yet implemented") // Not gonna bother implementing these at this time
    }


    override fun matches(container: Container, level: Level): Boolean {
        return false // your custom logic
    }

    override fun assemble(container: Container, registryAccess: RegistryAccess): ItemStack {
        return ItemStack(BuiltInRegistries.ITEM.get(resultId))
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean = true

    override fun getResultItem(registryAccess: RegistryAccess): ItemStack {
        return ItemStack(BuiltInRegistries.ITEM.get(resultId))
    }
}