package hauveli.hexagony

import hauveli.hexagony.client.NeoForgeHexagonyClient
import hauveli.hexagony.datagen.NeoForgeHexagonyDatagen
import hauveli.hexagony.features.graph_crafting.GraphCraftingRecipeStuff
import hauveli.hexagony.interop.HexagonyEMIPlugin
import hauveli.hexagony.registry.HexagonyCreativeTabs
import hauveli.hexagony.registry.HexagonyItems
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.registries.RegisterEvent
import java.util.function.BiConsumer
import java.util.function.Consumer


@Mod(Hexagony.MODID)
class NeoForgeHexagony(modBus: IEventBus, container: ModContainer) {
    init {

        modBus.apply {
            addListener(NeoForgeHexagonyClient::init)
            addListener(NeoForgeHexagonyDatagen::init)
            addListener(NeoForgeHexagonyServer::init)
            addListener(::registerCreativeModeTabItems)
        }
        Hexagony.init()
        // my cringe stuff
        NeoForge.EVENT_BUS.addListener(this::onServerStarted)


        fun <T> bind(
            registry: ResourceKey<out Registry<T>>,
            source: Consumer<BiConsumer<T, ResourceLocation>>
        ) {
            modBus.addListener({ event: RegisterEvent ->
                if (registry == event.registryKey) {
                    source.accept(BiConsumer { t: T, rl: ResourceLocation ->
                        event.register<T>(
                            registry,
                            rl
                        ) { t }
                    })
                }
            })
        }
        bind(Registries.CREATIVE_MODE_TAB, HexagonyCreativeTabs::registerCreativeTabs)
    }


    // uhh did I fix this when I updated to 1.21.1? todo: figure out if I did...
    fun onServerStarted(event: ServerStartedEvent) {
        GraphCraftingRecipeStuff.init(event.server.allLevels.first())
    }

    // how do I do this in common? if anybody knows please tell me
    fun registerCreativeModeTabItems(event: BuildCreativeModeTabContentsEvent) {
        Hexagony.LOGGER.info("wtf it runs but doesn't work???? TEST FUCK {}", event.tab.displayName)
        HexagonyItems.registerItemCreativeTab(event, event.tab);
    }


    companion object {
        internal val container: ModContainer
            get() = ModList.get().getModContainerById(Hexagony.MODID).get()
    }
}
