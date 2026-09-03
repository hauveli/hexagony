package hauveli.hexagony.features.hat.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.HumanoidArmorModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import javax.annotation.Nonnull

// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

class HexagonyLivingHatModel (
    root: ModelPart, // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into
    // this model's constructor
    // public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(modLoc("robes"), "main");
    val slot: EquipmentSlot
) : HumanoidArmorModel<LivingEntity?>(root) {
    private var rooot: ModelPart = root.getChild("rooot")
    private var hat2: ModelPart = this.rooot.getChild("hat")
    private var simplifyLogic: ModelPart = this.hat2.getChild("simplify_logic2")
    private var base: ModelPart = this.simplifyLogic.getChild("base")
    private var middle: ModelPart = this.base.getChild("middle")
    private var tail2: ModelPart = this.middle.getChild("tail2")

    override fun renderToBuffer(ms: PoseStack, buffer: VertexConsumer, light: Int, overlay: Int, color: Int) {
        renderArmorPart(slot)
        super.renderToBuffer(ms, buffer, light, overlay, color)
    }

    private fun renderArmorPart(slot: EquipmentSlot) {
        setAllVisible(false)

        rooot.visible = false

        when (slot) {
            EquipmentSlot.HEAD -> {
                rooot.visible = true
                hat2.visible = true
                simplifyLogic.visible = true
                base.visible = true
                middle.visible = true
                tail2.visible = true
            }

            else -> {}
        }
    }

    companion object {

        fun variant0(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.root

            val rooot =
                partdefinition.addOrReplaceChild("rooot", CubeListBuilder.create(), PartPose.offset(0.0f, 24.0f, 0.0f))

            val hat = rooot.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, -13.0f, 0.0f))

            val simplify_logic2 = hat.addOrReplaceChild(
                "simplify_logic2",
                CubeListBuilder.create().texOffs(0, 20).mirror()
                    .addBox(-8.0f, -1.0f, -8.0f, 16.0f, 1.0f, 16.0f, CubeDeformation(0.0f)).mirror(false)
                    .texOffs(0, 0).mirror().addBox(-8.0f, 0.0f, -8.0f, 16.0f, 4.0f, 16.0f, CubeDeformation(0.0f))
                    .mirror(false)
                    .texOffs(0, 37).mirror().addBox(-4.0f, -4.0f, -4.0f, 8.0f, 3.0f, 8.0f, CubeDeformation(0.0f))
                    .mirror(false),
                PartPose.offset(0.0f, 0.0f, 0.0f)
            )

            val base =
                simplify_logic2.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f))

            val cone1_r1 = base.addOrReplaceChild(
                "cone1_r1",
                CubeListBuilder.create().texOffs(0, 48).mirror()
                    .addBox(-3.0542f, -4.2905f, -3.0542f, 6.0f, 5.0f, 6.0f, CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, -3.0f, 0.0f, -0.2742f, 0.0381f, 0.2742f)
            )

            val middle = base.addOrReplaceChild("middle", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f))

            val cone2_r1 = middle.addOrReplaceChild(
                "cone2_r1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                    .addBox(-1.0825f, -4.9116f, -1.0825f, 4.0f, 5.0f, 4.0f, CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.02f, -7.0f, 0.02f, -0.5299f, 0.147f, 0.5299f)
            )

            val tail2 = middle.addOrReplaceChild("tail2", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f))

            val cone3_r1 = tail2.addOrReplaceChild(
                "cone3_r1",
                CubeListBuilder.create().texOffs(56, 0).mirror()
                    .addBox(-0.6826f, -6.8133f, -0.6826f, 2.0f, 7.0f, 2.0f, CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(2.1387f, -9.9972f, 2.1387f, -1.0275f, 0.6165f, 1.0275f)
            )

            return LayerDefinition.create(meshdefinition, 64, 64)
        }
    }


}