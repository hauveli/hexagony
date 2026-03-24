package hauveli.hexagony.mind_anchor

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.phys.Vec3
import java.util.UUID
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries

class MindAnchorData : SavedData() {

    val anchors: MutableMap<UUID, MindAnchorEntry> = mutableMapOf()

    // Fetch or create the saved data for the overworld
    companion object {
        private const val DATA_NAME = "hexagony_mind_anchors"

        fun get(server: MinecraftServer): MindAnchorData {
            return server.overworld().dataStorage.computeIfAbsent(::load, ::MindAnchorData, DATA_NAME)
        }

        fun load(tag: CompoundTag): MindAnchorData {
            val data = MindAnchorData()
            val list = tag.getList(DATA_NAME, Tag.TAG_COMPOUND.toInt())

            for (i in 0 until list.size) {
                val entryTag = list.getCompound(i)

                val mindUUID = entryTag.getUUID("mindUUID")
                val holderUUID = entryTag.getUUID("holderUUID")
                val type = entryTag.getString("Type")
                val dimension = ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation(entryTag.getString("Dimension"))
                )
                val pos = BlockPos(
                    entryTag.getDouble("X").toInt(),
                    entryTag.getDouble("Y").toInt(),
                    entryTag.getDouble("Z").toInt()
                )

                val location = when (type) {
                    "BLOCK_ENTITY" -> AnchorLocation.AsBlock(dimension, pos)
                    else -> AnchorLocation.InEntity(holderUUID)
                }

                data.anchors[mindUUID] = MindAnchorEntry(
                    mindUUID, location,
                    lastKnownPos = pos.center,
                    lastKnownDimension = dimension,
                )
            }

            return data
        }
    }

    // Get a specific mind anchor
    fun getMindAnchor(mindUUID: UUID): MindAnchorEntry? = anchors[mindUUID]

    // Add or update a mind anchor
    fun setMindAnchor(
        mindUUID: UUID,
        holderUUID: UUID,
        type: String,
        dimension: ResourceLocation,
        pos: Vec3
    ) {
        val location = when (type) {
            "BLOCK_ENTITY" -> AnchorLocation.AsBlock(
                ResourceKey.create(Registries.DIMENSION, dimension),
                BlockPos(pos.x.toInt(), pos.y.toInt(), pos.z.toInt())
            )
            else -> AnchorLocation.InEntity(holderUUID)
        }

        anchors[mindUUID] = MindAnchorEntry(
            mindUUID, location,
            lastKnownPos = pos,
            lastKnownDimension = ResourceKey.create(Registries.DIMENSION, dimension),
        )
        setDirty() // mark for saving
    }

    // Save all anchors to NBT
    override fun save(tag: CompoundTag): CompoundTag {
        val list = ListTag()

        anchors.values.forEach { entry ->
            val entryTag = CompoundTag()
            entryTag.putUUID("mindUUID", entry.mindUUID)

            when (val loc = entry.location) {
                is AnchorLocation.AsBlock -> {
                    entryTag.putString("holderType", "BLOCK_ENTITY")
                    entryTag.putString("Dimension", loc.dimension.location().toString())
                    entryTag.putDouble("X", loc.pos.x.toDouble())
                    entryTag.putDouble("Y", loc.pos.y.toDouble())
                    entryTag.putDouble("Z", loc.pos.z.toDouble())
                    entryTag.putUUID("holderUUID", UUID(0, 0)) // placeholder
                }

                is AnchorLocation.InEntity -> {
                    entryTag.putString("holderType", "ENTITY")
                    entryTag.putUUID("holderUUID", loc.entityUUID)
                    entryTag.putString("Dimension", "minecraft:overworld") // optional placeholder
                    entryTag.putDouble("X", 0.0) // optional placeholder
                    entryTag.putDouble("Y", 0.0)
                    entryTag.putDouble("Z", 0.0)
                }
            }

            list.add(entryTag)
        }

        tag.put(DATA_NAME, list)
        return tag
    }
}