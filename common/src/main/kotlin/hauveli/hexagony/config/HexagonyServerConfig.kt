package hauveli.hexagony.config

import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.events.common.TickEvent
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.ConfigHolder
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.TransitiveObject
import me.shedaniel.autoconfig.serializer.PartitioningSerializer
import me.shedaniel.autoconfig.serializer.PartitioningSerializer.GlobalData
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.InteractionResult
import hauveli.hexagony.Hexagony
import hauveli.hexagony.common.control.PlayerActionAPI.onServerTick
import hauveli.hexagony.networking.msg.MsgSyncConfigS2C

object HexagonyServerConfig {
    @JvmStatic
    lateinit var holder: ConfigHolder<GlobalConfig>

    @JvmStatic
    val config get() = syncedServerConfig ?: holder.config.server

    // only used on the client
    private var syncedServerConfig: ServerConfig? = null

    fun init() {
        holder = AutoConfig.register(
            GlobalConfig::class.java,
            PartitioningSerializer.wrap(::Toml4jConfigSerializer),
        )

        // prevent this holder from saving the server config; that happens in the client config gui
        holder.registerSaveListener { _, _ -> InteractionResult.FAIL }
        TickEvent.Server.SERVER_POST.register {
                server ->
            onServerTick(server)
        }
    }

    fun initServer() {
        PlayerEvent.PLAYER_JOIN.register { player ->
            MsgSyncConfigS2C(holder.config.server).sendToPlayer(player)
        }
        TickEvent.Server.SERVER_POST.register {
                server ->
            onServerTick(server)
        }
    }

    fun onSyncConfig(serverConfig: ServerConfig?) {
        syncedServerConfig = serverConfig
    }

    @Config(name = Hexagony.MODID)
    class GlobalConfig(
        @Category("server")
        @TransitiveObject
        val server: ServerConfig = ServerConfig(),
    ) : GlobalData()

    @Config(name = "server")
    class ServerConfig : ConfigData {
        fun encode(buf: FriendlyByteBuf) {
            buf.writeInt(dummyServerConfigOption)
            buf.writeDouble(recoveryPerRest)
            buf.writeDouble(maximumHealthPenaltyMultiplier)
            buf.writeDouble(overcastDamagePenaltyMultiplier)
            buf.writeDouble(overcastDamagePenaltyAdditionalDamage)
            buf.writeBoolean(requireScrollForEnlightenment)
            buf.writeUtf(overcastAttributeName)
        }

        fun decode(buf: FriendlyByteBuf): ServerConfig {
            dummyServerConfigOption = buf.readInt()
            recoveryPerRest = buf.readDouble()
            maximumHealthPenaltyMultiplier = buf.readDouble()
            overcastDamagePenaltyMultiplier = buf.readDouble()
            overcastDamagePenaltyAdditionalDamage = buf.readDouble()
            requireScrollForEnlightenment = buf.readBoolean()
            overcastAttributeName = buf.readUtf()
            return this
        }

        @Tooltip
        var dummyServerConfigOption: Int = 64
            private set
        @Tooltip
        var recoveryPerRest: Double = 1.0
            private set
        @Tooltip
        var maximumHealthPenaltyMultiplier: Double = 1.0
            private set
        @Tooltip
        var overcastDamagePenaltyMultiplier: Double = 1.0
            private set
        @Tooltip
        var overcastDamagePenaltyAdditionalDamage: Double = 0.0
            private set
        @Tooltip
        var overcastAttributeName: String = "hexagony_overcast_penalty"
            private set
        @Tooltip
        var requireScrollForEnlightenment: Boolean = true
            private set
    }
}
