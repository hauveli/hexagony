package hauveli.hexagony.mind_anchor

import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import hauveli.hexagony.common.blocks.BlockFullMindAnchor
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.networking.msg.MsgMindAnchorPositionS2C
import hauveli.hexagony.networking.msg.MsgPlayerControlIntegerS2C
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object MindAnchorManager {

    enum class MessageTypes {
        POSITION
    }

    var localPos: Vec3? = null

    private val runtime = ConcurrentHashMap<UUID, MindAnchorRuntime>()

    private fun runtime(uuid: UUID) =
        runtime.computeIfAbsent(uuid) { MindAnchorRuntime() }

    fun trackBlock(
        server: MinecraftServer,
        mindUUID: UUID,
        blockEntity: BlockEntity
    ) {
        val data = MindAnchorData.get(server)
        val entry = data.getOrCreate(mindUUID)

        entry.type = AnchorType.BLOCK_ENTITY
        entry.activeUUID = null
        entry.dimension = blockEntity.level!!.dimension()
        entry.pos = blockEntity.blockPos

        runtime(mindUUID).trackBlock(blockEntity)

        data.setDirty()

        forwardToUuid(entry.mindUUID, server, entry.pos.center)
    }

    fun trackItemEntity(
        server: MinecraftServer,
        mindUUID: UUID,
        entity: ItemEntity
    ) {
        val data = MindAnchorData.get(server)
        val entry = data.getOrCreate(mindUUID)

        entry.type = AnchorType.ITEM_ENTITY
        entry.activeUUID = entity.uuid
        entry.dimension = entity.level().dimension()
        entry.pos = entity.blockPosition()

        runtime(mindUUID).trackItemEntity(entity)

        data.setDirty()

        forwardToUuid(entry.mindUUID, server, entity.position())
    }

    fun trackItemStack(
        server: MinecraftServer,
        mindUUID: UUID,
        holder: Entity
    ) {
        val data = MindAnchorData.get(server)
        val entry = data.getOrCreate(mindUUID)

        entry.type = AnchorType.ITEM_STACK
        entry.activeUUID = holder.uuid
        entry.dimension = holder.level().dimension()
        entry.pos = holder.blockPosition()

        runtime(mindUUID).trackItemStack(holder)

        data.setDirty()
        forwardToUuid(entry.mindUUID, server, holder.position())
    }

    // todo, do something smart with this instead of accessing runtime[uuid]
    fun getRuntime(uuid: UUID): MindAnchorRuntime? {
        return runtime[uuid]
    }

    fun forwardToUuid(uuid: UUID, server: MinecraftServer, vec: Vec3) {
        val player = server.playerList.getPlayer(uuid) ?: return
        forwardToPlayer(player, vec) // Update the player's info...
    }

    fun forwardToPlayer(serverPlayer: ServerPlayer, vec: Vec3) {
        HexagonyNetworking.CHANNEL.sendToPlayer(
            serverPlayer,
            MsgMindAnchorPositionS2C(
                MessageTypes.POSITION,
                vec.toVector3f()
            )
        )
    }

    // Gotta sync the position just in case...
    fun initServer() {
        PlayerEvent.PLAYER_JOIN.register {
            serverPlayer ->
            onJoin(serverPlayer)
        }
    }

    fun onJoin(serverPlayer: ServerPlayer) {
        val pos = getPosition(serverPlayer) ?: return
        forwardToPlayer(serverPlayer, pos)
    }

    fun getPosition(serverPlayer: ServerPlayer): Vec3? {
        val rt = runtime[serverPlayer.uuid]
        val be = rt?.blockEntity
        val ie = rt?.itemEntity
        val it = rt?.itemStack
        val pos: Vec3?
        if (be != null) {
            pos = be.blockPos.center
        } else if (ie != null) {
            pos = ie.position()
        } else if (it != null) {
            pos = it.position() // this is actually a PLAYER
        } else {
            pos = null
        }
        return pos // can be null...
    }

    fun getPowered(uuid: UUID): Boolean? {
        val rt = runtime[uuid]
        val be = rt?.blockEntity
        if (be != null) {
            val bs = be.blockState
            if (bs != null && bs.hasProperty(BlockFullMindAnchor.POWERED)) {
                return bs.getValue(BlockFullMindAnchor.POWERED)
            }
        }
        // I really hope this isn't reached in normal circumstances...
        return null
    }

    fun getSignalStrength(uuid: UUID): Double? {
        val rt = runtime[uuid]
        val be = rt?.blockEntity
        if (be != null) {
            val level = be.level
            if (level != null) {
                return level.getDirectSignalTo(be.blockPos).toDouble()// I
            }
        }
        // I really hope this isn't reached in normal circumstances...
        return null
    }
}