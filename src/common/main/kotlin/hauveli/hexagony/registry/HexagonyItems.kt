package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.hat.LivingHatItem
import hauveli.hexagony.features.hat.StupidChudDummyItem
import hauveli.hexagony.features.mind_anchor.item.ItemMindAnchor
import hauveli.hexagony.registry.HexagonyCreativeTabs.HEXAGONY_MAIN_TAB
import hauveli.hexagony.registry.HexagonySounds.MUSIC_DISC_ALBUM_SELULANCE_FRACTAL_FOREST
import hauveli.hexagony.registry.HexagonySounds.MusicDiscEntry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.JukeboxSong
import net.minecraft.world.item.Rarity
import java.util.function.Supplier

object HexagonyItems : HexagonyRegistrar<Item>(
    BuiltInRegistries.ITEM.key() as ResourceKey<Registry<Item>>,
    { BuiltInRegistries.ITEM }
) {
    private val ITEM_TABS: MutableMap<CreativeModeTab, MutableList<() -> TabEntry>> =
        LinkedHashMap<CreativeModeTab, MutableList<() -> TabEntry>>()


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

    val LIVING_HAT = make("living_hat") {
        LivingHatItem(fireResistantUnstackable.rarity(Rarity.EPIC))
    }


    val MUSIC_DISC_SELULANCE_NIGHT_CODING = make("music_disc/selulance/night_coding") {
        musicDiscItem(HexagonySounds.MUSIC_DISC_SELULANCE_NIGHT_CODING.jukeboxSong)
    }

    val ALBUM_FRACTAL_FOREST = makeMusicDiscAlbum(MUSIC_DISC_ALBUM_SELULANCE_FRACTAL_FOREST)

    // the layers because I'm a stupid chud dummy who couldn't figure out how to rotate the modelparts of an item in 1.21.1 properly
    val LIVING_HAT_A = make("living_hat/living_hat_a") { StupidChudDummyItem() }
    val LIVING_HAT_B = make("living_hat/living_hat_b") { StupidChudDummyItem() }
    val LIVING_HAT_C = make("living_hat/living_hat_c") { StupidChudDummyItem() }
    val LIVING_HAT_D = make("living_hat/living_hat_d") { StupidChudDummyItem() }

    private abstract class TabEntry {
        abstract fun register(r: CreativeModeTab.Output?)

        class ItemEntry(private val item: Item) : TabEntry() {
            override fun register(r: CreativeModeTab.Output?) {
                r?.accept(item)
            }
        }

        class StackEntry(private val stack: Supplier<ItemStack>) : TabEntry() {
            override fun register(r: CreativeModeTab.Output?) {
                r?.accept(stack.get())
            }
        }
    }

    @JvmStatic
    fun registerItemCreativeTab(r: CreativeModeTab.Output, tab: CreativeModeTab) {
        for (item in ITEM_TABS.getOrDefault(tab, mutableListOf<() -> TabEntry?>())) {
            item()!!.register(r)
        }
    }

    private fun <T : Item> make(name: String, builder: () -> T): HexagonyRegistrar<Item>.Entry<T> {
        val registered = register(Hexagony.id(name), builder)
        ITEM_TABS.computeIfAbsent(HEXAGONY_MAIN_TAB) {t: CreativeModeTab -> ArrayList() }
            .add({TabEntry.ItemEntry(registered.value)})
        return registered
    }

    private fun makeMusicDiscAlbum(album: List<MusicDiscEntry<SoundEvent>>): List<HexagonyRegistrar<Item>.Entry<Item>> {
        val mutableList = mutableListOf<HexagonyRegistrar<Item>.Entry<Item>>()
        for (track in album) {
            mutableList.addLast(
                make(track.soundEvent.id.path) {
                    musicDiscItem(track.jukeboxSong)
                }
            )
        }
        return mutableList.toList()
    }
}