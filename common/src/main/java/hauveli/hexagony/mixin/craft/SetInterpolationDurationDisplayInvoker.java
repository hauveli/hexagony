package hauveli.hexagony.mixin.craft;

import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface SetInterpolationDurationDisplayInvoker {
    @Invoker("setInterpolationDuration")
    void setLerpDur(int interpolationDuration);
    @Invoker("setInterpolationDelay")
    void setLerpDelay(int interpolationDuration);
    @Invoker("setTransformation")
    void setTrans(Transformation transformation);
    @Invoker("setBillboardConstraints")
    void setBillboard(Display.BillboardConstraints constraints);
}