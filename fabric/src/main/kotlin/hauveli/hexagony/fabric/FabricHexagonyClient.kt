package hauveli.hexagony.fabric

import hauveli.hexagony.HexagonyClient
import hauveli.hexagony.common.client.PlayerMovementAPI
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

object FabricHexagonyClient : ClientModInitializer {
    override fun onInitializeClient() {
        HexagonyClient.init()
        ClientTickEvents.START_CLIENT_TICK.register {
            // Minecraft.getInstance().player?.jumpFromGround()
            //Minecraft.getInstance().player?.zza = 1.0f // forward movement, seriously what even is this? why zza?
            // Minecraft.getInstance().player?.xxa = 1.0f // sideways movement, seriously what even is this? why xxa?
            // Minecraft.getInstance().player?.yya = 1.0f // upwards movement, seriously what even is this? why yya?
            PlayerMovementAPI.onClientTick()
        }
    }
}
