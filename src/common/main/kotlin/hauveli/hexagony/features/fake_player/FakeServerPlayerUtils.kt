package hauveli.hexagony.features.fake_player

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ParticleSpray.Companion.burst
import hauveli.hexagony.features.healthcasting.OvercastUtils
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.world.entity.Entity.RemovalReason
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import java.util.UUID


object FakeServerPlayerUtils {
    @JvmField
    var TEAM_NAME: String = "hexagony:invisible_name"

    private fun FakeServerPlayer.removeHealthCastingPenalty() {
        OvercastUtils.setModifierValue(this, 0.0)
    }

    fun FakeServerPlayer.respawnFakeClone(level: ServerLevel, pos: Vec3, uuid: UUID): FakeServerPlayer {
        val clone = FakeServerPlayer(level, uuid)

        val connection = DummyConnection()
        // val listener = DummyServerGamePacketListenerImpl(connection, clone)
        // connection.setListenerForServerboundHandshake(listener)
        clone.server.playerList.placeNewPlayer(
            connection,
            clone,
            null
        )
        // clone.getAttribute(Attributes.)

        // fuck creative mode
        clone.setGameMode(GameType.SURVIVAL)
        clone.stopRiding()
        clone.tags.add("FakePlayer") // I don't know if there's a better way to get whether a ServerPlayer is fake or not

        clone.setPos(pos)

        clone.server.playerList
            .broadcastAll(ClientboundTeleportEntityPacket( clone )) //instance.dimension);
        clone.server.playerList.broadcastAll(
            ClientboundRotateHeadPacket(clone, (clone.yHeadRot * 256 / 360).toInt().toByte()),
            clone.level().dimension()
        )

        return clone
    }

    // TODO: figure out how to set the gameMode in a smarter way...
    fun FakeServerPlayer.copyPlayerDataFromTo(target: ServerPlayer) {
        /*
        println(original.scoreboardName) // Player###
        println(original.name.toString()) // literal{Player###}
        println(original.customName.toString()) // null
         */
        target.xRot = this.xRot
        target.yRot = this.yRot
        target.yHeadRot = this.yHeadRot
        target.yBodyRot = this.yBodyRot

        // this shadows the items?
        for (i in 0 until this.inventory.containerSize) {
            val stack: ItemStack = this.inventory.getItem(i).copy()
            target.inventory.setItem(i, stack)
        }

        for (slot in EquipmentSlot.entries) {
            val stack = this.getItemBySlot(slot).copy()
            target.setItemSlot(slot, stack)
        }

        target.removeAllEffects() // hope whatever was applying an important effect wasnt single shot!
        for (effect in this.activeEffects) {
            target.addEffect(effect)
        }

        target.experienceLevel = this.experienceLevel
        target.experienceProgress = this.experienceProgress
        // target.setExperienceLevels(dummySource.experienceLevel)
        target.setExperiencePoints(this.totalExperience)

        target.foodData.foodLevel = this.foodData.foodLevel
        target.foodData.setSaturation(this.foodData.saturationLevel)
        target.foodData.setExhaustion(this.foodData.exhaustionLevel)

        target.isInvisible = this.isInvisible
        target.isCustomNameVisible = this.isCustomNameVisible
        target.customName = this.name

        target.invulnerableTime = this.invulnerableTime
        target.abilities.invulnerable = this.abilities.invulnerable
        target.isInvulnerable = this.isInvulnerable
        target.hurtMarked = this.hurtMarked
        target.remainingFireTicks = this.remainingFireTicks
        // I could try to check the reply from a packet but that seems like way more effort than this, even if it would be better...


        target.abilities.walkingSpeed = this.abilities.walkingSpeed
        target.abilities.flyingSpeed = this.abilities.flyingSpeed
        target.abilities.mayfly = this.abilities.mayfly
        target.abilities.mayBuild = this.abilities.mayBuild
        target.abilities.instabuild = this.abilities.instabuild
        target.abilities.invulnerable = this.abilities.invulnerable
        target.abilities.flying = this.abilities.flying

        // done via attributes?
        // target.setMaxUpStep( dummySource.maxUpStep() )

        target.remainingFireTicks = this.remainingFireTicks

        // clarification: I'm referring to stuff like EXP, max health, last slept etc
        // I can't think of a way to determine which modifiers would be bound
        // to the "mind" and which would be bound to the "body"...
        // TODO:
        // make it datapackable?
        // probably make it datapackable, yeah...
        //
        this.removeHealthCastingPenalty()
        target.health = this.health

        target.attributes.load( this.attributes.save() )

        // need real source for this, maybe...
        target.setGameMode(this.gameMode.gameModeForPlayer)

        if (target.connection != null) {
            target.teleportTo(
                this.serverLevel(),
                this.position().x,
                this.position().y,
                this.position().z,
                this.yRot,
                this.xRot
            )
        } else {
            target.setServerLevel(this.serverLevel())
            target.setPos(this.position())
        }


        println("Max health was: ${target.maxHealth} for target")
        println("Max health was: ${this.maxHealth} for source")

        this.discard()
    }

