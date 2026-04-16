package hauveli.hexagony.common.mind_anchor

import at.petrak.hexcasting.api.misc.MediaConstants
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import hauveli.hexagony.common.blocks.BlockEntityFullMindAnchor
import hauveli.hexagony.common.blocks.BlockFullMindAnchor
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.common.control.placeholderUUID
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.networking.msg.MsgMindAnchorPositionS2C
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
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

    private fun fuckingExplodeAndDie(serverPlayer: ServerPlayer) {
        val server = serverPlayer.server
        val uuid = serverPlayer.uuid
        val pe = PlayerControlData.get(server).getOrCreate(uuid)
        val me = MindAnchorData.get(server).getOrCreate(uuid)
        val level = server.getLevel(me.dimension)
        var pos: Vec3? = null
        val rt = runtime[uuid]
        if (rt != null) {
            if (rt.blockEntity != null) {
                println("exploding at blockEntity!!!")
                pos = rt.blockEntity!!.blockPos.center
                level?.removeBlockEntity(rt.blockEntity!!.blockPos)
                level?.removeBlock(rt.blockEntity!!.blockPos, false)
                level?.destroyBlock(rt.blockEntity!!.blockPos, false)
                rt.blockEntity!!.setRemoved()
            } else if (rt.itemEntity != null) {
                //rt.itemEntity!!.setRemoved(Entity.RemovalReason.DISCARDED)
                //rt.itemEntity!!.kill()
                println("exploding at itemEntity!!!")
                pos = rt.itemEntity!!.position()
                rt.itemEntity!!.discard()
            } else if (rt.itemStack != null) {
                if (rt.entity != null) {
                    println("exploding at holder!!!")
                    pos = rt.entity!!.position()
                }
                rt.itemStack!!.count-- // max stack size is 1 so this should work?
            } else if (rt.entity != null) {
                // erm... I'm not sure what I want to do here yet
                println("item was being held when meant to die...")
            } else {
                println("All runtime things were NULL! AHHH")
            }
        }
        if (pos != null) {
            println("exploding!!!")
            serverPlayer.level().explode(
                serverPlayer,
                pos.x,
                pos.y,
                pos.z,
                3f,
                Level.ExplosionInteraction.BLOCK
            )
        }
        if (!pe.isDetached) {
            println("trying to explode player")
            pos = serverPlayer.position()
            level?.explode(
                serverPlayer,
                pos.x,
                pos.y,
                pos.z,
                3f,
                Level.ExplosionInteraction.BLOCK
            )
        }
        println("releasing references")
        removeReference(server, uuid)
        pe.graftUUID = placeholderUUID
        pe.reattach(serverPlayer)
        // Todo: custom death message ala "dissipated into media"
        serverPlayer.die(serverPlayer.damageSources().genericKill())
    }

    val warningEffectWeak = MobEffectInstance(
        MobEffects.BLINDNESS,
        20, // in ticks
        1,
        false,
        false,
        false
    )
    val warningEffectMedium = MobEffectInstance(
        MobEffects.BLINDNESS,
        20, // in ticks
        2,
        false,
        false,
        false
    )
    val warningEffectStrong = MobEffectInstance(
        MobEffects.BLINDNESS,
        20, // in ticks
        3,
        false,
        false,
        false
    )
    val effectTiers = listOf(
        warningEffectStrong,
        warningEffectStrong,
        warningEffectMedium,
        warningEffectMedium,
        warningEffectWeak,
    )

    // Variable for iff mind anchor can not be found, but media must be subtracted?
    // only tick anchors of players who are connected to the server btw
    fun perSecond(server: MinecraftServer, serverPlayer: ServerPlayer) {
        val uuid = serverPlayer.uuid
        val me = MindAnchorData.get(server).getOrCreate(uuid)
        val currentMedia = getMedia(serverPlayer)
        val pe = PlayerControlData.get(server).getOrCreate(uuid)
        println("DOING MIND ANCHOR PER SECOND STUFF before graft id match")
        if (me.graftUUID == pe.graftUUID && currentMedia != null) {
            println("ok graft ids are matching")
            println("How much is media atm? ${me.media}") //todo: why is the effect ufcked
            println("how much current: ${currentMedia}")
            subtractMedia(serverPlayer, MediaConstants.CRYSTAL_UNIT)
            if (currentMedia == 0L) { // -1 is infinite
                // fucking explode
                println("KABOOM!!!")
                fuckingExplodeAndDie(serverPlayer)
            } else if (currentMedia <= MediaConstants.QUENCHED_SHARD_UNIT) {
                println("Erm below safety threshold for media...")
                if (!pe.isDetached)
                    pe.detach(serverPlayer)
            } else if (currentMedia <= MediaConstants.QUENCHED_BLOCK_UNIT) {
                // TODO: try to indicate to the player that they are about to die using SHADERS
                // it is already betwen 0 and 4 because of math, but I'm making it explicit so that
                // it is clear what is happening
                val proportion: Int = MediaConstants.QUENCHED_SHARD_UNIT.div(currentMedia).coerceIn(0,4).toInt()
                serverPlayer.addEffect(
                    effectTiers[proportion]
                )
            } // Do nothing otherwise

        } else if (currentMedia == 0L && lastSeenWayTooLongAgo(server, uuid)) {
            println("graft ids not matching, ITS FUCKED!!!!!")
            println("Or took too long!!! : ${lastSeenWayTooLongAgo(server, uuid)}")
            // Stop tracking it internally, I don't know all the ways this state would be reached
            // but it likely can be reached somehow, definitely with creative
            removeReference(server, uuid)
        }
        println("ended up? ${me.media}") //todo: why is the effect ufcked
        println("ended current: ${currentMedia}")
    }

    private const val TICK_THRESHOLD = 1000
    fun lastSeenWayTooLongAgo(server: MinecraftServer, uuid: UUID): Boolean {
        val rt = runtime[uuid]
        val lastSeen = rt?.lastSeenTick
        if (lastSeen != null) {
            return lastSeen + TICK_THRESHOLD > server.tickCount
        }
        return false // probably ok to do this?
    }

    fun removeReference(server: MinecraftServer, uuid: UUID) {
        MindAnchorData.get(server).anchors.remove(uuid)
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
                removeReference(server, uuid)
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
        if (pos != null)
            rt?.lastSeenTick = serverPlayer.server.tickCount
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
        if (media != null)
            rt?.lastSeenTick = serverPlayer.server.tickCount
        println(media)
        return media // can be null...
    }

    // Todo: check if a mind anchor exists tied to player before allowing detach

    fun subtractMedia(serverPlayer: ServerPlayer, toSubtract: Long) {
        println("Inside subtract! Right beofre return!")
        val rt = runtime[serverPlayer.uuid] ?: return
        val me = MindAnchorData.get(serverPlayer.server).getOrCreate(serverPlayer.uuid)
        println("made it past return!!!")
        val be = rt.blockEntity
        val ie = rt.itemEntity
        val it = rt.itemStack // because itemStack has no position
        var cost = toSubtract
        if (!PlayerControlData.get(serverPlayer.server).getOrCreate(serverPlayer.uuid).isDetached) {
            cost = toSubtract * 10 // cost more if not detached
        }
        println("cost is: ${cost}")

        if (be != null) {
            println("Trying blockentity!!!")
            (be as BlockEntityFullMindAnchor)
            be.media = (be.media - cost).coerceAtLeast(0)
            me.media = be.media

        } else if (ie != null) {
            println("trying itementity!!!")
            val itemStack = ie.item
            val beTag = itemStack?.tag?.getCompound("BlockEntityTag")
            val media = beTag?.getLong("media")
            if (media != null) {
                val remainingMedia = (media - cost).coerceAtLeast(0)
                beTag.putLong("media", remainingMedia)
                me.media = remainingMedia
            }

        } else if (it != null) {
            println("trying itemstack....")
            val beTag = it.tag?.getCompound("BlockEntityTag")
            val media = beTag?.getLong("media")
            if (media != null) {
                val remainingMedia = (media - cost).coerceAtLeast(0)
                beTag.putLong("media", remainingMedia)
                me.media = remainingMedia
            }
        }
    }

    fun getPowered(uuid: UUID): Boolean? {
        val rt = runtime[uuid]
        val be = rt?.blockEntity
        if (be != null) {
            val bs = be.blockState
            if (bs != null && bs.hasProperty(BlockFullMindAnchor.POWERED)) {
                rt.lastSeenTick = be.level?.server?.tickCount
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
                rt.lastSeenTick = be.level?.server?.tickCount
                return level.getDirectSignalTo(be.blockPos).toDouble()// I
            }
        }
        // I really hope this isn't reached in normal circumstances...
        return null
    }
}