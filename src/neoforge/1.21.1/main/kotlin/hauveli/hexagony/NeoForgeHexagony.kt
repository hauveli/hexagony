package hauveli.hexagony

import hauveli.hexagony.client.NeoForgeHexagonyClient
import hauveli.hexagony.datagen.NeoForgeHexagonyDatagen
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod

@Mod(Hexagony.MODID)
class NeoForgeHexagony(modBus: IEventBus, container: ModContainer) {
    init {
        modBus.apply {
            addListener(NeoForgeHexagonyClient::init)
            addListener(NeoForgeHexagonyDatagen::init)
            addListener(NeoForgeHexagonyServer::init)
        }
        Hexagony.init()
    }

    companion object {
        internal val container: ModContainer
            get() = ModList.get().getModContainerById(Hexagony.MODID).get()
    }
}
