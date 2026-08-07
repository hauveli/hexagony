package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony.MODID
import hauveli.hexagony.Hexagony.id
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import java.util.function.BiConsumer


object HexagonyCreativeTabs {
    val TABS: MutableMap<ResourceLocation, CreativeModeTab> = LinkedHashMap()

    // @JvmStatic
    fun registerCreativeTabs(r: BiConsumer<CreativeModeTab, ResourceLocation>) {
        for (e in TABS.entries) {
            r.accept(e.value, e.key)
        }
    }

    // todo: future self please do this in a cooler simpler way
    @JvmStatic
    fun init() {
        // start what I want to do differently
        fun <T> bind(registry: Registry<in T>): BiConsumer<T, ResourceLocation> =
            BiConsumer<T, ResourceLocation> { t, id ->
                if (t != null) {
                    Registry.register(registry, id, t)
                }
            }

        registerCreativeTabs(bind(BuiltInRegistries.CREATIVE_MODE_TAB))
        // end wiwtdd
    }

    val HEXAGONY_MAIN_TAB = make("main_tab") {
        val tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon({ HexagonyItems.MIND_ANCHOR_EMPTY.value.defaultInstance })
        return@make tab
    }

    // hee heee heeee
    val CreativeModeTab.key: ResourceKey<CreativeModeTab>?
        get() = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(this).orElse(null)

    fun make(name: String, builder: () -> CreativeModeTab.Builder): CreativeModeTab {
        val tab = builder().title(Component.translatable("itemGroup.$MODID.$name.title")).build()
        val old = TABS.put(id(name), tab)
        require(old == null) { "Typo? Duplicate id $name" }
        return tab
    }
}


/*

object HexagonyCreativeTabs : HexagonyRegistrar<CreativeModeTab>(
    BuiltInRegistries.CREATIVE_MODE_TAB.key() as ResourceKey<Registry<CreativeModeTab>>,
    { BuiltInRegistries.CREATIVE_MODE_TAB }
) {
    val TABS: MutableMap<ResourceLocation, CreativeModeTab> = LinkedHashMap()

    val HEXAGONY_MAIN_TAB = make("main_tab") {
        val tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon({ HexagonyItems.MIND_ANCHOR_EMPTY.value.defaultInstance })
            .title(Component.translatable("$MODID.creative_tab.title")).build()
        return@make tab
    }

    // hee heee heeee
    val CreativeModeTab.key: ResourceKey<CreativeModeTab>?
        get() = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(this).orElse(null)

    fun <T : CreativeModeTab> make(name: String, builder: () -> T): HexagonyRegistrar<CreativeModeTab>.Entry<T> {

        TABS[id(name)] = builder()
        return register(id(name), builder)
    }
}

 */