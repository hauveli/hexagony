package hauveli.hexagony.casting.actions.spells.dump

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedSpiralPatternCastEnv
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapStackSize
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.common.lib.HexDataComponents
import at.petrak.hexcasting.xplat.IXplatAbstractions
import hauveli.hexagony.Hexagony
import hauveli.hexagony.config.HexagonyConfigs
import net.minecraft.nbt.NbtOps
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

object OpDump : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env.castingEntity !is ServerPlayer)
            throw MishapBadCaster()

        if (!HexagonyConfigs.COMMON_CONFIG.dumpSpiralWorksInImpetus.get()
            && env !is StaffCastEnv)
            throw MishapBadCaster()

        val serverPlayer = (env.castingEntity as ServerPlayer)
        val uiPatterns = IXplatAbstractions.INSTANCE.getPatternsSavedInUi(serverPlayer)

        if (uiPatterns.isEmpty())
            throw MishapNoPattern()

        val patternIotas: List<PatternIota> = uiPatterns.map { PatternIota(it.pattern) }

        return patternIotas.asActionResult
    }
}
