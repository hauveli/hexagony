package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.mind_anchor.item.ItemMindAnchor
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Rarity

object HexagonyItems : HexagonyRegistrar<Item>(
    BuiltInRegistries.ITEM.key() as ResourceKey<Registry<Item>>,
    { BuiltInRegistries.ITEM }
) {
    // BlockItems
    @JvmField
    val MIND_ANCHOR_EMPTY = make("mind_anchor/empty") {
        BlockItem(
            HexagonyBlocks.MIND_ANCHOR_EMPTY.value,   // safe: lazy evaluated during init
            Item.Properties()
                .stacksTo(64)
                .rarity(Rarity.RARE)
                .fireResistant()
        )
    }

    // BlockItems
    @JvmField
    val MIND_ANCHOR_FULL = make("mind_anchor/full") {
        ItemMindAnchor(
            HexagonyBlocks.MIND_ANCHOR_FULL.value,   // safe: lazy evaluated during init
            Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant()
        )
    }

    private fun <T : Item> make(name: String, builder: () -> T): HexagonyRegistrar<Item>.Entry<T> =
        register(Hexagony.id(name), builder)
}