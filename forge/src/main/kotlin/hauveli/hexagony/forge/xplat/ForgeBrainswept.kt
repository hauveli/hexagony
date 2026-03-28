package hauveli.hexagony.forge.xplat

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.common.msgs.IMessage
import at.petrak.hexcasting.xplat.IXplatAbstractions
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob

// From HexMod
/**
 * Sent server->client to synchronize the status of a brainswept mob.
 */
@JvmRecord
data class MsgBrainsweepAck(val target: Int) : IMessage {
    override fun getFabricId(): ResourceLocation {
        return ID
    }

    override fun serialize(buf: FriendlyByteBuf) {
        buf.writeInt(target)
    }

    companion object {
        val ID: ResourceLocation = HexAPI.modLoc("sweep")

        fun deserialize(buffer: ByteBuf): MsgBrainsweepAck {
            val buf = FriendlyByteBuf(buffer)

            val target = buf.readInt()
            return MsgBrainsweepAck(target)
        }

        fun of(target: Entity): MsgBrainsweepAck {
            return MsgBrainsweepAck(target.getId())
        }

        fun handle(msg: MsgBrainsweepAck) {
            Minecraft.getInstance().execute(object : Runnable {
                override fun run() {
                    val level = Minecraft.getInstance().level
                    if (level != null) {
                        val entity = level.getEntity(msg.target)
                        if (entity is Mob) {
                            IXplatAbstractions.INSTANCE.setBrainsweepAddlData(entity)
                        }
                    }
                }
            })
        }
    }
}