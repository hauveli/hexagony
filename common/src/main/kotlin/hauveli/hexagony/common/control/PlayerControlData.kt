package hauveli.hexagony.common.control

import hauveli.hexagony.networking.HexagonyNetworking
import hauveli.hexagony.networking.msg.MsgPlayerControlBooleanS2C
import hauveli.hexagony.networking.msg.MsgPlayerControlFloatS2C
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import java.util.UUID


import net.minecraft.world.level.saveddata.SavedData

data class PlayerControlEntry (
    val mindUUID: UUID, // lol

    var shouldMoveForwardBackward: Float = 0f, // ws
    var shouldMoveLeftRight: Float = 0f, // ad
    var shouldLookUpDown: Float = 0f, // pitch
    var shouldLookLeftRight: Float = 0f, // yaw
    var shouldLookRoll: Float = 0f, // roll if I can find a use?
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

    fun jump(serverPlayer: ServerPlayer) {
        shouldJump = true // this SHOULD be reflected in the client? hmm... better to set it inside of handler?
        // FakePlayer must be tagged for this to work, I could not identify any sane way to mark them otherwise.
        if (serverPlayer.tags.contains("FakePlayer")) return
        HexagonyNetworking.CHANNEL.sendToPlayer(
            serverPlayer,
            MsgPlayerControlBooleanS2C(PlayerControlData.MessageTypeBoolean.SHOULD_JUMP, true)
        )
    }

    fun stop() {
        shouldJump = false
        shouldMoveForwardBackward = 0f
        shouldMoveLeftRight = 0f
    }

    fun forward() {
        shouldMoveForwardBackward = 1.0f
    }

    fun backward() {
        shouldMoveForwardBackward = -1.0f
    }

    fun left() {
        shouldMoveLeftRight = 1.0f
    }

    fun right() {
        shouldMoveLeftRight = -1.0f
    }

    fun sprint(sprint: Boolean) {
        shouldSprint = sprint
    }

    fun sneak(sneak: Boolean) {
        shouldSneak = sneak
    }

    fun attack() {
        shouldAttack = true
    }

    fun attackOnce() {
        shouldAttack = true
    }

    fun attackContinuous() {
        shouldAttack = true
        shouldAttackPeriod = 1
    }

    fun attackPeriodic(period: Int) {
        shouldAttack = true
        shouldAttackPeriod = period
    }

    fun use() {
        shouldUse = true
    }

    fun useOnce() {
        shouldUse = true
    }

    fun useContinuous() {
        shouldUse = true
        shouldUsePeriod = 1
    }

    fun usePeriodic(period: Int) {
        shouldUse = true
        shouldUsePeriod = period
    }


    // can implement this, but realistically, if shouldUse or shouldAttack are bound, we have hands, and should be allowed to do this
    // only needed if shouldAttack or shouldUse are NOT bound.
    fun hotbar(number: Int) {
        shouldHotbarSlot = number
    }

    fun swapHands() {
        shouldSwapHands = true
    }

    fun moveForwardBackward(serverPlayer: ServerPlayer, walking: Float) {
        shouldMoveForwardBackward = walking
        if (serverPlayer.tags.contains("FakePlayer")) return
        HexagonyNetworking.CHANNEL.sendToPlayer(
            serverPlayer,
            MsgPlayerControlFloatS2C(
                PlayerControlData.MessageTypeFloat.SHOULD_MOVE_FORWARD_BACKWARD,
                walking
            )
        )
    }

    fun moveLeftRight(walking: Float) {
        shouldMoveLeftRight = walking
    }

    // TODO: pitch yaw roll independently
    fun look(pitch: Float, yaw: Float) {
        shouldLookUpDown = pitch
        shouldLookLeftRight = yaw
    }

    // TODO: pitch yaw roll independently
    fun drop(entireStack: Boolean) {
        shouldDrop = true
        shouldDropStack = entireStack
    }
}

// Actually, it should all be stored so, none of it is runtime?
class PlayerControlRuntime () {

}

class PlayerControlData : SavedData() {
    enum class MessageTypeBoolean {
        SHOULD_JUMP,
        SHOULD_SPRINT,
        SHOULD_SNEAK,
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