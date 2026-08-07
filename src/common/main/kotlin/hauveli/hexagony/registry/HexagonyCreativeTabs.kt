package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony.MODID
import hauveli.hexagony.Hexagony.id
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab


object HexagonyCreativeTabs : HexagonyRegistrar<CreativeModeTab>(
    BuiltInRegistries.CREATIVE_MODE_TAB.key() as ResourceKey<Registry<CreativeModeTab>>,
    { BuiltInRegistries.CREATIVE_MODE_TAB }
)   {
    val TABS: MutableMap<ResourceLocation, CreativeModeTab> = LinkedHashMap()

    // Fishex would have been a good addon name, too
    val HEXAGONY_MAIN_TAB = register(MODID,
        CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon({ HexagonyItems.MIND_ANCHOR_FULL.value.defaultInstance })
    )
    // hee heee heeee
    val CreativeModeTab.key: ResourceKey<CreativeModeTab>?
        get() = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(this).orElse(null)

    private fun register(name: String, tabBuilder: CreativeModeTab.Builder): HexagonyRegistrar<CreativeModeTab>.Entry<CreativeModeTab> {
        val tab = tabBuilder.title(Component.translatable("$name.creative_tab.title")).build()
        val old = TABS.put(id(name), tab)
        require(old == null) { "Typo? Duplicate id $name" }
        return make(name) {tab}
    }

    private fun <T : CreativeModeTab> make(name: String, builder: () -> T): HexagonyRegistrar<CreativeModeTab>.Entry<T> =
        register(id(name), builder)
}