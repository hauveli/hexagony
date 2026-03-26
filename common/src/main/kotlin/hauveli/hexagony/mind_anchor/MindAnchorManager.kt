package hauveli.hexagony.mind_anchor

import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import hauveli.hexagony.common.blocks.BlockFullMindAnchor
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.Vec3
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object MindAnchorManager {

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
    }

    // todo, do something smart with this instead of accessing runtime[uuid]
    fun getRuntime(uuid: UUID): MindAnchorRuntime? {
        return runtime[uuid]
    }

    fun getPosition(uuid: UUID): Vec3? {
        val rt = runtime[uuid]
        val be = rt?.blockEntity
        val ie = rt?.itemEntity
        val it = rt?.itemStack
        if (be != null) {
            return be.blockPos.center
        } else if (ie != null) {
            return ie.position()
        } else if (it != null) {
            return it.position() // this is actually a PLAYER
        }
        // I really hope this isn't reached in normal circumstances...
        return null
    }

    fun getPowered(uuid: UUID): Boolean? {
        val rt = runtime[uuid]
        val be = rt?.blockEntity
        if (be != null) {
            val bs = be.blockState
            if (bs != null && bs.hasProperty(BlockFullMindAnchor.Companion.POWERED)) {
                return bs.getValue(BlockFullMindAnchor.Companion.POWERED)
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