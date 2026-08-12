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

    private val ITEMS: MutableList<Entry<*>> = mutableListOf()

    @JvmStatic
    fun registerItemCreativeTab(r: CreativeModeTab.Output, tab: CreativeModeTab) {
        if (ITEM_TABS.isEmpty()) {
            for (item in ITEMS) {
                ITEM_TABS.computeIfAbsent(HEXAGONY_MAIN_TAB.value) { t: CreativeModeTab -> ArrayList() }
                    .add({ TabEntry.ItemEntry(item.value) })
            }
        }
        for (item in ITEM_TABS.getOrDefault(tab, mutableListOf<() -> TabEntry?>())) {
            item()!!.register(r)
        }
    }

    private fun <T : Item> make(name: String, builder: () -> T): HexagonyRegistrar<Item>.Entry<T> {
        val registered = register(Hexagony.id(name), builder)
        ITEMS.add(registered)
        return registered
    }

    private fun props(): Item.Properties {return Item.Properties()}
    private fun stacksTo(props: Item.Properties = props(), stackSizeLimit: Int = 64): Item.Properties {
        return props.stacksTo(stackSizeLimit)}
    private fun uncommon(props: Item.Properties = props()): Item.Properties {
        return props.rarity(Rarity.UNCOMMON) }
    private fun rare(props: Item.Properties = props()): Item.Properties {
        return props.rarity(Rarity.RARE) }
    private fun epic(props: Item.Properties = props()): Item.Properties {
        return props.rarity(Rarity.EPIC) }

    private fun unstackable(props: Item.Properties = props()): Item.Properties {
        return stacksTo(props, 1)}

    private fun fireResistant(props: Item.Properties = props()): Item.Properties {
        return props.fireResistant()}

    private fun fireResistantUnstackable(props: Item.Properties = props()): Item.Properties {
        return fireResistant(unstackable(props))}

    private fun fireResistantUncommon(props: Item.Properties = props()): Item.Properties {
        return fireResistant(uncommon(props))}
    private fun fireResistantRare(props: Item.Properties = props()): Item.Properties {
        return fireResistant(rare(props))}
    private fun fireResistantEpic(props: Item.Properties = props()): Item.Properties {
        return fireResistant(epic(props))}

    private fun unstackableUncommon(props: Item.Properties = props()): Item.Properties {
        return unstackable(uncommon(props))}
    private fun unstackableRare(props: Item.Properties = props()): Item.Properties {
        return unstackable(rare(props))}


    private fun unstackableFireResistantUncommon(props: Item.Properties = props()): Item.Properties {
        return unstackable(fireResistantUncommon(props))}
    private fun unstackableFireResistantRare(props: Item.Properties = props()): Item.Properties {
        return unstackable(fireResistantRare(props))}
    private fun unstackableFireResistantEpic(props: Item.Properties = props()): Item.Properties {
        return unstackable(fireResistantEpic(props))}

    fun newItem(): Item {
        return Item(props())
    }

    private fun musicDiscItem(resourceKey: ResourceKey<JukeboxSong>): Item {
        return Item(unstackableRare().jukeboxPlayable(resourceKey))
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

    // BlockItems
    @JvmField
    val MIND_ANCHOR_EMPTY = make("mind_anchor/empty") {
        BlockItem(
            HexagonyBlocks.MIND_ANCHOR_EMPTY.value,   // safe: lazy evaluated during init
            fireResistantRare()
        )
    }

    // BlockItems
    @JvmField
    val MIND_ANCHOR_FULL = make("mind_anchor/full") {
        ItemMindAnchor(
            HexagonyBlocks.MIND_ANCHOR_FULL.value,   // safe: lazy evaluated during init
            unstackableFireResistantEpic()
        )
    }

    val LIVING_HAT = make("living_hat") {
        LivingHatItem(unstackableFireResistantEpic())
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
}