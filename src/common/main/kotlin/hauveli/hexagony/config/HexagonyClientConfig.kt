package hauveli.hexagony.config

import hauveli.hexagony.Hexagony
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean
import net.minecraft.network.chat.Component

// guide: https://moddedmc.wiki/en/project/fzzy-config/latest/docs/config-design/New-Configs#2-config-creation
class HexagonyClientConfig : Config(Hexagony.id("client_config")) {

    var revealGreatSpellsOnHeldInBook = ValidatedBoolean(true)

}