package hauveli.hexagony.features.hat

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import hauveli.hexagony.Hexagony.id
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.world.entity.Entity
import javax.annotation.Nonnull


class LivingHatModel<T : Entity>(root: ModelPart) : EntityModel<T>() {
    // why can't I just import a .json for this........
    private var rooot: ModelPart
    private var hat: ModelPart
    private var simplify_logic2: ModelPart
    private var base: ModelPart
    private var middle: ModelPart
    private var tail2: ModelPart
    init {

        this.rooot = root.getChild("rooot");
        this.hat = this.rooot.getChild("hat");
        this.simplify_logic2 = this.hat.getChild("simplify_logic2");
        this.base = this.simplify_logic2.getChild("base");
        this.middle = this.base.getChild("middle");
        this.tail2 = this.middle.getChild("tail2");
    }

    override fun renderToBuffer(
        @Nonnull poseStack: PoseStack,
        @Nonnull buffer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        rooot.render(poseStack, buffer, packedLight, packedOverlay, color)
    }

    override fun setupAnim(t: T?, v: Float, v1: Float, v2: Float, v3: Float, v4: Float) {
    }

    companion object {
        // So that I can re-remember that this is what the first argument in "model layer location" is meant to be
        private val TEXTURE = id("textures/item/living_hat.png")

        // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            TEXTURE,
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.getRoot()

            val rooot =
                partdefinition.addOrReplaceChild("rooot", CubeListBuilder.create(), PartPose.offset(0.0f, 24.0f, 0.0f))

            val hat = rooot.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, -13.0f, 0.0f))

            val simplify_logic2 = hat.addOrReplaceChild(
                "simplify_logic2",
                CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-8.0f, -1.0f, -8.0f, 16.0f, 1.0f, 16.0f, CubeDeformation(0.0f))
                    .texOffs(0, 17).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 3.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.7854f, 0.0f)
            )

            val base =
                simplify_logic2.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f))

            val cone1_r1 = base.addOrReplaceChild(
                "cone1_r1",
                CubeListBuilder.create().texOffs(0, 28)
                    .addBox(-3.0542f, -4.2905f, -3.0542f, 6.0f, 5.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -3.0f, 0.0f, -0.2742f, 0.0381f, 0.2742f)
            )

            val middle = base.addOrReplaceChild("middle", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f))

            val cone2_r1 = middle.addOrReplaceChild(
                "cone2_r1",
                CubeListBuilder.create().texOffs(24, 37)
                    .addBox(-1.0825f, -4.9116f, -1.0825f, 4.0f, 5.0f, 4.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.02f, -7.0f, 0.02f, -0.5299f, 0.147f, 0.5299f)
            )

            val tail2 = middle.addOrReplaceChild("tail2", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f))

            val cone3_r1 = tail2.addOrReplaceChild(
                "cone3_r1",
                CubeListBuilder.create().texOffs(24, 46)
                    .addBox(-0.6826f, -6.8133f, -0.6826f, 2.0f, 7.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(2.1387f, -9.9972f, 2.1387f, -1.0275f, 0.6165f, 1.0275f)
            )

            return LayerDefinition.create(meshdefinition, 64, 64)
        }
    }
}