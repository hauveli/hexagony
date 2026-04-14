package hauveli.hexagony.common.bilocation

import com.mojang.authlib.GameProfile
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.config.HexagonyServerConfig.config
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import java.util.*


class FakePlayer(
    server: MinecraftServer,
    level: ServerLevel,
    pos: BlockPos,
    yRot: Float
) : Player(
    level,
    pos,
    yRot,
    GameProfile(UUID.randomUUID(), "HomunculusDummy")
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
        super.discard()
    }

    companion object {


        private fun removeHealthCastingPenalty(player: ServerPlayer) {
            val maxHealth = player.getAttribute(Attributes.MAX_HEALTH) ?: return
            // can maxHealth be null?
            val modifiers: MutableCollection<AttributeModifier> = maxHealth.modifiers

            // future me speaking, I should probably just have incremented a single modifier instead of doing this...
            // TODO: make healthcast penalty modifier a single modifier instead of multiple
            // todo: and simply decrement the value of it....
            for (modifier in modifiers.stream().toList()) {
                if (config.overcastAttributeName == modifier.name) {
                    // checking against name, not uuid
                    // It might be better to construct a list of uuids and store those
                    // but frankly, chance of hexcasting_overcast_penalty
                    // being used for anything but this is so exceedingly low I dont care
                    // to implement the cleaner solution, this simple one is ok for now
                    maxHealth.removeModifier(modifier)
                }
            }
        }


        // TODO: figure out how to set the gameMode in a smarter way...
        fun copyPlayerDataFromTo(realSource: ServerPlayer, dummySource: ServerPlayer, target: ServerPlayer) {
            /*
            println(original.scoreboardName) // Player###
            println(original.name.toString()) // literal{Player###}
            println(original.customName.toString()) // null
             */
            target.xRot = dummySource.xRot
            target.yRot = dummySource.yRot
            target.yHeadRot = dummySource.yHeadRot
            target.yBodyRot = dummySource.yBodyRot

            // this shadows the items?
            for (i in 0 until dummySource.inventory.containerSize) {
                val stack: ItemStack = dummySource.inventory.getItem(i).copy()
                target.inventory.setItem(i, stack)
            }

            for (slot in EquipmentSlot.entries) {
                val stack = dummySource.getItemBySlot(slot).copy()
                target.setItemSlot(slot, stack)
            }

            target.removeAllEffects() // hope whatever was applying an important effect wasnt single shot!
            for (effect in dummySource.activeEffects) {
                target.addEffect(effect)
            }

            target.experienceLevel = dummySource.experienceLevel
            target.experienceProgress = dummySource.experienceProgress
            // target.setExperienceLevels(dummySource.experienceLevel)
            target.setExperiencePoints(dummySource.totalExperience)

            target.health = dummySource.health
            target.foodData.foodLevel = dummySource.foodData.foodLevel
            target.foodData.setSaturation(dummySource.foodData.saturationLevel)
            target.foodData.setExhaustion(dummySource.foodData.exhaustionLevel)

            target.isInvisible = dummySource.isInvisible
            target.isCustomNameVisible = dummySource.isCustomNameVisible
            target.customName = dummySource.name

            target.invulnerableTime = dummySource.invulnerableTime
            target.abilities.invulnerable = dummySource.abilities.invulnerable
            target.isInvulnerable = dummySource.isInvulnerable
            target.hurtMarked = dummySource.hurtMarked
            target.remainingFireTicks = dummySource.remainingFireTicks
            // I could try to check the reply from a packet but that seems like way more effort than this, even if it would be better...


            target.abilities.walkingSpeed = dummySource.abilities.walkingSpeed
            target.abilities.flyingSpeed = dummySource.abilities.flyingSpeed
            target.abilities.mayfly = dummySource.abilities.mayfly
            target.abilities.mayBuild = dummySource.abilities.mayBuild
            target.abilities.instabuild = dummySource.abilities.instabuild
            target.abilities.invulnerable = dummySource.abilities.invulnerable
            target.abilities.flying = dummySource.abilities.flying

            target.setMaxUpStep( dummySource.maxUpStep() )

            target.remainingFireTicks = dummySource.remainingFireTicks

            // I can't think of a way to determine which modifiers would be bound
            // to the "mind" and which would be bound to the "body"...
            removeHealthCastingPenalty(target)

            target.attributes.load( dummySource.attributes.save() )

            // need real source for this, maybe...
            target.setGameMode(realSource.gameMode.gameModeForPlayer)

            if (target.connection != null) {
                target.teleportTo(
                    dummySource.serverLevel(),
                    dummySource.position().x,
                    dummySource.position().y,
                    dummySource.position().z,
                    dummySource.yRot,
                    dummySource.xRot
                )
            } else {
                target.setServerLevel(dummySource.serverLevel())
                target.setPos(dummySource.position())
            }


            println("Max health was: ${target.maxHealth} for target")
            println("Max health was: ${dummySource.maxHealth} for source")

            dummySource.discard()
        }

        fun copyPlayerDataFrom(original: ServerPlayer): FakePlayer {
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

            dummyHolder.setPos(original.position())

            return dummyHolder
        }

    }

    // everything below here is from gnembon's fabric carpet mod (or modified version of it)
    // https://github.com/gnembon/fabric-carpet/blob/1.20.2/src/main/java/carpet/patches/EntityPlayerMPFake.java
    override fun onEquipItem(slot: EquipmentSlot, previous: ItemStack, stack: ItemStack) {
        if (!isUsingItem) super.onEquipItem(slot, previous, stack)
    }
}