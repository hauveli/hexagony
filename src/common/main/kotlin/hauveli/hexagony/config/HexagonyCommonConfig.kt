package hauveli.hexagony.config

import hauveli.hexagony.Hexagony
import hauveli.hexagony.features.healthcasting.OvercastUtils
import me.fzzyhmstrs.fzzy_config.annotations.Translation
import me.fzzyhmstrs.fzzy_config.annotations.Version
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
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

    // todo: add a detailed description text field in this class to explain in greater detail how it works
    class HealthcastingOption(mode: OvercastUtils.ValueMode, perInstance: Double, additional: Double): Walkable {
        constructor(): this(OvercastUtils.ValueMode.NONE, 1.0, 1.0)
        var mode = ValidatedEnum(mode, ValidatedEnum.WidgetType.SCROLLABLE)
        @ValidatedDouble.Restrict(min = 0.0, type = ValidatedNumber.WidgetType.TEXTBOX_WITH_BUTTONS)
        var perInstance = perInstance // ValidatedDouble(perInstance)
        @ValidatedDouble.Restrict(min = 0.0, type = ValidatedNumber.WidgetType.TEXTBOX_WITH_BUTTONS)
        var additional = additional // ValidatedDouble(additional)
    }

    var healthcastingGroup = ConfigGroup("healthcasting_group", collapsedByDefault = true)
    // Healthcasting stuff
    var healthcastingRest = HealthcastingOption(OvercastUtils.ValueMode.CONSTANT, 1.0,0.0)
    var healthcastingPenalty = HealthcastingOption(OvercastUtils.ValueMode.VARIABLE, 1.0,0.0)
    @ConfigGroup.Pop // a bit confusing that it has to be here and not one below...
    var healthcastingDamage = HealthcastingOption(OvercastUtils.ValueMode.NONE, 0.0,0.0)

    var enlightenmentGroup = ConfigGroup("enlightenment_group", collapsedByDefault = true)
    // Enlightenmnt stuff
    var requireScrollForEnlightenment: ValidatedBoolean = ValidatedBoolean(true)
    @ConfigGroup.Pop
    var requireScrollForAllGatedSpells: ValidatedCondition<Boolean> = ValidatedBoolean(true)
        .toCondition(
            requireScrollForEnlightenment,
            Component.translatable("hexagony.config.gated_spells.condition_not_met"),
            { false })

    var greatSpellSettingsGroup = ConfigGroup("great_spells_settings_group")
    @ConfigGroup.Pop
    var dumpSpiralWorksInImpetus: ValidatedBoolean = ValidatedBoolean(true)
}