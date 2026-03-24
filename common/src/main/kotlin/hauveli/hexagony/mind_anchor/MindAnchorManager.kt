package hauveli.hexagony.mind_anchor

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID

object MindAnchorManager {

    fun getAnchor(server: MinecraftServer, mindUUID: UUID): MindAnchorEntry? {
        val data = MindAnchorData.get(server)
        return data.getMindAnchor(mindUUID)
    }

    private fun updateLastKnown(entry: MindAnchorEntry, server: MinecraftServer) {
        when (val loc = entry.location) {
            is AnchorLocation.AsBlock -> {
                entry.lastKnownPos = Vec3(loc.pos.x + 0.5, loc.pos.y + 0.5, loc.pos.z + 0.5) //hmm.... I should use .center somehow
                entry.lastKnownDimension = loc.dimension
            }
            is AnchorLocation.InEntity -> {
                val entity = server.allLevels
                    .firstNotNullOfOrNull { it.getEntity(loc.entityUUID) } // Wow this is barely readable!!! Tahnk you linter!!
                if (entity != null) {
                    entry.lastKnownPos = entity.position()
                    entry.lastKnownDimension = entity.level().dimension()
                }
            }
        }
    }

    fun trackEntity(server: MinecraftServer, mindUUID: UUID, holder: Entity) {
        val data = MindAnchorData.get(server)
        val entry = data.getMindAnchor(mindUUID)

        val newLocation = AnchorLocation.InEntity(holder.uuid)

        if (entry != null) {
            entry.location = newLocation
        } else {
            data.anchors[mindUUID] = MindAnchorEntry(
                mindUUID,
                newLocation,
                holder.position(),
                holder.level().dimension()
            )
        }

        data.getMindAnchor(mindUUID)?.let { updateLastKnown(it, server) }
        data.setDirty()
    }

    fun trackEntityByUUID(server: MinecraftServer, mindUUID: UUID, holderUUID: UUID) {
        val data = MindAnchorData.get(server)
        val entry = data.getMindAnchor(mindUUID)

        val newLocation = AnchorLocation.InEntity(holderUUID)
        val holderEntity = getEntityByUuid(server, holderUUID)

        if (entry != null) {
            entry.location = newLocation
        } else {
            // doesn't make ANY sense if this is null, does it?
            if (holderEntity != null) {
                data.anchors[mindUUID] = MindAnchorEntry(
                    mindUUID,
                    newLocation,
                    holderEntity.position(),
                    holderEntity.level().dimension()
                )
            }
        }

        data.setDirty()
    }

    fun getEntity(server: MinecraftServer, mindUUID: UUID): Entity? {
        val entry = getAnchor(server, mindUUID) ?: return null
        val loc = entry.location
        if (loc is AnchorLocation.InEntity) {
            return getEntityByUuid(server, loc.entityUUID)
        }
        return null
    }

    fun getEntityByUuid(server: MinecraftServer, toFindUuid: UUID): Entity? {
        for (level in server.allLevels) {
            level.getEntity(toFindUuid)?.let { return it }
        }
        return null
    }

    fun trackBlock(server: MinecraftServer, mindUUID: UUID, level: ServerLevel, pos: BlockPos) {
        val data = MindAnchorData.get(server)
        val newLocation = AnchorLocation.AsBlock(level.dimension(), pos)
        val entry = data.getMindAnchor(mindUUID)

        if (entry != null) {
            entry.location = newLocation
        } else {
            data.anchors[mindUUID] = MindAnchorEntry(
                mindUUID,
                newLocation,
                pos.center,
                level.dimension()
            )
        }

        data.getMindAnchor(mindUUID)?.let { updateLastKnown(it, server) }
        data.setDirty()
    }

    fun getBlockPos(server: MinecraftServer, mindUUID: UUID): BlockPos? {
        val entry = getAnchor(server, mindUUID) ?: return null
        return when (val loc = entry.location) {
            is AnchorLocation.AsBlock -> loc.pos
            else -> null
        }
    }

    fun getDimension(server: MinecraftServer, mindUUID: UUID): ResourceKey<Level>? {
        val entry = getAnchor(server, mindUUID) ?: return null
        return when (val loc = entry.location) {
            is AnchorLocation.AsBlock -> loc.dimension
            is AnchorLocation.InEntity -> getEntity(server, mindUUID)?.level()?.dimension()
        }
    }

    fun getLastKnownPos(server: MinecraftServer, mindUUID: UUID): Vec3? =
        getAnchor(server, mindUUID)?.lastKnownPos

    fun getLastKnownDimension(server: MinecraftServer, mindUUID: UUID): ResourceKey<Level>? =
        getAnchor(server, mindUUID)?.lastKnownDimension

    fun getBestGuessPos(server: MinecraftServer, mindUUID: UUID): Vec3 {
        val posBlock = getBlockPos(server, mindUUID)
        val posEntity = getEntity(server, mindUUID)?.position()
        val posOld = getLastKnownPos(server, mindUUID)
        // ORDER OF PRIORITY: pos_block > pos_entity > pos_old
        if (posBlock != null) {
            return posBlock.center
        } else if (posEntity != null) {
            return posEntity
        } else if (posOld != null) {
            return posOld
        }
        return Vec3(0.0,0.0,0.0) // I would hope Vec3 memoizes this one...
    }


    fun moveAnchor(
        server: MinecraftServer,
        mindUUID: UUID,
        holder: Entity? = null,
        level: ServerLevel? = null,
        pos: BlockPos? = null
    ) {
        when {
            holder != null -> trackEntity(server, mindUUID, holder)
            level != null && pos != null -> trackBlock(server, mindUUID, level, pos)
        }
    }

    fun moveAnchor(
        server: MinecraftServer,
        mindUUID: UUID,
        holder: Entity
    ) {
        trackEntity(server, mindUUID, holder)
    }

    fun moveAnchor(
        server: MinecraftServer,
        mindUUID: UUID,
        level: ServerLevel,
        pos: BlockPos
    ) {
        trackBlock(server, mindUUID, level, pos)
    }
}