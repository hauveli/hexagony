package hauveli.hexagony.common.control

import hauveli.hexagony.common.homunculus.FakeServerPlayer.Companion.respawnFakeClone
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.networking.msg.MsgPlayerControlBooleanC2S
import hauveli.hexagony.networking.msg.MsgPlayerControlBooleanS2C
import hauveli.hexagony.networking.msg.MsgPlayerControlFloatS2C
import hauveli.hexagony.networking.msg.MsgPlayerControlIntegerS2C
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID


import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.phys.Vec3

val placeholderUUID: UUID = UUID.fromString("2cf1d3b8-3230-4a8c-80ee-5d34c508819b")

data class PlayerControlEntry (
    val mindUUID: UUID, // lol
    var ownerUUID: UUID = placeholderUUID, // placeholder, equals mindUUID if self, this field is technically unneeded, but here for ease
    var graftUUID: UUID = placeholderUUID,
    var isFakePlayer: Boolean = false, // default to false
    var isDetached: Boolean = false,
    var durationSeconds: Long = -1L,

    var shouldMoveForwardBackward: Float = 0f, // ws
    var shouldMoveLeftRight: Float = 0f, // ad
    var shouldLookUpDown: Float = 0f, // pitch
    var shouldLookLeftRight: Float = 0f, // yaw
    var shouldLookRoll: Float = 0f, // roll if I can find a use?
    var shouldLook: Boolean = false, // to force look
    var shouldJump: Boolean = false, // space
    var shouldSprint: Boolean = false, // ctrl
    var shouldSneak: Boolean = false, // shift

    var shouldAttack: Boolean = false, // lmb
    var shouldAttackPeriod: Int = 0,

    var shouldUse: Boolean = false, // rmb
    var shouldUsePeriod: Int = 0,

    var shouldSwapHands: Boolean = false,
    var shouldHotbarSlot: Int = 0,

    var shouldDrop: Boolean = false,
    var shouldDropStack: Boolean = false
) {

    // not persistent, but should it be? I feel like this will auto-resolve on world-reload and is a very fringe edge-case if it causes a problem...
    var using = false
    var breaking = false
    var blockDestroyStage = 0
    var lastInteractingWithBlockPos: BlockPos? = null

    // TODO: Ok so I tried and then I got somewhere and then I did git stash and lost track of it so whatever I'm just gonna make this work as it is
    fun stop(serverPlayer: ServerPlayer) {
        // TODO:
        // I've considered calling removeEntry() here, but I think a better solution
        // would be to populate the list of things to run with LifecycleEvent.player_join and remove with player_quit
        // because the homunculi DIE when the media runs out, this should in general eventually have
        // the homunculi run out of power and despawn
        if (serverPlayer.tags.contains("FakePlayer")) {
            isFakePlayer = true
            isDetached = false
            shouldMoveForwardBackward = 0f
            shouldMoveLeftRight = 0f
            shouldLookUpDown = 0f
            shouldLookLeftRight = 0f
            shouldLookRoll = 0f
            shouldLook = false
            shouldJump = false
            shouldSprint = false
            shouldSneak = false
            shouldAttack = false
            shouldAttackPeriod = 0
            shouldUse = false
            shouldUsePeriod = 0
            shouldSwapHands = false
            shouldHotbarSlot = -1
            shouldDrop = false
            shouldDropStack = false
        } else {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_STOP,
                    true
                )
            )
        }
    }

    fun duration(duration: Long) {
        durationSeconds = duration
    }

    fun requestData() {
        HexagonyNetworking.CHANNEL.sendToServer(
            MsgPlayerControlBooleanC2S(
                PlayerControlData.MessageTypeSimple.DATA_REQUEST,
                true
            )
        )
    }

    fun detach(serverPlayer: ServerPlayer) {
        isDetached = true
        println("OK checking thing!!!")
        if (serverPlayer.tags.contains("FakePlayer")) {
            println("Killing fakeplayer")
            serverPlayer.hurt(serverPlayer.damageSources().genericKill(),
                (serverPlayer.maxHealth+serverPlayer.absorptionAmount) * 2)
            serverPlayer.hurtMarked = true
        } else {
            println("About to send message!!!")
            println(isDetached)
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(PlayerControlData.MessageTypeBoolean.SHOULD_DETACH, true)
            )
        }
    }

    fun reattach(serverPlayer: ServerPlayer) {
        isDetached = false
        if (serverPlayer.tags.contains("FakePlayer")) {
            // What would it even mean to "re-attach" a FakePlayer?
            // It would mean we move the FakePlayer to the player's position, and the player to the FakePlayer's position...
            // TODO: swapperoo
        } else {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(PlayerControlData.MessageTypeBoolean.SHOULD_REATTACH, true)
            )
        }
    }

    fun jump(serverPlayer: ServerPlayer, jump: Boolean) {
        println("Set jump: ${jump}")
        shouldJump = jump
        if (!serverPlayer.tags.contains("FakePlayer")) {
            println("Sending jump message to client!")
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_JUMP,
                    jump
                )
            )
        }
    }

    fun sprint(serverPlayer: ServerPlayer,sprint: Boolean) {
        shouldSprint = sprint
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_SPRINT,
                    shouldSprint
                )
            )
        }
    }

    fun sneak(serverPlayer: ServerPlayer,sneak: Boolean) {
        shouldSneak = sneak
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_SNEAK,
                    shouldSneak
                )
            )
        }
    }

    fun attackOnce(serverPlayer: ServerPlayer) {
        shouldAttack = true
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_ATTACK,
                    shouldAttack
                )
            )
        }
    }

    fun attackPeriodic(serverPlayer: ServerPlayer, period: Int) {
        this.attackOnce(serverPlayer)
        shouldAttackPeriod = period
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlIntegerS2C(
                    PlayerControlData.MessageTypeInt.SHOULD_ATTACK_PERIOD,
                    shouldAttackPeriod
                )
            )
        }
    }

    fun attackContinuous(serverPlayer: ServerPlayer) {
        this.attackPeriodic(serverPlayer, 1)
    }

    fun useOnce(serverPlayer: ServerPlayer) {
        shouldUse = true
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_USE,
                    shouldUse
                )
            )
        }
    }

    fun usePeriodic(serverPlayer: ServerPlayer, period: Int) {
        this.useOnce(serverPlayer)
        shouldUsePeriod = period
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlIntegerS2C(
                    PlayerControlData.MessageTypeInt.SHOULD_USE_PERIOD,
                    shouldUsePeriod
                )
            )
        }
    }

    fun useContinuous(serverPlayer: ServerPlayer) {
        this.usePeriodic(serverPlayer, 1)
    }



    // can implement this, but realistically, if shouldUse or shouldAttack are bound, we have hands, and should be allowed to do this
    // only needed if shouldAttack or shouldUse are NOT bound.
    fun hotbar(serverPlayer: ServerPlayer,number: Int) {
        shouldHotbarSlot = number
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlIntegerS2C(
                    PlayerControlData.MessageTypeInt.SHOULD_HOTBAR_SLOT,
                    shouldHotbarSlot
                )
            )
        }
    }

    fun swapHands(serverPlayer: ServerPlayer) {
        shouldSwapHands = true
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_SWAP_HANDS,
                    shouldSwapHands
                )
            )
        }
    }

    fun moveForwardBackward(serverPlayer: ServerPlayer, walking: Float) {
        shouldMoveForwardBackward = walking
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlFloatS2C(
                    PlayerControlData.MessageTypeFloat.SHOULD_MOVE_FORWARD_BACKWARD,
                    walking
                )
            )
        }
    }

    fun moveLeftRight(serverPlayer: ServerPlayer, walking: Float) {
        shouldMoveLeftRight = walking
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlFloatS2C(
                    PlayerControlData.MessageTypeFloat.SHOULD_MOVE_LEFT_RIGHT,
                    walking
                )
            )
        }
    }

    fun lookForced(serverPlayer: ServerPlayer) {
        shouldLook = true
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_LOOK,
                    shouldLook
                )
            )
        }
    }

    // TODO: pitch yaw roll independently
    fun look(serverPlayer: ServerPlayer, pitch: Float, yaw: Float) {
        this.lookForced(serverPlayer)
        shouldLookUpDown = pitch
        shouldLookLeftRight = yaw
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlFloatS2C(
                    PlayerControlData.MessageTypeFloat.SHOULD_LOOK_UP_DOWN,
                    pitch
                )
            )
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlFloatS2C(
                    PlayerControlData.MessageTypeFloat.SHOULD_LOOK_LEFT_RIGHT,
                    yaw
                )
            )
        }
    }

    fun dropStack(serverPlayer: ServerPlayer) {
        shouldDropStack = true
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_DROP_STACK,
                    shouldDropStack
                )
            )
        }
    }

    fun drop(serverPlayer: ServerPlayer, entireStack: Boolean) {
        shouldDrop = true
        if (entireStack)
            this.dropStack(serverPlayer) // Todo: actually just call it in the other order...
        if (!serverPlayer.tags.contains("FakePlayer")) {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_DROP,
                    shouldDrop
                )
            )
        }
    }

    fun setFake(bool: Boolean) {
        isFakePlayer = bool // never matters on client
    }

    fun setOwner(uuid: UUID) {
        ownerUUID = uuid // never matters on client
    }

    fun setGraft(uuid: UUID) {
        graftUUID = uuid // never matters on client
    }
}

