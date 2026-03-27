package hauveli.hexagony.casting.actions.spells

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import com.mojang.authlib.GameProfile
import hauveli.hexagony.common.client.FreeCamAPI
import hauveli.hexagony.mind_anchor.MindAnchorManager.getPosition
import net.minecraft.client.Minecraft
import net.minecraft.client.telemetry.TelemetryProperty
import net.minecraft.commands.arguments.GameModeArgument
import net.minecraft.core.UUIDUtil
import net.minecraft.server.commands.GameModeCommand
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerPlayerGameMode
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3
import java.util.UUID

object OpMindAnchorPosition : ConstMediaAction  {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = env.castingEntity
        val level = target?.level()

        println("What..")
        if (level != null) {
            target as ServerPlayer
            // target.lookAt()
            if (target.gameMode.isSurvival) {
                //target.setGameMode(GameType.SPECTATOR)
                val dummy = ServerPlayer(
                    target.server,
                    target.serverLevel(),
                    GameProfile(
                        UUID.randomUUID(),
                        "TEST"
                    )
                )
                //dummy.setPos(Vec3(0.0,10.0,0.0))
                // level.addFreshEntity(dummy)
                //target.camera = dummy.camera
                // target.forward = Vec3(0.0,0.0,0.0) // not working because .forward is val

            } else {
                target.setGameMode(GameType.SURVIVAL)
            }
            val mc = Minecraft.getInstance()
            //FreeCamAPI(mc).detachCamera()
            val player = mc.player
            println("Hmm...")
            if (player != null) {
                player.input.jumping = true
                println("Erm...?")
            }
        }

        if (target != null) {
            env.assertEntityInRange(target)
            val pos = getPosition(target.uuid)
            if (pos != null) {
                return listOf(Vec3Iota(pos))
            }
        }
        return listOf(GarbageIota())
    }
}
