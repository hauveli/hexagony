package hauveli.hexagony.features.graph_crafting

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.client.ClientTickCounter
import at.petrak.hexcasting.client.render.renderEntity
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredient
import com.mojang.blaze3d.systems.RenderSystem
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.entity.Entity
import java.util.stream.Collectors

class GraphCraftingEmiStack(val ingredient: BrainsweepeeIngredient) : EmiStack() {
    private val id: ResourceLocation?

    init {
        val bareId = this.ingredient.getSomeKindOfReasonableIDForEmi()
        this.id = HexAPI.modLoc(bareId)
    }

    override fun copy(): EmiStack {
        return GraphCraftingEmiStack(this.ingredient)
    }

    override fun isEmpty(): Boolean {
        return false
    }

    val nbt: CompoundTag?
        get() = null

    override fun getKey(): Any? {
        return id
    }

    override fun getId(): ResourceLocation? {
        return id
    }

    override fun getTooltipText(): MutableList<Component?>? {
        val mc = Minecraft.getInstance()
        val advanced = mc.options.advancedItemTooltips

        return ingredient.getTooltip(advanced)
    }

    override fun getTooltip(): MutableList<ClientTooltipComponent?> {
        return getTooltipText()!!.stream()
            .map<FormattedCharSequence?> { obj: Component? -> obj!!.getVisualOrderText() }
            .map<ClientTooltipComponent?> { `$$0`: FormattedCharSequence? -> ClientTooltipComponent.create(`$$0`) }
            .collect(Collectors.toList())
    }

    override fun getName(): Component? {
        return ingredient.getName()
    }

    override fun render(graphics: GuiGraphics?, x: Int, y: Int, delta: Float, flags: Int) {
        if ((flags and RENDER_ICON) != 0) {
            val mc = Minecraft.getInstance()
            val level = mc.level
            if (level != null) {
                val examples = this.ingredient.exampleEntities(level)
                var example: Entity? = null
                if (!examples.isEmpty()) {
                    val seconds = System.currentTimeMillis() / 1000
                    example = examples.get((seconds % examples.size).toInt())
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