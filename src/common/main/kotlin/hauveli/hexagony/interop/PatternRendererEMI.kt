package hauveli.hexagony.interop

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.utils.isOfTag
import at.petrak.hexcasting.client.render.PatternColors
import at.petrak.hexcasting.client.render.PatternRenderer
import at.petrak.hexcasting.client.render.PatternSettings
import at.petrak.hexcasting.xplat.IXplatAbstractions
import dev.emi.emi.api.render.EmiRenderable
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import kotlin.math.max
import kotlin.math.min

// I literally copypasted https://github.com/FallingColors/HexMod/blob/1.21/Fabric/src/main/java/at/petrak/hexcasting/fabric/interop/emi/PatternRendererEMI.java
// will remove when no longer needed.
class PatternRendererEMI(pattern: ResourceLocation?, w: Int, h: Int) : EmiRenderable {
    private val width: Int
    private val height: Int

    private var xOffset = 0
    private var yOffset = 0

    private var strokeOrder: Boolean

    private val pat: HexPattern?
    private var patSets: PatternSettings

    init {
        val regi = IXplatAbstractions.INSTANCE.getActionRegistry()
        val entry = regi.get(pattern)
        this.strokeOrder = isOfTag<ActionRegistryEntry?>(regi, pattern!!, HexTags.Actions.PER_WORLD_PATTERN)
        this.pat = entry!!.prototype()
        this.width = w
        this.height = h
        this.patSets = PatternSettings(
            "pattern_drawable_" + w + "_" + h,
            PatternSettings.PositionSettings(
                width.toDouble(),
                height.toDouble(),
                0.0,
                0.0,
                PatternSettings.AxisAlignment.CENTER_FIT,
                PatternSettings.AxisAlignment.CENTER_FIT,
                max(width, height).toDouble(),
                0.0,
                0.0
            ),
            PatternSettings.StrokeSettings.fromStroke(0.075 * min(width, height)),
            PatternSettings.ZappySettings.READABLE
        )
    }

    fun shift(x: Int, y: Int): PatternRendererEMI {
        xOffset += x
        yOffset += y
        return this
    }

    fun strokeOrder(order: Boolean): PatternRendererEMI {
        if (order != strokeOrder) {
            patSets = PatternSettings(
                "pattern_drawable_" + width + "_" + height + (if (order) "" else "nostroke"),
                patSets.posSets,
                patSets.strokeSets,
                if (order) PatternSettings.ZappySettings.READABLE else PatternSettings.ZappySettings.STATIC
            )
        }
        strokeOrder = order
        return this
    }

    override fun render(graphics: GuiGraphics, x: Int, y: Int, delta: Float) {
        val ps = graphics.pose()
        ps.pushPose()
        ps.translate((xOffset + x).toFloat(), (yOffset + y + 1).toFloat(), 0f)
        PatternRenderer.renderPattern(
            pat, graphics.pose(), patSets,
            PatternColors(-0x37f3f5f4, -0xcccfd0).withDotColors(-0x7f999c9d, 0),
            0.0, 10
        )
        ps.popPose()
    }
}