package hauveli.hexagony.mind_anchor

import hauveli.hexagony.common.blocks.anchors.MindAnchor.Companion.TAG_STORED_PLAYER
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.phys.Vec3
import java.util.UUID

object MindAnchorScanner {

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

        }

        return null
    }
}