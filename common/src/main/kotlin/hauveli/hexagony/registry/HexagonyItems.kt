package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

object HexagonyItems : HexagonyRegistrar<Item>(
    BuiltInRegistries.ITEM.key() as ResourceKey<Registry<Item>>,
    { BuiltInRegistries.ITEM }
) {
    // BlockItems
    @JvmField
    val MIND_ANCHOR = make("mind_anchor") {
        BlockItem(
            HexagonyBlocks.MIND_ANCHOR.value,   // safe: lazy evaluated during init
            Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant()
        )
    }

    private fun <T : Item> make(name: String, builder: () -> T): HexagonyRegistrar<Item>.Entry<T> =
        register(Hexagony.id(name), builder)
}