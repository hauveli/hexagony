package hauveli.hexagony.common.bilocation

import com.mojang.authlib.GameProfile
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.config.HexagonyServerConfig.config
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
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