class PlayerControlData : SavedData() {
    // TODO: all prefixed with should, I should remove that...
    enum class MessageTypeSimple {
        DATA_REQUEST
    }

    enum class MessageTypeBoolean {
        SHOULD_DETACH,
        SHOULD_REATTACH,
        SHOULD_STOP,
        SHOULD_JUMP,
        SHOULD_SPRINT,
        SHOULD_SNEAK,
        SHOULD_LOOK,
        SHOULD_ATTACK,
        SHOULD_USE,
        SHOULD_SWAP_HANDS,
        SHOULD_DROP,
        SHOULD_DROP_STACK
    }

    enum class MessageTypeFloat {
        SHOULD_MOVE_FORWARD_BACKWARD,
        SHOULD_MOVE_LEFT_RIGHT,
        SHOULD_LOOK_UP_DOWN,
        SHOULD_LOOK_LEFT_RIGHT,
        SHOULD_LOOK_ROLL
    }

    enum class MessageTypeInt {
        SHOULD_ATTACK_PERIOD,
        SHOULD_USE_PERIOD,
        SHOULD_HOTBAR_SLOT
    }

    val players: MutableMap<UUID, PlayerControlEntry> = mutableMapOf()

    fun getOrCreate(uuid: UUID): PlayerControlEntry {
        return players.getOrPut(uuid) {
            PlayerControlEntry(
                uuid
            )
        }
    }

