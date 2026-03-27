package hauveli.hexagony.fabric.xplat

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft


class FabricClientTick : ClientModInitializer {
    val mc = Minecraft.getInstance()
    val player = mc.player
    val input = player?.input

    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register {
            // input?.forwardImpulse = 1.0f
        }
    }
}