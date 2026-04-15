package hauveli.hexagony.common.mind_anchor

import at.petrak.hexcasting.api.misc.MediaConstants
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor
import hauveli.hexagony.common.blocks.BlockFullMindAnchor
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.common.control.placeholderUUID
import hauveli.hexagony.common.craft.GraphCraftingRecipes
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.networking.msg.MsgMindAnchorPositionS2C
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.Vec3
import java.sql.DataTruncation
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

    private fun fuckingExplodeAndDie(serverPlayer: ServerPlayer) {
        val server = serverPlayer.server
        val uuid = serverPlayer.uuid
        val pos = getPosition(serverPlayer) ?: serverPlayer.position()
        val blockPos = BlockPos(pos.x.toInt(), pos.y.toInt(), pos.z.toInt())
        val pe = PlayerControlData.get(server).getOrCreate(uuid)
        val me = MindAnchorData.get(server).getOrCreate(uuid)
        val level = server.getLevel(me.dimension)
        val blockAtPos = level?.getBlockEntity(blockPos)
        if (blockAtPos is BlockEntityFullMindAnchor) {
            println("Block rebound")
            level.removeBlockEntity(blockPos)
            blockAtPos.setRemoved()
            level.explode(
                serverPlayer,
                pos.x,
                pos.y,
                pos.z,
                3f,
                Level.ExplosionInteraction.BLOCK
            )
        } else if (!pe.isDetached) {
            println("Player rebound")
            serverPlayer.level().explode(
                serverPlayer,
                pos.x,
                pos.y,
                pos.z,
                3f,
                Level.ExplosionInteraction.BLOCK
            )
        }
        MindAnchorData.get(server).anchors.remove(uuid)
        pe.graftUUID = placeholderUUID
    }

    val warningEffect = MobEffectInstance(
        MobEffects.BLINDNESS,
        2, // in ticks
        1,
        false,
        false,
        false
    );

    // Variable for iff mind anchor can not be found, but media must be subtracted?
    // only tick anchors of players who are connected to the server btw
    fun onTick(server: MinecraftServer, serverPlayer: ServerPlayer) {
        val uuid = serverPlayer.uuid
        val me = MindAnchorData.get(server).getOrCreate(uuid)
        val pe = PlayerControlData.get(server).getOrCreate(uuid)
        if (me.graftUUID == pe.graftUUID) {
            subtractMedia(serverPlayer, MediaConstants.DUST_UNIT)
            if (me.media == 0L) { // -1 is infinite
                // fucking explode
                println("KABOOM!!!")
                fuckingExplodeAndDie(serverPlayer)
            } else if (me.media <= MediaConstants.QUENCHED_SHARD_UNIT) {
                println("Erm below safety threshold for media...")
                pe.detach(serverPlayer)
            } else if (me.media <= MediaConstants.QUENCHED_BLOCK_UNIT) {
                // TODO: try to indicate to the player that they are about to die
                val proportion = MediaConstants.QUENCHED_SHARD_UNIT.div(me.media) // Greater than or equal to 1
                serverPlayer.addEffect(
                    warningEffect
                )
            } // Do nothing otherwise

        } else {
            // Stop tracking it internally, I don't know all the ways this state would be reached
            // but it likely can be reached somehow, definitely with creative
            MindAnchorData.get(server).anchors.remove(uuid)
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

        LifecycleEvent.SERVER_STARTED.register {
            server ->
            removeLooseHanging(server)
        }
    }

    fun removeLooseHanging(server: MinecraftServer) {
        val md = MindAnchorData.get(server)
        for (player in PlayerControlData.get(server).players) {
            val uuid: UUID = player.component1()
            if (md.anchors[uuid]?.graftUUID != player.component2().graftUUID ) {
                println("Hexagony: Cleaning up lost reference to mind anchor for player: ${uuid}")
                md.anchors.remove(uuid)
                md.setDirty() // I still dont udnerstand when I want to do this and when not
            }
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
        var cost = toSubtract
        if (PlayerControlData.get(serverPlayer.server).getOrCreate(serverPlayer.uuid).isDetached) {
            cost = toSubtract / MediaConstants.DUST_UNIT // cost less if detached
        }

        if (be != null) {
            println("Trying blockentity!!!")
            (be as BlockEntityFullMindAnchor)
            be.media = (be.media - cost).coerceAtLeast(0)

        } else if (ie != null) {
            println("trying itementity!!!")
            val itemStack = ie.item
            val beTag = itemStack?.tag?.getCompound("BlockEntityTag")
            val media = beTag?.getLong("media")
            if (media != null) {
                beTag.putLong("media", (media - cost).coerceAtLeast(0))
            }

        } else if (it != null) {
            println("trying itemstack....")
            val beTag = it.tag?.getCompound("BlockEntityTag")
            val media = beTag?.getLong("media")
            if (media != null) {
                beTag.putLong("media", (media - cost).coerceAtLeast(0))
            }
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