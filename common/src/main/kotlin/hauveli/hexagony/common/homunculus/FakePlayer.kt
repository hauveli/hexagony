package hauveli.hexagony.common.homunculus

import com.mojang.authlib.GameProfile
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import java.util.*


class FakePlayer(
    server: MinecraftServer,
    level: ServerLevel,
    pos: BlockPos,
    yRot: Float
) : Player(
    level,
    pos,
    yRot,
    GameProfile(UUID.randomUUID(), "HomunculusDummy")
    ) {
    override fun isSpectator(): Boolean {
        return false
    }

    override fun isCreative(): Boolean {
        return false
    }

    override fun isPushable(): Boolean {
        return true
    }
}