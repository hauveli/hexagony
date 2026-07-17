package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.mind_anchor.item.ItemMindAnchor
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.JukeboxSong
import net.minecraft.world.item.Rarity

object HexagonyItems : HexagonyRegistrar<Item>(
    BuiltInRegistries.ITEM.key() as ResourceKey<Registry<Item>>,
    { BuiltInRegistries.ITEM }
) {
    val props = Item.Properties()
    val fireResistant = props.fireResistant()
    val unstackable = props.stacksTo(1)
    val fireResistantUnstackable = unstackable.fireResistant()

    private fun musicDiscItem(resourceKey: ResourceKey<JukeboxSong>): Item {
        return Item(unstackable.jukeboxPlayable(resourceKey))
    }

    // BlockItems
    @JvmField
    val MIND_ANCHOR_EMPTY = make("mind_anchor/empty") {
        BlockItem(
            HexagonyBlocks.MIND_ANCHOR_EMPTY.value,   // safe: lazy evaluated during init
            fireResistant // stacking to 64 is default, I think?
                .rarity(Rarity.RARE)
        )
    }

    // BlockItems
    @JvmField
    val MIND_ANCHOR_FULL = make("mind_anchor/full") {
        ItemMindAnchor(
            HexagonyBlocks.MIND_ANCHOR_FULL.value,   // safe: lazy evaluated during init
            fireResistantUnstackable
                .rarity(Rarity.EPIC)
        )
    }

    val MUSIC_DISC_SELULANCE_NIGHT_CODING = make("music_disc/selulance/night_coding") {
        musicDiscItem(HexagonySounds.MUSIC_DISC_SELULANCE_NIGHT_CODING.jukeboxSong)
    }

    private fun <T : Item> make(name: String, builder: () -> T): HexagonyRegistrar<Item>.Entry<T> =
        register(Hexagony.id(name), builder)
}