package hauveli.hexagony.common.bilocation

import com.mojang.authlib.GameProfile
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.*

class FakeServerPlayer(
    server: MinecraftServer,
    level: ServerLevel,
    val original: ServerPlayer
) : ServerPlayer(
    server,
    level,
    GameProfile(UUID.randomUUID(), original.name.toString())
    ) {
    init {
        moveTo(original.x, original.y, original.z, original.yRot, original.xRot)

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

        connection = DummyServerGamePacketListenerImpl(server, this)
        DummyServerGamePacketListenerImpl.sendDummyPlayerInfo(server, this)
    }

    override fun tick() {
        super.tick()
    }

    override fun isSpectator(): Boolean {
        return false
    }

    override fun isCreative(): Boolean {
        return false
    }

    override fun aiStep() {
        // empty it
    }

    companion object {
        fun spawnFakeClone(original: ServerPlayer): FakeServerPlayer {
            val server = original.server ?: throw IllegalStateException("Server null")
            val level = original.level() as ServerLevel

            val clone = FakeServerPlayer(server, level, original)

            // server.playerList
            level.addFreshEntity(clone)

            return clone
        }
    }
}