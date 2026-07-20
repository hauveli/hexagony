package hauveli.hexagony.features.fake_player

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import com.mojang.authlib.GameProfile
import hauveli.hexagony.features.fake_player.FakeServerPlayerUtils.removeForReal
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.player.ChatVisiblity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.portal.DimensionTransition
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import java.util.*


class FakeServerPlayer(
    level: ServerLevel,
    uuid: UUID
) : ServerPlayer(
    level.server,
    level,
    GameProfile(uuid, "Homunculus"),
    ClientInformation(
        "idk",
        0,
        ChatVisiblity.HIDDEN,
        false,
        0,
        HumanoidArm.RIGHT,
        false,
        false
    )
) {

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

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        return super.hurt(source, amount)
    }

    override fun baseTick() {
        super.baseTick()
    }

    override fun isControlledByLocalInstance(): Boolean {
        return true
    }

    override fun die(damageSource: DamageSource) {
        super.die(damageSource)
        removeForReal()
    }

    override fun kill() {
        super.kill()
        removeForReal()
    }

    // everything below here is from gnembon's fabric carpet mod (or modified version of it)
    // https://github.com/gnembon/fabric-carpet/blob/1.20.2/src/main/java/carpet/patches/EntityPlayerMPFake.java
    override fun onEquipItem(slot: EquipmentSlot, previous: ItemStack, stack: ItemStack) {
        if (!isUsingItem) super.onEquipItem(slot, previous, stack)
    }

    override fun tick() {
        if (this.getServer()!!.tickCount % 10 == 0) {
            this.connection.resetPosition()
            this.serverLevel().chunkSource.move(this)
        }
        try {
            super.tick()
            this.doTick()
            if (isDeadOrDying) {
                shakeOff()
                if (deathTime >= 20) {
                    removeForReal()
                }
            }
        } catch (ignored: NullPointerException) {
            // happens with that paper port thingy - not sure what that would fix, but hey
            // the game not gonna crash violently.
        }
    }

    private fun shakeOff() {
        if (vehicle is Player) stopRiding()
        for (passenger in indirectPassengers) {
            if (passenger is Player) passenger.stopRiding()
        }
    }

    override fun getIpAddress(): String {
        return "127.0.0.1" // hee hee, maybe set to server ip? unsure
    }

    override fun allowsListing(): Boolean {
        return false
    }

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {
        doCheckFallDamage(0.0, y, 0.0, onGround)
    }

    override fun changeDimension(transition: DimensionTransition): Entity? {
        return super.changeDimension(transition)
        // idk how to add the below stuff here...
        /*
        super.changeDimension(serverLevel)
        if (wonGame) {
            val p = ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)
            connection.handleClientCommand(p)
        }

        // whar
        if (connection.player.isChangingDimension) {
            connection.player.hasChangedDimension()
        }
        return connection.player
         */
    }
}