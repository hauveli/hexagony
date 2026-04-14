package hauveli.hexagony.common.mind_anchor

import hauveli.hexagony.common.blocks.anchors.MindAnchor.Companion.TAG_STORED_PLAYER
import net.minecraft.server.MinecraftServer
import net.minecraft.world.Container
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3
import java.util.UUID

object MindAnchorScanner {

    enum class MessageTypes {
        POSITION
    }

    fun scanForMindAnchor(server: MinecraftServer, mindUUID: UUID): Vec3? {

        for (level in server.allLevels) {

            for (entity in level.allEntities) {
                if (entity is ItemEntity) {
                    val tag = entity.item.tag ?: continue
                    val compTag = tag.getCompound("BlockEntityTag")
                    if (!compTag.getUUID(TAG_STORED_PLAYER).equals(mindUUID)) continue
                    return entity.position()

                }

                if (entity is Container) {
                    for (i in 0 until entity.containerSize) {
                        val stack = entity.getItem(i)
                        val tag = stack.tag ?: continue
                        val compTag = tag.getCompound("BlockEntityTag")
                        if (!compTag.getUUID(TAG_STORED_PLAYER).equals(mindUUID)) continue
                        return entity.position()
                    }
                }
            }

            /*
            level.blockTicks
            // Idk how to make it check for just blockEntities
            val result = mutableListOf<BlockEntity>()

            // Iterate all loaded chunks
            level.chunkSource.chunkMap.getChunks().forEach { chunkHolder ->
                val chunk = chunkHolder.getTickingChunk() ?: return@forEach
                result += chunk.blockEntities.values
            }
            */
        }

        return null
    }
}