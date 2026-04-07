package hauveli.hexagony.common.bilocation

import com.mojang.authlib.GameProfile
import hauveli.hexagony.common.control.PlayerControlData
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.TickTask
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.food.FoodData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import java.util.*


class FakeServerPlayer(
    server: MinecraftServer,
    level: ServerLevel,
    uuid: UUID
) : ServerPlayer(
    server,
    level,
    GameProfile(uuid, "Homunculus")
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

    // player nametag visibility
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

    private fun removeForReal() {
        stopTracking()
        super.disconnect()
        super.discard()
    }

    companion object {
        fun spawnFakeClone(original: ServerPlayer, pos: Vec3, uuid: UUID): FakeServerPlayer {
            val server = original.server ?: throw IllegalStateException("Server null")
            val level = original.level() as ServerLevel

            val clone = FakeServerPlayer(server, level, uuid)

            clone.moveTo(pos.x, pos.y, pos.z, 0f, 0f)

            /*
            println(original.scoreboardName) // Player###
            println(original.name.toString()) // literal{Player###}
            println(original.customName.toString()) // null
             */

            // this shadows the items?
            for (i in 0 until original.inventory.containerSize) {
                val stack: ItemStack = original.inventory.getItem(i).copy()
                clone.inventory.setItem(i, stack)
            }

            for (slot in EquipmentSlot.entries) {
                val stack = original.getItemBySlot(slot).copy()
                clone.setItemSlot(slot, stack)
            }

            clone.experienceLevel = original.experienceLevel
            clone.experienceProgress = original.experienceProgress
            clone.health = original.health
            clone.foodData.foodLevel = original.foodData.foodLevel
            clone.foodData.setSaturation(original.foodData.saturationLevel)
            clone.foodData.setExhaustion(original.foodData.exhaustionLevel)

            clone.isInvisible = false
            clone.isCustomNameVisible = false
            clone.customName = Component.literal("Homunculus")
            clone.hidePlayerName()

            clone.invulnerableTime = 0
            clone.abilities.invulnerable = false
            clone.isInvulnerable = false
            clone.hurtMarked = false
            clone.remainingFireTicks = 0
            // I could try to check the reply from a packet but that seems like way more effort than this, even if it would be better...


            clone.abilities.walkingSpeed = original.abilities.walkingSpeed
            clone.abilities.flyingSpeed = original.abilities.flyingSpeed
            clone.abilities.mayfly = original.abilities.mayfly
            clone.abilities.mayBuild = original.abilities.mayBuild
            clone.abilities.instabuild = original.abilities.instabuild
            clone.abilities.invulnerable = original.abilities.invulnerable
            clone.abilities.flying = original.abilities.flying

            clone.setMaxUpStep( original.maxUpStep() )

            clone.remainingFireTicks = original.remainingFireTicks

            clone.attributes.load( original.attributes.save() )

            clone.abilities.invulnerable = false

            val connection = DummyConnection(PacketFlow.SERVERBOUND)
            val listener = DummyServerGamePacketListenerImpl(server, connection,clone)
            connection.setListener(listener)
            server.playerList.placeNewPlayer(
                connection,
                clone
            )
            // clone.getAttribute(Attributes.)


            clone.setGameMode(original.gameMode.gameModeForPlayer)

            // fuck creative mode
            clone.setGameMode(GameType.SURVIVAL)
            clone.stopRiding()
            clone.tags.add("FakePlayer") // I don't know if there's a better way to get whether a ServerPlayer is fake or not

            server.playerList
                .broadcastAll(ClientboundTeleportEntityPacket( clone )) //instance.dimension);
            server.playerList.broadcastAll(
                ClientboundRotateHeadPacket(clone, (clone.yHeadRot * 256 / 360).toInt().toByte()),
                level.dimension()
            )

            return clone
        }
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
        return "127.0.0.1"
    }

    override fun allowsListing(): Boolean {
        return false
    }

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {
        doCheckFallDamage(0.0, y, 0.0, onGround)
    }

    override fun changeDimension(serverLevel: ServerLevel): Entity? {
        super.changeDimension(serverLevel)
        if (wonGame) {
            val p = ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)
            connection.handleClientCommand(p)
        }

        // If above branch was taken, *this* has been removed and replaced, the new instance has been set
        // on 'our' connection (which is now theirs, but we still have a ref).
        if (connection.player.isChangingDimension) {
            connection.player.hasChangedDimension()
        }
        return connection.player
    }
}