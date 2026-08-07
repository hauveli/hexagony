package hauveli.hexagony.features.hat

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import hauveli.hexagony.Hexagony
import hauveli.hexagony.registry.HexagonyItems
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.HeadedModel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.ZombieVillager
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import java.util.*
import java.util.function.Consumer


// todo: this doesn't run for some reason, figure out why...
// from
// https://github.com/fonnymunkey/SimpleHats/blob/MultiLoader-1.21.1/common/src/main/java/fonnymunkey/simplehats/client/hat/HatLayer.java
class HatLayer<T : LivingEntity, M>(renderer: RenderLayerParent<T, M>) :
    RenderLayer<T, M>(renderer) where M : EntityModel<T>, M : HeadedModel {

    override fun getParentModel(): M {
        // val modelpart = LivingHatModel.createBodyLayer().bakeRoot() // I don't think I can do anything in this direction.....
        return super.getParentModel()
    }

    override fun render(
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        livingEntity: T?,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTicks: Float,
        age: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        Hexagony.LOGGER.info("hello!!")
        val forceFirstPersonNoRender = true
        //Hacky fix for first person render mods also rendering player layers over the camera
        if (livingEntity === Minecraft.getInstance().cameraEntity
            && Minecraft.getInstance().options.cameraType == CameraType.FIRST_PERSON
            && forceFirstPersonNoRender) return

        Hexagony.LOGGER.info("What...!!")
        Optional.ofNullable<T?>(livingEntity).ifPresent(Consumer { component: T? ->
            if (livingEntity !is Player) {
                val matchingItemStack = livingEntity?.armorSlots
                    ?.find { it.item == HexagonyItems.LIVING_HAT }
                if (matchingItemStack != null && !matchingItemStack.isEmpty) {
                    render(
                        matchingItemStack,
                        poseStack,
                        buffer,
                        packedLight,
                        livingEntity,
                        limbSwing,
                        limbSwingAmount,
                        partialTicks,
                        age,
                        netHeadYaw,
                        headPitch
                    )
                }
            }
        })
    }

    private fun render(
        itemStack: ItemStack,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        livingEntity: T?,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTicks: Float,
        age: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        Hexagony.LOGGER.info("inthething now!!")
        if (!livingEntity!!.isInvisible) {
            poseStack.pushPose()

            val hatOffsetY = 0.2f

            //Slightly scale up to fix some skin layer difference issues
            poseStack.scale(1.01f, 1.01f, 1.01f)
            poseStack.translate(0.0f, 0.0f - hatOffsetY, 0.0f)

            val flag = livingEntity is Villager || livingEntity is ZombieVillager
            if (livingEntity.isBaby && livingEntity !is Villager) {
                poseStack.translate(0.0f, 0.03125f, 0.0f)
                poseStack.scale(1f,1f,1f)
                poseStack.translate(0.0f, 1.0f, 0.0f)
            }

            // if I CAN do it however, most likely getParentModel()
            val modelPart = parentModel
            modelPart.head.translateAndRotate(poseStack)
            translateToHead(poseStack, flag)
            // todo: future self please figure this out somehow
            // if I can't manipulate each modelpart individually, I have all the parts split up like this
            Minecraft.getInstance().entityRenderDispatcher.itemInHandRenderer
                .renderItem(livingEntity, HexagonyItems.LIVING_HAT_A.value.defaultInstance, ItemDisplayContext.HEAD, false, poseStack, buffer, packedLight)
            Minecraft.getInstance().entityRenderDispatcher.itemInHandRenderer
                .renderItem(livingEntity, HexagonyItems.LIVING_HAT_B.value.defaultInstance, ItemDisplayContext.HEAD, false, poseStack, buffer, packedLight)
            Minecraft.getInstance().entityRenderDispatcher.itemInHandRenderer
                .renderItem(livingEntity, HexagonyItems.LIVING_HAT_C.value.defaultInstance, ItemDisplayContext.HEAD, false, poseStack, buffer, packedLight)
            Minecraft.getInstance().entityRenderDispatcher.itemInHandRenderer
                .renderItem(livingEntity, HexagonyItems.LIVING_HAT_D.value.defaultInstance, ItemDisplayContext.HEAD, false, poseStack, buffer, packedLight)

            // I can also try doing each part individually?
            // modelPart.head.getChild("i forgot the names of my cubes but I have to get the above to even run before I can consider this................")

            poseStack.popPose()
        }
        if (livingEntity is Player) {
            val particlesEnabled = true
            val frequency = 300
            val particleType = ParticleTypes.DRIPPING_WATER
            if (particlesEnabled && !Minecraft.getInstance().isPaused && livingEntity.getRandom()
                    .nextFloat() < (if (livingEntity.isInvisible) frequency / 2 else frequency)
            ) {
                val d0 = livingEntity.getRandom().nextGaussian() * 0.02
                val d1 = livingEntity.getRandom().nextGaussian() * 0.02
                val d2 = livingEntity.getRandom().nextGaussian() * 0.02
                val y = livingEntity.randomY
                val particleType: ParticleType<*>? = particleType
                if (particleType is SimpleParticleType) {
                    livingEntity.level().addParticle(
                        particleType,
                        livingEntity.x + livingEntity.getRandom().nextFloat() - 0.5,
                        y,
                        livingEntity.z + livingEntity.getRandom().nextFloat() - 0.5,
                        d0,
                        d1,
                        d2
                    )
                }
            }
        }
    }

    companion object {
        private fun translateToHead(pPoseStack: PoseStack, pIsVillager: Boolean) {
            pPoseStack.translate(0.0f, -0.25f, 0.0f)
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0f))
            pPoseStack.scale(0.625f, -0.625f, -0.625f)
            if (pIsVillager) {
                pPoseStack.translate(0.0f, 0.1875f, 0.0f)
            }
        }
    }
}