package hauveli.hexagony.common.items

import com.google.common.collect.Multimap
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.ItemStack


// import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;

/**
 * Custom Mind Anchor item
 * Binds to a player when used
 */


/**
 * Why don't we just use the same API mod on Forge and Fabric? Beats me. botania does it like this.
 * I feel like botnia probably does it this way becase it's older than xplat curios
 */
interface MindContainerItem {
    fun getMindContainerAttrs(stack: ItemStack?): Multimap<Attribute?, AttributeModifier?>?
}