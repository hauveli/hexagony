package hauveli.hexagony.client

import hauveli.hexagony.features.hat.HatLayer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.player.PlayerRenderer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity

@Environment(EnvType.CLIENT)
object FabricHexagonyClient : ClientModInitializer {
    override fun onInitializeClient() {
        HexagonyClient.init()

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(LivingEntityFeatureRendererRegistrationCallback { entityType: EntityType<out LivingEntity?>?, entityRenderer: LivingEntityRenderer<*, *>?, registrationHelper: LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper?, context: EntityRendererProvider.Context? ->
            if (entityRenderer is PlayerRenderer) {
                registrationHelper!!.register(HatLayer(entityRenderer))
            }
        })
    }
}