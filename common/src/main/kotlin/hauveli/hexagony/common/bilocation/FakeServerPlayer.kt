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
) : Player(
    level,
    original.blockPosition(),
    original.yRot,
    GameProfile(UUID.randomUUID(), original.name.string)) {
    init {
        moveTo(original.x, original.y, original.z, original.yRot, original.xRot)

        for (i in 0 until original.inventory.containerSize) {
            val stack: ItemStack = original.inventory.getItem(i).copy()
            inventory.setItem(i, stack)
        }

        for (slot in EquipmentSlot.values()) {
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
    }

    override fun tick() {
        super.tick()
        // Optional: AI or movement logic here
    }

    override fun isSpectator(): Boolean {
        return false
    }

    override fun isCreative(): Boolean {
        return false
    }

    companion object {
        fun spawnFakeClone(original: ServerPlayer): FakeServerPlayer {
            val server = original.server ?: throw IllegalStateException("Server null")
            val level = original.level() as ServerLevel

            val clone = FakeServerPlayer(server, level, original)

            level.addFreshEntity(clone)

            return clone
        }
    }
}