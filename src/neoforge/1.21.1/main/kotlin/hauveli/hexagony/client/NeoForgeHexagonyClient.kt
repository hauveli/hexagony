package hauveli.hexagony.client

import at.petrak.hexcasting.client.model.HexModelLayers
import hauveli.hexagony.Hexagony
import hauveli.hexagony.Hexagony.MODID
import hauveli.hexagony.features.hat.LivingHatItem
import hauveli.hexagony.features.hat.client.HexagonyLivingHatModel
import hauveli.hexagony.features.hat.client.HexagonyModelLayers
import hauveli.hexagony.registry.HexagonyItems
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent
import java.util.function.BiConsumer
import java.util.function.Supplier


@EventBusSubscriber(modid = MODID)
object NeoForgeHexagonyClient {
    @Suppress("UNUSED_PARAMETER")
    fun init(event: FMLClientSetupEvent) {
        HexagonyClient.init()
    }

    /*
    @JvmStatic
    @SubscribeEvent
    fun addEntityLayers(event: EntityRenderersEvent.AddLayers) {
        val playerRenderer = event.getSkin<EntityRenderer<out Player>>(PlayerSkin.Model.WIDE)
        if (playerRenderer is PlayerRenderer) {
            playerRenderer.addLayer(HatLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>(playerRenderer))
        }
        val playerRendererWide = event.getSkin<EntityRenderer<out Player>>(PlayerSkin.Model.WIDE)
        if (playerRendererWide is PlayerRenderer) {
            playerRendererWide.addLayer(HatLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>(playerRendererWide))
        }
    }

     */


    // https://github.com/FallingColors/HexMod/blob/make-the-armor-work/Neoforge/src/main/java/at/petrak/hexcasting/forge/ForgeHexClientInitializer.java
    @JvmStatic
    @SubscribeEvent
    fun registerArmorRenderer(evt: RegisterClientExtensionsEvent) {
        Hexagony.LOGGER.info("logging: method set up at least...")
        evt.registerItem(object : IClientItemExtensions {
            //private final Map<EquipmentSlot, HumanoidModel<LivingEntity>[]> MODEL_SETS = new Object2ObjectArrayMap<>();
            override fun getHumanoidArmorModel(
                livingEntity: LivingEntity,
                itemStack: ItemStack,
                equipmentSlot: EquipmentSlot,
                original: HumanoidModel<*>
            ): HexagonyLivingHatModel? {
                val armor = itemStack.item as LivingHatItem
                Hexagony.LOGGER.info("logging: armor")
                Hexagony.LOGGER.info(armor)
                Hexagony.LOGGER.info("logging done")
                return armor.armorModels!![armor.getVariant(itemStack)]

                //                var variant = itemStack.get(HexDataComponents.ITEM_VARIANT.get());
//                var modelSet = MODEL_SETS.computeIfAbsent(equipmentSlot, ItemRobes::provideArmorModelsForSlot);
//                return variant != null ? modelSet[variant] : modelSet[0];
            }
        }, HexagonyItems.LIVING_HAT.value)
    }


    @JvmStatic
    @SubscribeEvent
    fun registerEntityLayers(evt: EntityRenderersEvent.RegisterLayerDefinitions) {
        Hexagony.LOGGER.info("logging: armor layers initted")
        HexagonyModelLayers.init({
            layerLocation: ModelLayerLocation, supplier: Supplier<LayerDefinition> ->
            evt.registerLayerDefinition(layerLocation, supplier)
        })
    }
}