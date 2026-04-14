package hauveli.hexagony.common.bilocation

import com.mojang.authlib.GameProfile
import hauveli.hexagony.common.control.PlayerControlData
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
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

    fun removeForReal() {
        stopTracking()
        super.disconnect()
        super.discard()
    }

    companion object {

        fun respawnFakeClone(server: MinecraftServer, level: ServerLevel, pos: Vec3, uuid: UUID): FakeServerPlayer {
            val clone = FakeServerPlayer(server, level, uuid)

            val connection = DummyConnection(PacketFlow.SERVERBOUND)
            val listener = DummyServerGamePacketListenerImpl(server, connection,clone)
            connection.setListener(listener)
            server.playerList.placeNewPlayer(
                connection,
                clone
            )
            // clone.getAttribute(Attributes.)

            // fuck creative mode
            clone.setGameMode(GameType.SURVIVAL)
            clone.stopRiding()
            clone.tags.add("FakePlayer") // I don't know if there's a better way to get whether a ServerPlayer is fake or not

            clone.setPos(pos)

            server.playerList
                .broadcastAll(ClientboundTeleportEntityPacket( clone )) //instance.dimension);
            server.playerList.broadcastAll(
                ClientboundRotateHeadPacket(clone, (clone.yHeadRot * 256 / 360).toInt().toByte()),
                level.dimension()
            )

            return clone
        }

        fun copyPlayerDataFromTo(source: ServerPlayer, target: ServerPlayer) {
            val server = source.server ?: throw IllegalStateException("Server null")
            val level = source.level() as ServerLevel

            /*
            println(original.scoreboardName) // Player###
            println(original.name.toString()) // literal{Player###}
            println(original.customName.toString()) // null
             */
            target.xRot = source.xRot
            target.yRot = source.yRot
            target.yHeadRot = source.yHeadRot
            target.yBodyRot = source.yBodyRot

            // this shadows the items?
            for (i in 0 until source.inventory.containerSize) {
                val stack: ItemStack = source.inventory.getItem(i).copy()
                target.inventory.setItem(i, stack)
            }

            for (slot in EquipmentSlot.entries) {
                val stack = source.getItemBySlot(slot).copy()
                target.setItemSlot(slot, stack)
            }

            target.experienceLevel = source.experienceLevel
            target.experienceProgress = source.experienceProgress
            target.health = source.health
            target.foodData.foodLevel = source.foodData.foodLevel
            target.foodData.setSaturation(source.foodData.saturationLevel)
            target.foodData.setExhaustion(source.foodData.exhaustionLevel)

            target.isInvisible = source.isInvisible
            target.isCustomNameVisible = source.isCustomNameVisible
            target.customName = source.name

            target.invulnerableTime = source.invulnerableTime
            target.abilities.invulnerable = source.abilities.invulnerable
            target.isInvulnerable = source.isInvulnerable
            target.hurtMarked = source.hurtMarked
            target.remainingFireTicks = source.remainingFireTicks
            // I could try to check the reply from a packet but that seems like way more effort than this, even if it would be better...


            target.abilities.walkingSpeed = source.abilities.walkingSpeed
            target.abilities.flyingSpeed = source.abilities.flyingSpeed
            target.abilities.mayfly = source.abilities.mayfly
            target.abilities.mayBuild = source.abilities.mayBuild
            target.abilities.instabuild = source.abilities.instabuild
            target.abilities.invulnerable = source.abilities.invulnerable
            target.abilities.flying = source.abilities.flying

            target.setMaxUpStep( source.maxUpStep() )

            target.remainingFireTicks = source.remainingFireTicks

            target.attributes.load( source.attributes.save() )

            target.setGameMode(source.gameMode.gameModeForPlayer)

            if (target.connection != null) {
                target.teleportTo(
                    source.serverLevel(),
                    source.position().x,
                    source.position().y,
                    source.position().z,
                    source.yRot,
                    source.xRot
                )
            } else {
                target.setServerLevel(source.serverLevel())
                target.setPos(source.position())
            }

            source.discard()
        }

        fun copyPlayerDataFrom(original: ServerPlayer): ServerPlayer {
            val server = original.server ?: throw IllegalStateException("Server null")
            val level = original.level() as ServerLevel

            val dummyHolder = FakeServerPlayer(server, level, UUID.randomUUID())
            /*
            println(original.scoreboardName) // Player###
            println(original.name.toString()) // literal{Player###}
            println(original.customName.toString()) // null
             */
            dummyHolder.xRot = original.xRot
            dummyHolder.yRot = original.yRot
            dummyHolder.yHeadRot = original.yHeadRot
            dummyHolder.yBodyRot = original.yBodyRot

            // this shadows the items?
            for (i in 0 until original.inventory.containerSize) {
                val stack: ItemStack = original.inventory.getItem(i).copy()
                dummyHolder.inventory.setItem(i, stack)
            }

            for (slot in EquipmentSlot.entries) {
                val stack = original.getItemBySlot(slot).copy()
                dummyHolder.setItemSlot(slot, stack)
            }

            dummyHolder.experienceLevel = original.experienceLevel
            dummyHolder.experienceProgress = original.experienceProgress
            dummyHolder.health = original.health
            dummyHolder.foodData.foodLevel = original.foodData.foodLevel
            dummyHolder.foodData.setSaturation(original.foodData.saturationLevel)
            dummyHolder.foodData.setExhaustion(original.foodData.exhaustionLevel)

            dummyHolder.isInvisible = original.isInvisible
            dummyHolder.isCustomNameVisible = original.isCustomNameVisible
            dummyHolder.customName = original.customName
            // dummyHolder.hidePlayerName()

            dummyHolder.invulnerableTime = original.invulnerableTime
            dummyHolder.isInvulnerable = original.isInvulnerable
            dummyHolder.hurtMarked = original.hurtMarked
            dummyHolder.remainingFireTicks = original.remainingFireTicks
            // I could try to check the reply from a packet but that seems like way more effort than this, even if it would be better...


            dummyHolder.abilities.walkingSpeed = original.abilities.walkingSpeed
            dummyHolder.abilities.flyingSpeed = original.abilities.flyingSpeed
            dummyHolder.abilities.mayfly = original.abilities.mayfly
            dummyHolder.abilities.mayBuild = original.abilities.mayBuild
            dummyHolder.abilities.instabuild = original.abilities.instabuild
            dummyHolder.abilities.invulnerable = original.abilities.invulnerable
            dummyHolder.abilities.flying = original.abilities.flying

            dummyHolder.setMaxUpStep( original.maxUpStep() )

            dummyHolder.remainingFireTicks = original.remainingFireTicks

            dummyHolder.attributes.load( original.attributes.save() )

            dummyHolder.setGameMode(original.gameMode.gameModeForPlayer)

            dummyHolder.setPos(original.position())

            dummyHolder.setServerLevel(original.serverLevel())

            return dummyHolder
        }

        fun doFakeConnectionStuff(server: MinecraftServer, fakePlayer: ServerPlayer) {
            val connection = DummyConnection(PacketFlow.SERVERBOUND)
            val listener = DummyServerGamePacketListenerImpl(server, connection, fakePlayer)
            connection.setListener(listener)
            server.playerList.placeNewPlayer(
                connection,
                fakePlayer
            )
            // clone.getAttribute(Attributes.)

            // fuck creative mode
            fakePlayer.setGameMode(GameType.SURVIVAL)
            fakePlayer.stopRiding()
            fakePlayer.tags.add("FakePlayer") // I don't know if there's a better way to get whether a ServerPlayer is fake or not

            server.playerList
                .broadcastAll(ClientboundTeleportEntityPacket( fakePlayer )) //instance.dimension);
            server.playerList.broadcastAll(
                ClientboundRotateHeadPacket(fakePlayer, (fakePlayer.yHeadRot * 256 / 360).toInt().toByte()),
                fakePlayer.level().dimension()
            )

        }

        fun spawnFakeClone(original: ServerPlayer, pos: Vec3, uuid: UUID): FakeServerPlayer {
            val server = original.server ?: throw IllegalStateException("Server null")
            val level = original.level() as ServerLevel

            val clone = FakeServerPlayer(server, level, uuid)

            clone.moveTo(pos.x, pos.y, pos.z, 0f, 0f)
            // Idk if feet or eyes
            //clone.lookAt(EntityAnchorArgument.Anchor.FEET, original.eyePosition)
            // can't call lookat without connection...

            // newly spawned dummy should have nothing
            //val dummy = copyPlayerDataFrom(original)
            //copyPlayerDataFromTo(original, dummy)

            doFakeConnectionStuff(server, clone)
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