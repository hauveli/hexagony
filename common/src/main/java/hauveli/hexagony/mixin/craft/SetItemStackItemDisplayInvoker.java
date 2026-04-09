package hauveli.hexagony.mixin.craft;

import com.mojang.math.Transformation;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemDisplay.class)
public interface SetItemStackItemDisplayInvoker {
    @Invoker("setItemStack")
    void setStack(ItemStack stack);
}