    override fun save(tag: CompoundTag): CompoundTag {
        val list = ListTag()

        players.values.forEach { entry ->
            val e = CompoundTag()

            // I'm too nooby at this time to know enough kotlin to NOT hardcode this but this would be really easy
            // if I knew how I'm sure, just k,v loop
            e.putUUID("mindUUID", entry.mindUUID)
            e.putUUID("ownerUUID", entry.ownerUUID)
            e.putUUID("graftUUID", entry.graftUUID)
            e.putBoolean("isFakePlayer", entry.isFakePlayer)
            e.putBoolean("isDetached", entry.isDetached)
            e.putLong("durationSeconds", entry.durationSeconds)

            e.putFloat("shouldMoveForwardBackward", entry.shouldMoveForwardBackward)
            e.putFloat("shouldMoveLeftRight", entry.shouldMoveLeftRight)
            e.putFloat("shouldLookUpDown", entry.shouldLookUpDown)
            e.putFloat("shouldLookLeftRight", entry.shouldLookLeftRight)
            e.putFloat("shouldLookRoll", entry.shouldLookRoll)

            e.putBoolean("shouldLook", entry.shouldLook)
            e.putBoolean("shouldJump", entry.shouldJump)
            e.putBoolean("shouldSprint", entry.shouldSprint)
            e.putBoolean("shouldSneak", entry.shouldSneak)

            e.putBoolean("shouldAttack", entry.shouldAttack)
            e.putInt("shouldAttackPeriod", entry.shouldAttackPeriod)

            e.putBoolean("shouldUse", entry.shouldUse)
            e.putInt("shouldUsePeriod", entry.shouldUsePeriod)

            e.putBoolean("shouldSwapHands", entry.shouldSwapHands)

            e.putInt("shouldHotbarSlot", entry.shouldHotbarSlot)

            e.putBoolean("shouldDrop", entry.shouldDrop)
            e.putBoolean("shouldDropStack", entry.shouldDropStack)

            list.add(e)
        }

        tag.put("PlayerControls", list)
        return tag
    }

