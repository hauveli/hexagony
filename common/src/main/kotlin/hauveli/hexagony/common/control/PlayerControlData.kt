package hauveli.hexagony.common.control

import hauveli.hexagony.common.bilocation.FakeServerPlayer
import hauveli.hexagony.common.bilocation.FreeCameraEntity
import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.networking.msg.MsgPlayerControlBooleanS2C
import hauveli.hexagony.networking.msg.MsgPlayerControlFloatS2C
import hauveli.hexagony.networking.msg.MsgPlayerControlIntegerS2C
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import java.util.UUID


import net.minecraft.world.level.saveddata.SavedData

data class PlayerControlEntry (
    val mindUUID: UUID, // lol

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
    fun stop(serverPlayer: ServerPlayer) {
        if (serverPlayer.tags.contains("FakePlayer")) {
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

    fun detach(serverPlayer: ServerPlayer) {
        if (serverPlayer.tags.contains("FakePlayer")) {
            // What would it even mean to "detach" a FakePlayer?
        } else {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(PlayerControlData.MessageTypeBoolean.SHOULD_DETACH, true)
            )
        }
    }

    fun reattach(serverPlayer: ServerPlayer) {
        if (serverPlayer.tags.contains("FakePlayer")) {
            // What would it even mean to "re-attach" a FakePlayer?
        } else {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(PlayerControlData.MessageTypeBoolean.SHOULD_REATTACH, true)
            )
        }
    }

    fun jump(serverPlayer: ServerPlayer) {
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldJump = true
        } else {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(PlayerControlData.MessageTypeBoolean.SHOULD_JUMP, true)
            )
        }
    }

    fun sprint(serverPlayer: ServerPlayer,sprint: Boolean) {
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldSprint = sprint
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldSneak = sneak
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldAttack = true
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldAttackPeriod = period
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldUse = true
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldUsePeriod = period
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldHotbarSlot = number
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldSwapHands = true
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldMoveForwardBackward = walking
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldMoveLeftRight = walking
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldLook = true
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldLookUpDown = pitch
            shouldLookLeftRight = yaw
        } else {
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
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldDropStack = true
        } else {
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
        if (entireStack)
            this.dropStack(serverPlayer) // Todo: actually just call it in the other order...
        if (serverPlayer.tags.contains("FakePlayer")) {
            shouldDrop = true
        } else {
            HexagonyNetworking.CHANNEL.sendToPlayer(
                serverPlayer,
                MsgPlayerControlBooleanS2C(
                    PlayerControlData.MessageTypeBoolean.SHOULD_DROP,
                    shouldDrop
                )
            )
        }
    }
}

class PlayerControlData : SavedData() {
    // TODO: all prefixed with should, I should remove that...
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

        lateinit var myEntry: PlayerControlEntry

        fun onJoin() {
            val player = Minecraft.getInstance().player
            // whatever, I'll just have each client track their own personal myEntry and then have the server tell the clients what to do
            // on join or something...
            myEntry = PlayerControlEntry(
                mindUUID = player!!.uuid,
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

        fun removeEntry(uuid: UUID) {
            val data = PlayerControlData()
            val removed = data.players.remove(uuid)
            println("Removed: ${removed.toString()}")
            data.setDirty()
        }

        private fun load(tag: CompoundTag): PlayerControlData {
            val data = PlayerControlData()
            val list = tag.getList("PlayerControls", Tag.TAG_COMPOUND.toInt())

            for (i in 0 until list.size) {
                val e = list.getCompound(i)

                val uuid = e.getUUID("mindUUID")

                // todo: is the stored data in bytes? is that why it can't just unpack it in a smart automatic way?
                data.players[uuid] =
                    PlayerControlEntry(
                        uuid,
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
            }

            return data
        }
    }
}