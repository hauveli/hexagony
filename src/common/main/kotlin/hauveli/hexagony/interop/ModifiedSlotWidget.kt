package hauveli.hexagony.interop

import at.petrak.hexcasting.client.ClientTickCounter
import com.mojang.blaze3d.systems.RenderSystem
import dev.emi.emi.api.render.EmiRender
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.widget.SlotWidget
import hauveli.hexagony.features.graph_crafting.emi.GraphCraftingEmiStack
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.world.phys.Vec3

// This also copy pasted from hexmod, because I don't want to depend on the Fabric stuff on common
// https://github.com/FallingColors/HexMod/blob/1.21/Fabric/src/main/java/at/petrak/hexcasting/fabric/interop/emi/TheCoolerSlotWidget.java
class ModifiedSlotWidget(stack: EmiIngredient?,
                         private val partition: Int,
                         private val x: Double,
                         private val y: Double,
                         private val z: Double,
                         private val renderScale: Float) :
    SlotWidget(stack, 50, 50) {
    private var useOffset = true
    private var xShift = 0f
    private var yShift = 0f
    private val partitionCount: Int = (stack as GraphCraftingEmiStack).ingredient.partitions.count()

    private val ROTATION_SPEED = 0.01

    private fun rotateAroundOrigin(): Vec3 {
        val vec = Vec3(x,y,z)
        val angle = ClientTickCounter.getTotal() * ROTATION_SPEED

        val axis = Vec3(1.0, 1.0, 1.0).normalize()

        val cos = kotlin.math.cos(angle)
        val sin = kotlin.math.sin(angle)

        val dot = vec.dot(axis)

        return vec.scale(cos)
            .add(axis.cross(vec).scale(sin))
            .add(axis.scale(dot * (1 - cos)))
    }

    override fun render(graphics: GuiGraphics, x: Int, y: Int, delta: Float) {
        val poseStack = graphics.pose()
        val bounds = this.getBounds()
        RenderSystem.setShader({ GameRenderer.getPositionTexShader() })
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
                // doesn't make so much sense when rotating a 3d view....
                /*
                if (this.output) {
                    EmiTexture.LARGE_SLOT.render(graphics, bounds.x(), bounds.y(), delta)
                } else {
                    EmiTexture.SLOT.render(graphics, bounds.x(), bounds.y(), delta)
                }
                 */
            }
        }

        val xOff = if (useOffset) (width - 16) / 2 else 0
        val yOff = if (useOffset) (height - 16) / 2 else 0
        poseStack.pushPose()

        val screenCenterX = bounds.x() + bounds.width() / 2.0
        val screenCenterY = bounds.y() + bounds.height() / 2.0

        val rotated = rotateAroundOrigin()

        poseStack.translate(
            screenCenterX + rotated.x,
            screenCenterY + rotated.y,
            rotated.z
        )
        poseStack.scale(renderScale, renderScale, 1f)

        //this.getStack().render(graphics, 0, 0, delta)
        this.getStack().render(graphics, 0, 0, delta)

        // I couldn't figure out why this doesn't work...
        /*
         // I just wanted something not likely to have many overlapping multiples of 1000 because of the other method showing alternate ingredients
        val halfsecondsish = System.currentTimeMillis() / 1567
        val currentPartition = halfsecondsish.toInt() % partitionCount
        if (partition == currentPartition) {
            this.getStack().render(graphics, 0, 0, delta)
        }

         */

        if (this.catalyst) EmiRender.renderCatalystIcon(this.getStack(), graphics, 0, 0)
        poseStack.popPose()
    }
}