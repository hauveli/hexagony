package hauveli.hexagony.config

import hauveli.hexagony.Hexagony
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.validation.ValidatedField
import me.fzzyhmstrs.fzzy_config.validation.ValidatedField.Companion.descriptionProvider
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import net.minecraft.network.chat.Component

// guide: https://moddedmc.wiki/en/project/fzzy-config/latest/docs/config-design/New-Configs#2-config-creation
class HexagonyCommonConfig : Config(Hexagony.id("common_config")) {


    fun <T> ValidatedField<T>.addDescription(): ValidatedField<T> {
        return this.descriptionProvider(
            { value: T, key: String ->
                Component.translatable("$key.description", value)
            }
        )
    }

    // todo: number entry field that coerces instead of a stupid slider
    // var dummyServerConfigOption: ValidatedInt = ValidatedInt(64, 100, 0).addDescription() as ValidatedInt
    var recoveryPerRest: ValidatedDouble = ValidatedDouble(1.0, 100.0, 0.0).addDescription() as ValidatedDouble
    var maximumHealthPenaltyMultiplier: ValidatedDouble = ValidatedDouble(1.0, 100.0, -100.0).addDescription() as ValidatedDouble
    var overcastDamagePenaltyMultiplier: ValidatedDouble = ValidatedDouble(1.0, 100.0, -100.0).addDescription() as ValidatedDouble
    var overcastDamagePenaltyAdditionalDamage: ValidatedDouble = ValidatedDouble(0.0, 100.0, -100.0).addDescription() as ValidatedDouble
    var requireScrollForEnlightenment: ValidatedBoolean = ValidatedBoolean(true).addDescription() as ValidatedBoolean
    var requireScrollForAllGatedSpells: ValidatedBoolean = ValidatedBoolean(true).addDescription() as ValidatedBoolean

}