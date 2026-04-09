package hauveli.hexagony.mind_anchor

import hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID


import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.saveddata.SavedData

// should I just somehow get them all to be part of the same variable instead? hmm...
class MindAnchorRuntime {
    var blockEntity: BlockEntity? = null
        private set

    var itemEntity: ItemEntity? = null
        private set

    var itemStack: ItemStack? = null
        private set

    var entity: Entity? = null
        private set

    fun trackBlock(be: BlockEntity) {
        clear()
        blockEntity = be
    }

    fun trackItemEntity(entity: ItemEntity) {
        clear()
        itemEntity = entity
    }

    fun trackItemStackAndEntity(item: ItemStack, ent: Entity) {
        clear()
        itemStack = item
        entity = ent
    }

    fun clear() {
        blockEntity = null
        itemEntity = null
        itemStack = null
        entity = null
    }
}

data class MindAnchorEntry(
    val mindUUID: UUID,
    var type: AnchorType,
    var activeUUID: UUID?,
    var dimension: ResourceKey<Level>,
    var pos: BlockPos,
    var media: Long
)

enum class AnchorType {
    BLOCK_ENTITY,
    ITEM_ENTITY,
    ITEM_STACK
}

class MindAnchorData : SavedData() {
    val anchors: MutableMap<UUID, MindAnchorEntry> = mutableMapOf()

    /*
    fun resolve(server: MinecraftServer): Pair<ServerLevel, Vec3>? {
        val level = server.getLevel(dimension) ?: return null
        val bPos = BlockPos(
            pos.x.toInt(),
            pos.y.toInt(),
            pos.z.toInt()
        )

        // returning the pair level, vec3
        return when (type) {
            AnchorType.BLOCK_ENTITY -> {
                // Unlikely scenario, but it's possible that the uuid does not match...
                if (level.getBlockEntity(bPos) != null &&
                    (level.getBlockEntity(bPos) as BlockEntityFullMindAnchor).getPlayerUuid() == mindUUID) {
                    level to pos
                } else
                    null
            }

            AnchorType.ITEM_ENTITY,
            AnchorType.ITEM_STACK -> {
                val entity = activeUUID?.let { level.getEntity(it) }
                if (entity != null) {
                    level to entity.position()
                } else {
                    // fallback to last known
                    level to pos
                }
            }
        }
    }
    */

    fun getOrCreate(uuid: UUID): MindAnchorEntry {
        return anchors.getOrPut(uuid) {
            MindAnchorEntry(
                uuid,
                AnchorType.ITEM_STACK,
                null,
                Level.OVERWORLD,
                BlockPos.ZERO,
                0L
            )
        }
    }

    override fun save(tag: CompoundTag): CompoundTag {
        val list = ListTag()

        anchors.values.forEach { entry ->
            val e = CompoundTag()

            e.putUUID("Mind", entry.mindUUID)
            e.putString("Type", entry.type.name)
            e.putString("Dimension", entry.dimension.location().toString())
            entry.activeUUID?.let { e.putUUID("ActiveUUID", it) }

            e.putInt("X", entry.pos.x)
            e.putInt("Y", entry.pos.y)
            e.putInt("Z", entry.pos.z)

            e.putLong("Media", entry.media)

            list.add(e)
        }

        tag.put("Anchors", list)
        return tag
    }

    companion object {

        private const val NAME = "hexagony_mind_anchors"

        fun get(server: MinecraftServer): MindAnchorData {
            return server.overworld().dataStorage.computeIfAbsent(
                ::load,
                ::MindAnchorData,
                NAME
            )
        }

        private fun load(tag: CompoundTag): MindAnchorData {
            val data = MindAnchorData()
            val list = tag.getList("Anchors", Tag.TAG_COMPOUND.toInt())

            for (i in 0 until list.size) {
                val e = list.getCompound(i)

                val uuid = e.getUUID("Mind")
                val type = AnchorType.valueOf(e.getString("Type"))
                val dim = ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation(e.getString("Dimension"))
                )

                val activeUUID =
                    if (e.hasUUID("ActiveUUID")) e.getUUID("ActiveUUID") else null

                val pos = BlockPos(
                    e.getInt("X"),
                    e.getInt("Y"),
                    e.getInt("Z")
                )

                val media = e.getLong("Media")

                data.anchors[uuid] =
                    MindAnchorEntry(uuid, type, activeUUID, dim, pos, media)
            }

            return data
        }
    }
}