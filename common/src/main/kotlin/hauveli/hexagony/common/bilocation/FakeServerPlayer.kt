package hauveli.hexagony.common.bilocation

import com.mojang.authlib.GameProfile
import hauveli.hexagony.common.control.PlayerControlData
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
import net.minecraft.world.phys.Vec3
import java.util.*

class FakeServerPlayer(
    server: MinecraftServer,
    level: ServerLevel,
    original: ServerPlayer,
    pos: Vec3
) : ServerPlayer(
    server,
    level,
    GameProfile(UUID.randomUUID(), original.scoreboardName)
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

        tags.add("FakePlayer") // I don't know if there's a better way to get whether a ServerPlayer is fake or not
        // I could try to check the reply from a packet but that seems like way more effort than this, even if it would be better...

        connection = DummyServerGamePacketListenerImpl(server, this)
        DummyServerGamePacketListenerImpl.sendDummyPlayerInfo(server, this)
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

    override fun tick() {
        super.tick()

        // Even have to do gravity because this stupid thing won't even fall without it............
        // gravity
        if (!onGround()) {
            deltaMovement = deltaMovement.add(0.0, -0.08, 0.0)
        }

        if (deltaMovement.lengthSqr() > 0.0001) {
            move(MoverType.SELF, deltaMovement)
            hasImpulse = false
        }

        val friction = if (onGround()) 0.6 else 0.91
        deltaMovement = deltaMovement.scale(friction)

        if (this.health <= 0) {
            deathTime++
            if (deathTime >= 20) {
                this.dieButForReal()
            }
        }
    }

    fun removeFakePlayer() {
        server.playerList.remove(this)
        serverLevel().removePlayerImmediately(this, RemovalReason.DISCARDED)
        discard()
    }

    fun dieButForReal() {
        PlayerControlData.removeEntry(uuid, server) // This is important, more than actually dying important.
        removeFakePlayer()
    }

    // I do this just in case it's called from a bad place
    override fun die(source: DamageSource) {
        PlayerControlData.removeEntry(this.uuid, server) // This is important, more than actually dying important.
        super.die(source)
    }

    override fun disconnect() {
        PlayerControlData.removeEntry(this.uuid, server) // This is important, more than actually dying important.
        super.disconnect()
    }

    override fun tickDeath() {
        PlayerControlData.removeEntry(this.uuid, server) // This is important, more than actually dying important.
        super.tickDeath()
    }

    override fun kill() {
        PlayerControlData.removeEntry(this.uuid, server) // This is important, more than actually dying important.
        super.kill()
    }

    override fun knockback(strength: Double, x: Double, z: Double) {
        super.knockback(strength, x, z)
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
        fun spawnFakeClone(original: ServerPlayer, pos: Vec3): FakeServerPlayer {
            val server = original.server ?: throw IllegalStateException("Server null")
            val level = original.level() as ServerLevel

            val clone = FakeServerPlayer(server, level, original, pos)

            // server.playerList
            level.addFreshEntity(clone)

            return clone
        }
    }
}