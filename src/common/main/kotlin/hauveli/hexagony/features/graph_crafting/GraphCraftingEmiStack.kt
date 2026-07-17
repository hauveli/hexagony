package hauveli.hexagony.features.graph_crafting

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.client.ClientTickCounter
import at.petrak.hexcasting.client.render.renderEntity
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredient
import com.mojang.blaze3d.systems.RenderSystem
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import hauveli.hexagony.Hexagony
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.EntityTypeTags
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity
import java.util.stream.Collectors

class GraphCraftingEmiStack(
    val ingredient: GraphCraftingRecipeStuff.ItemNodeVanilla
) : EmiStack() {
    private val id: ResourceLocation?

    init {
        val bareId = this.ingredient.validIngredients.first().descriptionId
        this.id = HexAPI.modLoc(bareId)
    }

    override fun copy(): EmiStack {
        return GraphCraftingEmiStack(this.ingredient)
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun getComponentChanges(): DataComponentPatch? {
        TODO("Not yet implemented")
    }

    val nbt: CompoundTag?
        get() = null

    override fun getKey(): Any? {
        return id
    }

    override fun getId(): ResourceLocation? {
        return id
    }

    override fun getTooltipText(): MutableList<Component?> {
        val mc = Minecraft.getInstance()
        val advanced = mc.options.advancedItemTooltips

        // return ingredient.getTooltip(advanced)
        return mutableListOf(ingredient.validIngredients.first().displayName)
    }


    override fun getTooltip(): MutableList<ClientTooltipComponent?> {
        return getTooltipText()!!.stream()
            .map{ obj: Component? -> obj!!.visualOrderText }
            .map{ fcs: FormattedCharSequence -> ClientTooltipComponent.create(fcs) }
            .collect(Collectors.toList())
    }

    override fun getName(): Component {
        return ingredient.validIngredients.first().displayName
    }

    override fun render(graphics: GuiGraphics?, x: Int, y: Int, delta: Float, flags: Int) {
        if ((flags and RENDER_ICON) != 0) {
            val mc = Minecraft.getInstance()
            val level = mc.level
            if (level != null) {

                // can cycle validIngredients by re-using this approach, I think?
                val examples = ingredient.validIngredients
                var example: Entity? = EntityType.ITEM.create(level)
                if (!examples.isEmpty()) {
                    val seconds = System.currentTimeMillis() / 1000
                    if (example is ItemEntity) {
                        example.item = examples[(seconds % examples.size).toInt()]
                    }
                }
                RenderSystem.enableBlend()
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
                renderEntity(
                    graphics!!,
                    example!!,
                    level,
                    (x + 8).toFloat(),
                    (y + 16).toFloat(),
                    ClientTickCounter.getTotal(),
                    8f,
                    0f
                ) { it: MultiBufferSource? -> it!! }
            }
        }

        //		if ((flags & RENDER_REMAINDER) != 0) {
//			EmiRender.renderRemainderIcon(this, poseStack, x, y);
//		}
    }
}