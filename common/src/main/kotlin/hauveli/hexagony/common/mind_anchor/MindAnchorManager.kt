package hauveli.hexagony.common.mind_anchor

import at.petrak.hexcasting.api.misc.MediaConstants
import dev.architectury.event.events.common.PlayerEvent
import hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor
import hauveli.hexagony.common.blocks.BlockFullMindAnchor
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.networking.msg.MsgMindAnchorPositionS2C
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object MindAnchorManager {

    enum class MessageTypesVec {
        POSITION
    }

    enum class MessageTypesFloat {
        MEDIA
    }

    // /give @p hexcasting:battery{"hexcasting:start_media":810200L, "hexcasting:media":810200L}

    private val MAX_CAPACITY = 9_000_000_000_000_000_000L

    var localPos: Vec3? = null
    @JvmStatic
    var localMedia: Float? = null

    private val runtime = ConcurrentHashMap<UUID, MindAnchorRuntime>()

    private fun runtime(uuid: UUID) =
        runtime.computeIfAbsent(uuid) { MindAnchorRuntime() }

    fun setGraft(server: MinecraftServer, uuid: UUID, graftUUID: UUID) {
        val anchor = MindAnchorData.get(server).anchors[uuid] ?: return
        anchor.graftUUID = graftUUID
    }

    // Variable for iff mind anchor can not be found, but media must be subtracted?
    fun onTick(server: MinecraftServer, player: ServerPlayer) {
        val uuid = player.uuid
        val MA = MindAnchorData.get(server).getOrCreate(uuid)
        val gU = PlayerControlData.get(server).getOrCreate(uuid).graftUUID
        if (MA.graftUUID == gU) {
            MA.media -= MediaConstants.DUST_UNIT
            if (MA.media <= 0) {
                // fucking explode
                println("KABOOM!!!")
                val pos = getPosition(player) ?: player.position()
                player.level().explode(
                    player,
                    pos.x,
                    pos.y,
                    pos.z,
                    3f,
                    Level.ExplosionInteraction.BLOCK
                )
                MindAnchorData.get(server).anchors.remove(uuid)

            }

        } else {

        }
    }

    fun trackBlock(
        server: MinecraftServer,
        mindUUID: UUID,
        blockEntity: BlockEntity
    ) {
        val data = MindAnchorData.Companion.get(server)
        val entry = data.getOrCreate(mindUUID)

        entry.type = AnchorType.BLOCK_ENTITY
        entry.activeUUID = null
        entry.dimension = blockEntity.level!!.dimension()
        entry.pos = blockEntity.blockPos

        // get media here, hmm...
        val be = blockEntity as BlockEntityFullMindAnchor
        entry.media = (MAX_CAPACITY - be.remainingMediaCapacity()).coerceAtLeast(0L)
        runtime(mindUUID).trackBlock(blockEntity)

        data.setDirty()

        forwardToUuid(entry.mindUUID, server, entry.pos.center)
    }

    fun trackItemEntity(
        server: MinecraftServer,
        mindUUID: UUID,
        entity: ItemEntity
    ) {
        val data = MindAnchorData.Companion.get(server)
        val entry = data.getOrCreate(mindUUID)

        entry.type = AnchorType.ITEM_ENTITY
        entry.activeUUID = entity.uuid
        entry.dimension = entity.level().dimension()
        entry.pos = entity.blockPosition()

        runtime(mindUUID).trackItemEntity(entity)

        // hmmm...
        val com = entity.item.tag?.getCompound("BlockEntityTag")
        val media = com?.getLong("media")
        if (media != null) {
            entry.media = media
        }

        data.setDirty()

        forwardToUuid(entry.mindUUID, server, entity.position())
    }

    fun trackItemStack(
        server: MinecraftServer,
        mindUUID: UUID,
        holder: Entity,
        itemStack: ItemStack
    ) {
        val data = MindAnchorData.Companion.get(server)
        val entry = data.getOrCreate(mindUUID)

        entry.type = AnchorType.ITEM_STACK
        entry.activeUUID = holder.uuid
        entry.dimension = holder.level().dimension()
        entry.pos = holder.blockPosition()

        // oof... can't do both... hmm...
        runtime(mindUUID).trackItemStackAndEntity(itemStack, holder)

        // Hmm... could just steal media from the person holding it hehe...
        val com = itemStack.tag?.getCompound("BlockEntityTag")
        val media = com?.getLong("media")
        if (media != null) {
            entry.media = media
        }

        data.setDirty()

        forwardToUuid(entry.mindUUID, server, holder.position())
    }

    fun forwardToUuid(uuid: UUID, server: MinecraftServer, vec: Vec3) {
        val player = server.playerList.getPlayer(uuid) ?: return
        forwardToPlayer(player, vec) // Update the player's info...
    }

    fun forwardToPlayer(serverPlayer: ServerPlayer, vec: Vec3) {
        HexagonyNetworking.CHANNEL.sendToPlayer(
            serverPlayer,
            MsgMindAnchorPositionS2C(
                MessageTypesVec.POSITION,
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
        val it = rt?.entity // because itemStack has no position
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

    fun getMedia(serverPlayer: ServerPlayer): Long? {
        val rt = runtime[serverPlayer.uuid]
        val be = rt?.blockEntity
        val ie = rt?.itemEntity
        val it = rt?.itemStack // because itemStack has no position
        println(rt)

        val media: Long?
        if (be != null) {
            (be as BlockEntityFullMindAnchor)
            // maybe I should get it via tag....
            (be as BlockEntityFullMindAnchor)
            media = be.updateTag.getLong("media")
        } else if (ie != null) {
            println("itemEntity")
            val itemStack = ie.item
            val beTag = itemStack?.tag?.getCompound("BlockEntityTag")
            println(beTag.toString())
            media = beTag?.getLong("media")
        } else if (it != null) {
            println("itemStack")
            val beTag = it.tag?.getCompound("BlockEntityTag")
            println(beTag.toString())
            media = beTag?.getLong("media")
        } else {
            media = null
        }
        println(media)
        return media // can be null...
    }

    fun subtractMedia(serverPlayer: ServerPlayer, toSubtract: Long) {
        println("Inside subtract! Right beofre return!")
        val rt = runtime[serverPlayer.uuid] ?: return
        println("made it past return!!!")
        val be = rt.blockEntity
        val ie = rt.itemEntity
        val it = rt.itemStack // because itemStack has no position

        if (be != null) {
            println("Trying blockentity!!!")
            (be as BlockEntityFullMindAnchor)
            val media = be.updateTag.getLong("media")
            be.updateTag.putLong("media", (media - toSubtract).coerceAtLeast(0))

        } else if (ie != null) {
            println("trying itementity!!!")
            val itemStack = ie.item
            val beTag = itemStack?.tag?.getCompound("BlockEntityTag")
            val media = beTag?.getLong("media")
            if (media != null)
                beTag.putLong("media", (media - toSubtract).coerceAtLeast(0))

        } else if (it != null) {
            println("trying itemstack....")
            val beTag = it.tag?.getCompound("BlockEntityTag")
            val media = beTag?.getLong("media")
            if (media != null)
                beTag.putLong("media", (media - toSubtract).coerceAtLeast(0))
        }
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