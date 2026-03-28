package hauveli.hexagony.forge.xplat
import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.common.msgs.*
import at.petrak.hexcasting.forge.network.MsgAltioraUpdateAck
import at.petrak.hexcasting.forge.network.MsgPigmentUpdateAck
import at.petrak.hexcasting.forge.network.MsgSentinelStatusUpdateAck
import hauveli.hexagony.Hexagony
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.simple.SimpleChannel
import org.apache.logging.log4j.util.TriConsumer
import java.util.function.*
import java.util.function.Function

object ForgePacketHandler {
    private const val PROTOCOL_VERSION = "1"
    private val NETWORK: SimpleChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocation(Hexagony.MODID,"main"),
        { PROTOCOL_VERSION },
        { anObject: String? -> PROTOCOL_VERSION == anObject },
        { anObject: String? -> PROTOCOL_VERSION == anObject }
    )

    val network: SimpleChannel
        get() = NETWORK

    fun init() {
        var messageIdx = 0
        /*
        NETWORK.registerMessage(
            messageIdx++,
            MsgBrainsweepAck::class.java,
            { obj: MsgBrainsweepAck?, buf: FriendlyByteBuf? -> obj!!.serialize(buf!!) },
            MsgBrainsweepAck::deserialize,
            ForgePacketHandler.makeClientBoundHandler(MsgBrainsweepAck::handle)
        )
        */
    }

    private fun <T> makeServerBoundHandler(
        handler: TriConsumer<T?, MinecraftServer?, ServerPlayer?>
    ): BiConsumer<T?, Supplier<NetworkEvent.Context?>?> {
        return BiConsumer { m: T?, ctx: Supplier<NetworkEvent.Context?>? ->
            handler.accept(m, ctx!!.get()!!.getSender()!!.getServer(), ctx.get()!!.getSender())
            ctx.get()!!.setPacketHandled(true)
        }
    }

    private fun <T> makeClientBoundHandler(consumer: Consumer<T?>): BiConsumer<T?, Supplier<NetworkEvent.Context?>?> {
        return BiConsumer { m: T?, ctx: Supplier<NetworkEvent.Context?>? ->
            consumer.accept(m)
            ctx!!.get()!!.setPacketHandled(true)
        }
    }
}