package hauveli.hexagony.features.mind_anchor


import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.saveddata.SavedData
import java.util.*

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

    var media: Long? = null

    var lastSeenTick: Int? = null

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
    var graftUUID: UUID?,
    var dimension: ResourceKey<Level>,
    var pos: BlockPos,
    var media: Long
)

enum class AnchorType {
    BLOCK_ENTITY,
    ITEM_ENTITY,
    ITEM_STACK
}


class MindAnchorSavedData : SavedData() {
    val anchors: MutableMap<UUID, MindAnchorEntry> = mutableMapOf()


    fun isGrafted(uuid: UUID): Boolean {
        return anchors[uuid] != null
    }

    fun getOrCreate(uuid: UUID): MindAnchorEntry {
        return anchors.getOrPut(uuid) {
            MindAnchorEntry(
                uuid,
                AnchorType.ITEM_STACK,
                null,
                null,
                Level.OVERWORLD,
                BlockPos.ZERO,
                0L
            )
        }
    }

    override fun save(
        tag: CompoundTag,
        registries: HolderLookup.Provider
    ): CompoundTag {
        val list = ListTag()

        anchors.values.forEach { entry ->
            val e = CompoundTag()

            e.putUUID("Mind", entry.mindUUID)
            e.putString("Type", entry.type.name)
            e.putString("Dimension", entry.dimension.location().toString())
            entry.activeUUID?.let { e.putUUID("ActiveUUID", it) }
            entry.graftUUID?.let { e.putUUID("graftUUID", it) }

            e.putInt("X", entry.pos.x)
            e.putInt("Y", entry.pos.y)
            e.putInt("Z", entry.pos.z)

            e.putLong("Media", entry.media)

            list.add(e)
        }

        tag.put("Anchors", list)

        return tag
    }

    fun foo() {
        // Change data in saved data

        // Mark the data as needing to be saved
        setDirty()
    }

    companion object {
        fun create(): MindAnchorSavedData {
            return MindAnchorSavedData()
        }

        private const val NAME = "hexagony_mind_anchors"
        fun load(
            tag: CompoundTag,
            lookupProvider: HolderLookup.Provider
        ): MindAnchorSavedData {
            val data = create()
            val list = tag.getList("Anchors", Tag.TAG_COMPOUND.toInt())

            for (i in 0 until list.size) {
                val e = list.getCompound(i)

                val uuid = e.getUUID("Mind")
                val type = AnchorType.valueOf(e.getString("Type"))
                val dim = ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.parse(e.getString("Dimension"))
                )

                val activeUUID =
                    if (e.hasUUID("ActiveUUID")) e.getUUID("ActiveUUID") else null

                val graftUUID =
                    if (e.hasUUID("graftUUID")) e.getUUID("graftUUID") else null

                val pos = BlockPos(
                    e.getInt("X"),
                    e.getInt("Y"),
                    e.getInt("Z")
                )

                val media = e.getLong("Media")

                data.anchors[uuid] =
                    MindAnchorEntry(uuid, type, activeUUID, graftUUID, dim, pos, media)
            }

            return data
        }


        fun get(server: MinecraftServer): MindAnchorSavedData {
            return server.overworld().dataStorage.computeIfAbsent(
                SavedData.Factory(
                    MindAnchorSavedData::create,
                    MindAnchorSavedData::load,
                    DataFixTypes.PLAYER
                ),
                NAME
            )
        }

    }
}

                /*
                val uuid = e.getUUID("Mind")
                    ?: continue

                val typeString = e.getString("Type")
                    ?: continue

                val type = try {
                    AnchorType.valueOf(typeString)
                } catch (_: IllegalArgumentException) {
                    continue
                }

                val dimensionString = e.getString("Dimension")
                    ?: continue

                val dimension = ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.parse(dimensionString)
                )

                val activeUUID = e.getUUID("ActiveUUID")

                val graftUUID = e.getUUID("GraftUUID")

                val pos = BlockPos(
                    e.getInt("X"),
                    e.getInt("Y"),
                    e.getInt("Z")
                )

                val media = e.getLong("Media")

                data.anchors[uuid] = MindAnchorEntry(
                    mindUUID = uuid,
                    type = type,
                    activeUUID = activeUUID,
                    graftUUID = graftUUID,
                    dimension = dimension,
                    pos = pos,
                    media = media
                )
            }

                 */
