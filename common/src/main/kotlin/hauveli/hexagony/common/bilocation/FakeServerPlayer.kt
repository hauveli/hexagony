package hauveli.hexagony.common.bilocation

import com.mojang.authlib.GameProfile
import hauveli.hexagony.common.control.PlayerControlData
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import java.util.*

class FakeServerPlayer(
    server: MinecraftServer,
    level: ServerLevel,
    original: ServerPlayer,
    uuid: UUID,
    pos: Vec3
) : ServerPlayer(
    server,
    level,
    GameProfile(uuid, "Homunculus")
    ) {
    init {
        moveTo(pos.x, pos.y, pos.z, 0f, 0f)

        /*
        println(original.scoreboardName) // Player###
        println(original.name.toString()) // literal{Player###}
        println(original.customName.toString()) // null
         */

        // this shadows the items?
        for (i in 0 until original.inventory.containerSize) {
            val stack: ItemStack = original.inventory.getItem(i).copy()
            inventory.setItem(i, stack)
        }

        for (slot in EquipmentSlot.entries) {
            val stack = original.getItemBySlot(slot).copy()
            setItemSlot(slot, stack)
        }

        experienceLevel = original.experienceLevel
        experienceProgress = original.experienceProgress
        health = original.health
        foodData.foodLevel = original.foodData.foodLevel
        foodData.setSaturation(original.foodData.saturationLevel)
        foodData.setExhaustion(original.foodData.exhaustionLevel)

        isInvisible = false
        isCustomNameVisible = false
        customName = Component.literal("Homunculus")
        hidePlayerName()

        invulnerableTime = 0
        abilities.invulnerable = false
        isInvulnerable = false
        hurtMarked = false
        remainingFireTicks = 0


        tags.add("FakePlayer") // I don't know if there's a better way to get whether a ServerPlayer is fake or not
        // I could try to check the reply from a packet but that seems like way more effort than this, even if it would be better...
    }

    override fun isInvulnerable(): Boolean {
        return false
    }

    override fun isInvulnerableTo(source: DamageSource): Boolean {
        return false
    }

    override fun isSpectator(): Boolean {
        return false
    }

    override fun isCreative(): Boolean {
        return false
    }
    override fun isPushable(): Boolean = true
    // override fun canBeCollidedWith(): Boolean = true

    override fun jumpFromGround() {
        super.jumpFromGround()
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        return super.hurt(source, amount)
    }

    var counter = 0
    override fun tick() {
        super.tick()
        // move() forces it to update. I have no idea how to do this in a better way.
        // figuring this out was rough.
        move(MoverType.SELF, Vec3.ZERO)
        if (counter == 1000) {
            counter = 0
            println("TICKING: ${this.stringUUID}")
        }
        counter++

        // Even have to do gravity because this stupid thing won't even fall without it............
        // gravity
        if (!onGround()) {
            deltaMovement = deltaMovement.add(0.0, -0.08, 0.0)
        }
        //if (this.)

        if (deltaMovement.lengthSqr() > 0.0001) {
            move(MoverType.SELF, deltaMovement)
            hasImpulse = false
        }

        val friction = if (onGround()) 0.6 else 0.91
        deltaMovement = deltaMovement.scale(friction)

        if (this.health <= 0) {
            deathTime++
            if (deathTime >= 20) {
                this.kill()
                this.discard()
            }
        }
    }

    override fun baseTick() {
        super.baseTick()
    }

    override fun isControlledByLocalInstance(): Boolean {
        return true
    }

    fun hidePlayerName() {
        val ser = this.server ?: return
        val scoreboard: Scoreboard = ser.scoreboard

        val teamName = "hexagony:invisible_name"
        val team: PlayerTeam = scoreboard.getPlayerTeam(teamName) ?: scoreboard.addPlayerTeam(teamName)

        team.nameTagVisibility = Team.Visibility.NEVER

        scoreboard.addPlayerToTeam(this.gameProfile.name, team)
    }

    fun stopTracking() {
        val ser = server ?: return
        PlayerControlData.removeEntry(uuid, ser) // This is important, more than actually dying important.
    }

    // I do this just in case it's called from a bad place
    override fun die(source: DamageSource) {
        stopTracking()
        super.die(source)
    }

    override fun tickDeath() {
        stopTracking()
        super.tickDeath()
    }

    override fun kill() {
        stopTracking()
        super.kill()
    }

    override fun knockback(strength: Double, x: Double, z: Double) {
        // super.knockback(strength, x, z)
        hasImpulse = true
        val dx = x
        val dz = z
        val scale = Math.sqrt(dx * dx + dz * dz)

        if (scale > 0.0) {
            val motionX = dx / scale * strength
            val motionZ = dz / scale * strength

            deltaMovement = deltaMovement.add(-motionX, 0.4, -motionZ)

            hasImpulse = true
        }
    }

    companion object {
        fun spawnFakeClone(original: ServerPlayer, pos: Vec3, uuid: UUID): FakeServerPlayer {
            val server = original.server ?: throw IllegalStateException("Server null")
            val level = original.level() as ServerLevel

            val clone = FakeServerPlayer(server, level, original, uuid, pos)

            val connection = DummyConnection(PacketFlow.SERVERBOUND)
            val listener = DummyServerGamePacketListenerImpl(server, connection,clone)
            connection.setListener(listener)

            val packet = ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                clone
            )

            server.playerList.broadcastAll(packet)
            // server.executeIfPossible {  }

            server.playerList.players.add(clone)
            level.addNewPlayer(clone)

            /*
            val field = listener.javaClass.getDeclaredField("awaitingPositionFromClient")
            field.isAccessible = true
            field.set(listener, null)
            */

            /*
            server.playerList.placeNewPlayer(
                connection,
                clone
            )
             */

            return clone
        }
    }
}