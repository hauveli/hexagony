package hauveli.hexagony.features.graph_crafting.emi

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.client.ClientTickCounter
import at.petrak.hexcasting.client.render.renderEntity
import com.mojang.blaze3d.systems.RenderSystem
import dev.emi.emi.api.stack.EmiStack
import hauveli.hexagony.Hexagony.MINECRAFT
import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.stream.Collectors

// This is basically a copy of BrainsweepeeEmiStack from hexmod.
// Modified to show alternate items for my node thingies.
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
        val mc = MINECRAFT!!
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
            val mc = MINECRAFT!!
            val level = mc.level
            if (level != null) {

                // can cycle validIngredients by re-using this approach, I think?
                val examples = ingredient.validIngredients
                var example: Entity? = EntityType.ITEM.create(level)
                // var example: Entity? = EntityType.ITEM_DISPLAY.create(level)
                if (!examples.isEmpty()) {
                    val seconds = System.currentTimeMillis() / 1000
                    if (example is ItemEntity) {
                        // if (example is Display.ItemDisplay) {
                        // example.itemStack = examples[(seconds % examples.size).toInt()]
                        // example.billboardConstraints = Display.BillboardConstraints.FIXED
                        example.item = examples[(seconds % examples.size).toInt()]
                    }
                }
                RenderSystem.enableBlend()
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

                renderEntity(
                    graphics!!,
                    example!!,
                    level,
                    0f,
                    0f,
                    0f, // ClientTickCounter.getTotal()
                    8f,
                    0f
                ) { it }
            }
        }

        //		if ((flags & RENDER_REMAINDER) != 0) {
//			EmiRender.renderRemainderIcon(this, poseStack, x, y);
//		}
    }
}