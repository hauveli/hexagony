package hauveli.hexagony.fabric

import hauveli.hexagony.HexagonyClient
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.Minecraft


object FabricHexagonyClient : ClientModInitializer {
    override fun onInitializeClient() {
        HexagonyClient.init()

        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            val mc = Minecraft.getInstance()
            val player = mc.player ?: return@register

            if (mc.cameraEntity != player) {
                val dispatcher = mc.entityRenderDispatcher
                val buffer = context.consumers() ?: return@register
                val poseStack = context.matrixStack()
                val bufferSource = mc.renderBuffers().bufferSource()

                poseStack.pushPose()

                dispatcher.render(
                    player,
                    player.x - context.camera().position.x,
                    player.y - context.camera().position.y,
                    player.z - context.camera().position.z,
                    player.yRot,
                    context.tickDelta(),
                    poseStack,
                    buffer,
                    15728880
                )

                poseStack.popPose()

                bufferSource.endBatch() // IMPORTANT
            }
        }
    }
}