    fun FakeServerPlayer.copyPlayerDataFrom(original: ServerPlayer): FakePlayer {
        val server = original.server ?: throw IllegalStateException("Server null")
        val level = original.level() as ServerLevel

        val dummyHolder = FakePlayer(
            server,
            level,
            original.blockPosition(),
            original.yRot
        )

        // fuck everything
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

        for (effect in original.activeEffects) {
            dummyHolder.addEffect(effect)
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


        // I think this is done via attributes?
        // dummyHolder.setMaxUpStep( original.maxUpStep() )

        dummyHolder.remainingFireTicks = original.remainingFireTicks

        dummyHolder.attributes.load( original.attributes.save() )

        dummyHolder.setPos(original.position())

        return dummyHolder
    }

    fun FakeServerPlayer.doFakeConnectionStuff() {
        val connection = DummyConnection()
        val cookie = CommonListenerCookie(this.gameProfile, 0, this.clientInformation(), false)
        val listener = DummyServerGamePacketListenerImpl(connection, this, cookie)
        // connection.setListenerForServerboundHandshake(listener)
        connection.setListenerForServerboundHandshake(listener)

        this.server.playerList.placeNewPlayer(
            connection,
            this,
            cookie // err...
        )
        /*
        */
        // clone.getAttribute(Attributes.)

        // fuck creative mode
        this.setGameMode(GameType.SURVIVAL)
        this.stopRiding()
        this.tags.add("FakePlayer") // I don't know if there's a better way to get whether a ServerPlayer is fake or not

        server.playerList
            .broadcastAll(ClientboundTeleportEntityPacket( this )) //instance.dimension);
        server.playerList.broadcastAll(
            ClientboundRotateHeadPacket(this, (this.yHeadRot * 256 / 360).toInt().toByte()),
            this.level().dimension()
        )

    }

    fun spawnFakeClone(original: ServerPlayer, pos: Vec3, uuid: UUID): FakeServerPlayer {
        val server = original.server ?: throw IllegalStateException("Server null")
        val level = original.level() as ServerLevel

        val clone = FakeServerPlayer(level, uuid)

        clone.moveTo(pos.x, pos.y, pos.z, 0f, 0f)
        // Idk if feet or eyes
        //clone.lookAt(EntityAnchorArgument.Anchor.FEET, original.eyePosition)
        // can't call lookat without connection...

        // newly spawned dummy should have nothing
        //val dummy = copyPlayerDataFrom(original)
        //copyPlayerDataFromTo(original, dummy)
        clone.doFakeConnectionStuff()
        clone.hidePlayerName()
        clone.theatrics()

        return clone
    }


    // player nametag visibility on tablist
    fun FakeServerPlayer.hidePlayerName() {
        val ser = this.server ?: return
        val scoreboard: Scoreboard = ser.scoreboard

        val team: PlayerTeam = scoreboard.getPlayerTeam(TEAM_NAME) ?: scoreboard.addPlayerTeam(TEAM_NAME)

        team.nameTagVisibility = Team.Visibility.NEVER
        scoreboard.addPlayerToTeam(this.gameProfile.name, team)
    }

    fun FakeServerPlayer.stopTracking() {
        val ser = server ?: return
        // PlayerControlData.removeEntry(uuid, ser) // This is important, more than actually dying important.
    }

    fun FakeServerPlayer.sendDisconnected() {
        val connection = DummyConnection()
        // val listener = DummyServerGamePacketListenerImpl(connection, this)
        // connection.setListenerForServerboundHandshake(listener)
        server.playerList.remove(
            this
        )
        // clone.getAttribute(Attributes.)
        // server.playerList.broadcastAll(ClientboundPlayerInfoRemovePacket( this )) //instance.dimension);
        server.playerList.broadcastAll(ClientboundPlayerInfoRemovePacket(listOf(this.uuid)))
    }

    fun FakeServerPlayer.theatrics() {
        val particleSpray = burst(
            this.position().add(0.0, this.eyeHeight / 2.0, 0.0), 1.0, 10
        )
        val pigment = HexAPI.INSTANCE.get().getColorizer(this)
        particleSpray.sprayParticles(this.serverLevel(), pigment)
    }

    fun FakeServerPlayer.removeForReal() {
        this.theatrics()
        this.stopTracking()
        this.discard()
        this.remove(RemovalReason.DISCARDED)
        // AHHH i don't know how to make this fake player go away
        this.sendDisconnected() // PLEASSSEEEEE
        this.disconnect() // move this up if it doesnt work
    }
}