package hauveli.hexagony.features.hat

import at.petrak.hexcasting.annotations.SoftImplement
import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.item.VariantItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.client.model.HexModelLayers
import at.petrak.hexcasting.common.items.ItemLens
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder
import at.petrak.hexcasting.common.lib.HexAttributes
import hauveli.hexagony.Hexagony
import hauveli.hexagony.Hexagony.id
import hauveli.hexagony.features.hat.client.HexagonyLivingHatModel
import hauveli.hexagony.features.hat.client.HexagonyModelLayers
import hauveli.hexagony.registry.HexagonyArmorMaterials
import net.minecraft.client.Minecraft
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.*
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level


class LivingHatItem(properties: Item.Properties?) :
   ItemMediaHolder(properties), Equipable, VariantItem {

    val LIVING_HAT_RESLOC: ResourceKey<Item> =
        ResourceKey.create(
            Registries.ITEM,
            id("living_hat")
        )

    fun getDefense(): Int {
        // should depend on remaining media
        return 555
    }

    override fun onUseTick(level: Level, p1: LivingEntity, itemStack: ItemStack, p3: Int) {
        super.onUseTick(level, p1, itemStack, p3)

        this.setMedia(itemStack, MediaConstants.DUST_UNIT * 420)
        val enchantments = level.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
        itemStack.enchant(
            enchantments.getOrThrow(Enchantments.BINDING_CURSE),
            1
        )
    }

    override fun inventoryTick(p0: ItemStack, p1: Level, p2: Entity, p3: Int, p4: Boolean) {
        super.inventoryTick(p0, p1, p2, p3, p4)
    }

    override fun getMaxMedia(p0: ItemStack?): Long {
        return MediaConstants.DUST_UNIT * 666 // super.getMaxMedia(p0)
    }

    override fun canProvideMedia(p0: ItemStack?): Boolean {
        return true
    }

    override fun canRecharge(p0: ItemStack?): Boolean {
        return false
    }

    override fun getEquipmentSlot(): EquipmentSlot {
        return EquipmentSlot.HEAD
    }

    override fun getEquipSound(): Holder<SoundEvent?> {
        return SoundEvents.ARMOR_EQUIP_LEATHER
    }



    // from: https://github.com/FallingColors/HexMod/blob/make-the-armor-work/Common/src/main/java/at/petrak/hexcasting/common/items/armor/ItemRobes.java
    /**
     * To get the armor model in;
     * On forge: client item extension in ForgeHexClientInitializer (line 161)
     * On fabric: custom HexRobesRenderer set up from FabricHexClientInitializer (line 60)
     */
    private var models: Array<HexagonyLivingHatModel?>? = arrayOf()

    /*
    val type: Type? = null
    fun ItemRobes(type: Type, properties: Properties) {
        super(HexagonyArmorMaterials.LIVING, type, properties)
        this.type = type
    }
     */

    val armorModels: Array<HexagonyLivingHatModel?>?
        get() {
            if (models == null)
                models = provideArmorModelsForSlot(equipmentSlot)
            return models
        }

    /*
    @SoftImplement("IItemExtension")
    override fun getArmorTexture(
        stack: ItemStack,
        entity: Entity,
        slot: EquipmentSlot,
        layer: ArmorMaterial.Layer,
        innerModel: Boolean
    ): ResourceLocation {
        return id("textures/armor/robes" + getVariant(stack) + ".png")
    }
     */

    override fun getName(pStack: ItemStack): Component {
        val descID = this.getDescriptionId(pStack)
        val robesItem: LivingHatItem = pStack.item as LivingHatItem
        return Component.translatable(descID + "." + getVariant(pStack))
    }

    override fun numVariants(): Int {
        return 3
    }

    companion object {
        var HOOD_MODIFIERS: ItemAttributeModifiers = ItemAttributeModifiers.builder()
            .add(HexAttributes.SCRY_SIGHT, ItemLens.SCRY_SIGHT, EquipmentSlotGroup.HEAD)
            .add(HexAttributes.GRID_ZOOM, ItemLens.GRID_ZOOM, EquipmentSlotGroup.HEAD)
            .add(
                Attributes.ARMOR, AttributeModifier(
                    HexAPI.modLoc("robes_hood_armor"), 3.0, AttributeModifier.Operation.ADD_VALUE
                ), EquipmentSlotGroup.HEAD
            )
            .build()

        fun provideArmorModelsForSlot(slot: EquipmentSlot): Array<HexagonyLivingHatModel?> {
            val models = Minecraft.getInstance().entityModels
            return arrayOf(
                HexagonyLivingHatModel(models.bakeLayer(HexagonyModelLayers.LIVING_HAT_0), slot),
            )
        }
    }
}