package hauveli.hexagony.config

import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.healthcasting.OvercastUtils
import me.fzzyhmstrs.fzzy_config.annotations.Translation
import me.fzzyhmstrs.fzzy_config.annotations.Version
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup
import me.fzzyhmstrs.fzzy_config.util.Walkable
import me.fzzyhmstrs.fzzy_config.validation.ValidatedField
import me.fzzyhmstrs.fzzy_config.validation.ValidatedField.Companion.descriptionProvider
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedCondition
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber
import net.minecraft.network.chat.Component


@Version(version = 0)
@Translation(prefix = Hexagony.MODID + HexagonyConfigs.BASE_KEY + "common")
// guide: https://moddedmc.wiki/en/project/fzzy-config/latest/docs/config-design/New-Configs#2-config-creation
class HexagonyCommonConfig : Config(Hexagony.id("common_config")) {

    class HealthcastingOption(mode: OvercastUtils.ValueMode, perInstance: Double, additional: Double): Walkable {
        constructor(): this(OvercastUtils.ValueMode.NONE, 1.0, 1.0)

        var mode = ValidatedEnum(mode, ValidatedEnum.WidgetType.CYCLING)
        @ValidatedDouble.Restrict(min = 0.0, type = ValidatedNumber.WidgetType.TEXTBOX_WITH_BUTTONS)
        var perInstance = perInstance // ValidatedDouble(perInstance)
        @ValidatedDouble.Restrict(min = 0.0, type = ValidatedNumber.WidgetType.TEXTBOX_WITH_BUTTONS)
        var additional = additional // ValidatedDouble(additional)
    }

    var configGroupHealthcasting = ConfigGroup("healthcasting", collapsedByDefault = true)
    // Healthcasting stuff
    var healthcastingRest = HealthcastingOption(OvercastUtils.ValueMode.CONSTANT, 1.0,0.0)
    var healthcastingPenalty = HealthcastingOption(OvercastUtils.ValueMode.VARIABLE, 1.0,0.0)
    var healthcastingDamage = HealthcastingOption(OvercastUtils.ValueMode.NONE, 0.0,0.0)
    @ConfigGroup.Pop
    @ConfigGroup.Pop


    var configGroupEnlightenment = ConfigGroup("enlightenment", collapsedByDefault = true)
    // Enlightenmnt stuff
    var requireScrollForEnlightenment: ValidatedBoolean = ValidatedBoolean(true)
    var requireScrollForAllGatedSpells: ValidatedCondition<Boolean> = ValidatedBoolean(true)
        .toCondition(
            requireScrollForEnlightenment,
            Component.translatable("hexagony.config.gated_spells.condition_not_met"),
            { false })

}