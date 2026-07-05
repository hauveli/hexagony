package hauveli.hexagony.config

import hauveli.hexagony.Hexagony
import me.fzzyhmstrs.fzzy_config.config.Config

// guide: https://moddedmc.wiki/en/project/fzzy-config/latest/docs/config-design/New-Configs#2-config-creation
class HexagonyClientConfig : Config(Hexagony.id("client_config")) {

    var testValue = 2.0

}