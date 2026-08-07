package hauveli.hexagony.client

import hauveli.hexagony.features.hat.HatLayer
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.player.PlayerRenderer
import net.minecraft.client.resources.PlayerSkin
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent


object NeoForgeHexagonyClient {
    @Suppress("UNUSED_PARAMETER")
    fun init(event: FMLClientSetupEvent) {
        HexagonyClient.init()
    }

    @SubscribeEvent
    fun addEntityLayers(event: EntityRenderersEvent.AddLayers) {
        val playerRenderer = event.getSkin<EntityRenderer<out Player?>?>(PlayerSkin.Model.WIDE)
        if (playerRenderer is PlayerRenderer) {
            playerRenderer.addLayer(HatLayer<AbstractClientPlayer?, PlayerModel<AbstractClientPlayer?>?>(playerRenderer))
        }
        val playerRendererWide = event.getSkin<EntityRenderer<out Player?>?>(PlayerSkin.Model.WIDE)
        if (playerRendererWide is PlayerRenderer) {
            playerRendererWide.addLayer(HatLayer<AbstractClientPlayer?, PlayerModel<AbstractClientPlayer?>?>(playerRendererWide))
        }
    }
}