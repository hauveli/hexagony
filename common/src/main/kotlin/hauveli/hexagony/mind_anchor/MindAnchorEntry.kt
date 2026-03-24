package hauveli.hexagony.mind_anchor

import at.petrak.hexcasting.api.utils.containsTag
import hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor
import hauveli.hexagony.registry.HexagonyBlocks
import hauveli.hexagony.registry.HexagonyItems
import hauveli.hexagony.registry.HexagonyRegistrar
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.levelgen.WorldDimensions
import net.minecraft.world.phys.Vec3
import java.util.UUID
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.LevelStem
import kotlin.math.min

sealed class AnchorLocation {
    data class InEntity(
        val entityUUID: UUID
    ) : AnchorLocation()

    data class AsBlock(
        val dimension: ResourceKey<Level>,
        val pos: BlockPos
    ) : AnchorLocation()
}

data class MindAnchorEntry(
    val mindUUID: UUID,
    var location: AnchorLocation,
    var lastKnownPos: Vec3,
    var lastKnownDimension: ResourceKey<Level>
) {

    fun resolveDimension(server: MinecraftServer): ResourceKey<Level>? {
        val currentLocation = location

        return when (currentLocation) {
            is AnchorLocation.AsBlock -> currentLocation.dimension
            is AnchorLocation.InEntity -> resolveEntity(server)?.level()?.dimension()
        }
    }

    fun resolveBlockPos(server: MinecraftServer): BlockPos? {
        val currentLocation = location   // <-- snapshot

        return when (currentLocation) {
            is AnchorLocation.AsBlock -> currentLocation.pos

            is AnchorLocation.InEntity -> {
                val entity = resolveEntity(server)
                entity?.blockPosition()
            }
        }
    }

    fun resolveEntity(server: MinecraftServer): Entity? {
        val currentLocation = location

        if (currentLocation !is AnchorLocation.InEntity)
            return null

        val uuid = currentLocation.entityUUID

        for (level in server.allLevels) {
            val entity = level.getEntity(uuid)
            if (entity != null) return entity
        }

        return null
    }
}