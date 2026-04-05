package hauveli.hexagony

import dev.architectury.event.events.client.ClientPlayerEvent
import dev.architectury.event.events.client.ClientTooltipEvent
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.TickEvent
import dev.architectury.registry.client.rendering.RenderTypeRegistry
import hauveli.hexagony.common.bilocation.FreeCameraEntity.Companion.updateFreeCam
import hauveli.hexagony.common.control.PlayerActionAPI.onClientTick
import hauveli.hexagony.common.control.PlayerActionAPI.onServerTick
import hauveli.hexagony.common.control.PlayerControlData
import hauveli.hexagony.config.HexagonyClientConfig
import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.server.level.ServerBossEvent

object HexagonyClient {
    fun init() {
        HexagonyClientConfig.init()

        var registered = false
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register {
            PlayerControlData.onJoin()
            // Erm... is this safe? Will this re-register an onClientTick method each time a world is left and re-joined?
            if (!registered) {
                TickEvent.PLAYER_PRE.register { player ->
                }
                TickEvent.PLAYER_POST.register { player ->
                    onClientTick()
                    updateFreeCam()
                }
            }
            registered = true
        }
        TickEvent.SERVER_POST.register { server ->
            onServerTick(server)
        }
    }

    fun getConfigScreen(parent: Screen): Screen {
        return AutoConfig.getConfigScreen(HexagonyClientConfig.GlobalConfig::class.java, parent).get()
    }
}