    companion object {

        // Just putting in dummy data for the local entry at the start
        var myEntry = PlayerControlEntry(
            mindUUID = placeholderUUID, // not used when local player.
            ownerUUID = placeholderUUID,
            graftUUID = placeholderUUID,
            isFakePlayer = false,
            isDetached = false,
            durationSeconds = -1L,
            shouldMoveForwardBackward = 0f,
            shouldMoveLeftRight = 0f,
            shouldLookUpDown = 0f,
            shouldLookLeftRight = 0f,
            shouldLookRoll = 0f,
            shouldLook = false,
            shouldJump = false,
            shouldSprint = false,
            shouldSneak = false,
            shouldAttack = false,
            shouldAttackPeriod = 0,
            shouldUse = false,
            shouldUsePeriod = 0,
            shouldSwapHands = false,
            shouldHotbarSlot = -1,
            shouldDrop = false,
            shouldDropStack = false
        )

        fun onJoinClient() {
            getSelf().requestData()
        }

        fun getSelf() : PlayerControlEntry {
            return myEntry
        }

        private const val NAME = "hexagony_player_controls"

        fun get(server: MinecraftServer): PlayerControlData {
            // Relying on overworld() seems bad but whatever
            return server.overworld().dataStorage.computeIfAbsent(
                ::load,
                ::PlayerControlData,
                NAME
            )
        }

        fun removeEntry(uuid: UUID, server: MinecraftServer) {
            val data = get(server)
            data.players.remove(uuid)
            data.setDirty()
        }

        private fun load(tag: CompoundTag): PlayerControlData {
            val data = PlayerControlData()
            val list = tag.getList("PlayerControls", Tag.TAG_COMPOUND.toInt())

            for (i in 0 until list.size) {
                val e = list.getCompound(i)

                val uuid = e.getUUID("mindUUID")
                val ownerUuid = e.getUUID("ownerUUID")
                val isFakePlayer = e.getBoolean("isFakePlayer")
                // Hmmm... I'm not sure how I'd like to handle non-logged in players....

                // todo: is the stored data in bytes? is that why it can't just unpack it in a smart automatic way?
                val playerEntry =
                    PlayerControlEntry(
                        uuid,
                        e.getUUID("ownerUUID"),
                        e.getUUID("graftUUID"),
                        e.getBoolean("isFakePlayer"),
                        e.getBoolean("isDetached"),
                        e.getLong("durationSeconds"),
                        e.getFloat("shouldMoveForwardBackward"),
                        e.getFloat("shouldMoveLeftRight"),
                        e.getFloat("shouldLookUpDown"),
                        e.getFloat("shouldLookLeftRight"),
                        e.getFloat("shouldLookRoll"),
                        e.getBoolean("shouldLook"),
                        e.getBoolean("shouldJump"),
                        e.getBoolean("shouldSprint"),
                        e.getBoolean("shouldSneak"),
                        e.getBoolean("shouldAttack"),
                        e.getInt("shouldAttackPeriod"),
                        e.getBoolean("shouldUse"),
                        e.getInt("shouldUsePeriod"),
                        e.getBoolean("shouldSwapHands"),
                        e.getInt("shouldHotbarSlot"),
                        e.getBoolean("shouldDrop"),
                        e.getBoolean("shouldDropStack"),
                    )
                data.players[uuid] = playerEntry
            }

            return data
        }

        fun init(server: MinecraftServer) {
            println("Registering event...")
            val data = this.get(server)
            for (pairData in data.players) {
                val playerDataEntry = pairData.component2()
                if (playerDataEntry.isFakePlayer) {
                    println("before place player")
                    val uuid = pairData.component1()
                    placeSavedPlayerInWorld(server, uuid)
                }
            }
        }

        fun placeSavedPlayerInWorld(server: MinecraftServer, uuid: UUID) {
            println("Before profile")
            val profile = server.profileCache?.get(uuid)?.get() ?: return
            println("Before tempplayer")
            val tempPlayer = ServerPlayer(server, server.overworld(), profile)
            println("Before serverLevel")
            val level = tempPlayer.serverLevel()
            val playerData = server.playerList.load(tempPlayer)
            tempPlayer.disconnect()
            tempPlayer.discard()
            println("past player")

            val pos = playerData?.getList("Pos", 6) ?: return
            val x = pos.getDouble(0)
            val y = pos.getDouble(1)
            val z = pos.getDouble(2)
            val fake = respawnFakeClone(server, level, Vec3(x, y, z), uuid)
            // fake.addTag()
            tempPlayer.discard()
            println("Respawned fake clone: ${fake}")
        }

        // TODO: this sucks, do something better, only doing this because I want it working (at all) so I can playtest
        fun onJoinServer(serverPlayer: ServerPlayer) {
            val server = serverPlayer.server

            val playerData = get(server).getOrCreate(serverPlayer.uuid)
            println("Sending control data to player")
            // god I should have thought about this before I started writing it
            // I will have to rewrite this to be nicer later on... I would prefer to just call .update() or .refresh()

            // duration is tracked by server...
            // playerData.duration(playerData.durationSeconds)
            println(playerData.isDetached)
            if (playerData.isDetached) {
                playerData.detach(serverPlayer)
            }

            if (playerData.shouldUse) {
                playerData.usePeriodic(serverPlayer, playerData.shouldUsePeriod)
            }
            if (playerData.shouldAttack) {
                playerData.usePeriodic(serverPlayer, playerData.shouldAttackPeriod)
            }

            if (playerData.shouldJump) {
                playerData.jump(serverPlayer, playerData.shouldJump)
            }
            if (playerData.shouldSprint) {
                playerData.sprint(serverPlayer, playerData.shouldSprint)
            }
            if (playerData.shouldSneak) {
                playerData.sneak(serverPlayer, playerData.shouldSneak)
            }

            if (playerData.shouldMoveForwardBackward != 0f) {
                playerData.moveForwardBackward(serverPlayer, playerData.shouldMoveForwardBackward)
            }
            if (playerData.shouldMoveLeftRight != 0f) {
                playerData.moveLeftRight(serverPlayer, playerData.shouldMoveLeftRight)
            }

            if (playerData.shouldLook) {
                playerData.look(serverPlayer, playerData.shouldLookUpDown, playerData.shouldLookLeftRight)
            }

            if (playerData.shouldDrop) {
                playerData.drop(serverPlayer, playerData.shouldDropStack)
            }

            if (playerData.shouldHotbarSlot != -1) {
                playerData.hotbar(serverPlayer, playerData.shouldHotbarSlot)
            }

            if (playerData.shouldSwapHands) {
                playerData.swapHands(serverPlayer)
            }
        }
    }
}