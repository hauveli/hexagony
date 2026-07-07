package hauveli.hexagony.config

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.api.RegisterType

object HexagonyConfigs {
    const val BASE_KEY = ".config."

    //instance of your config loaded from file and automatically registered to the SyncedConfigRegistry and ClientConfigRegistry using the getId() method
    val COMMON_CONFIG = ConfigApi.registerAndLoadConfig(::HexagonyCommonConfig)

    //adding the registerType, you can register a config as client-only. No syncing will occur. Useful for client-only mods.
    val CLIENT_CONFIG = ConfigApi.registerAndLoadConfig(::HexagonyClientConfig, RegisterType.CLIENT)

    fun init() {}
}