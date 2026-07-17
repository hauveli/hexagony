package hauveli.hexagony.interop

import com.mojang.blaze3d.systems.RenderSystem
import dev.emi.emi.api.render.EmiRender
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.widget.SlotWidget
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import java.util.function.Supplier

// This also copy pasted from hexmod, because I don't want to depend on the Fabric stuff on common
// https://github.com/FallingColors/HexMod/blob/1.21/Fabric/src/main/java/at/petrak/hexcasting/fabric/interop/emi/TheCoolerSlotWidget.java
class TheCoolerSlotWidget(stack: EmiIngredient?, x: Int, y: Int, private val renderScale: Float) :
    SlotWidget(stack, x, y) {
    private var useOffset = true
    private var xShift = 0f
    private var yShift = 0f

    fun useOffset(offset: Boolean): TheCoolerSlotWidget {
        useOffset = offset
        return this
    }

    fun customShift(xShift: Float, yShift: Float): TheCoolerSlotWidget {
        this.xShift = xShift
        this.yShift = yShift
        return this
    }

    override fun render(graphics: GuiGraphics, x: Int, y: Int, delta: Float) {
        val poseStack = graphics.pose()
        val bounds = this.getBounds()
        RenderSystem.setShader(Supplier { GameRenderer.getPositionTexShader() })
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        val width = bounds.width()
        val height = bounds.height()
        if (this.drawBack) {
            if (this.textureId != null) {
                graphics.blit(
                    this.textureId,
                    bounds.x(),
                    bounds.y(),
                    width,
                    height,
                    this.u.toFloat(),
                    this.v.toFloat(),
                    width,
                    height,
                    256,
                    256
                )
            } else {
                if (this.output) {
                    EmiTexture.LARGE_SLOT.render(graphics, bounds.x(), bounds.y(), delta)
                } else {
                    EmiTexture.SLOT.render(graphics, bounds.x(), bounds.y(), delta)
                }
            }
        }

        val xOff = if (useOffset) (width - 16) / 2 else 0
        val yOff = if (useOffset) (height - 16) / 2 else 0
        poseStack.pushPose()
        poseStack.translate(bounds.x() + xOff + xShift, bounds.y() + yOff + yShift, 0f)
        poseStack.scale(renderScale, renderScale, 1f)
        this.getStack().render(graphics, 0, 0, delta)
        if (this.catalyst) EmiRender.renderCatalystIcon(this.getStack(), graphics, 0, 0)
        poseStack.popPose()
    }
}