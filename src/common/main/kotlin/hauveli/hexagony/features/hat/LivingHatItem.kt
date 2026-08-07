package hauveli.hexagony.features.hat

import at.petrak.hexcasting.api.item.MediaHolderItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder
import hauveli.hexagony.Hexagony.id
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterials
import net.minecraft.world.item.EnchantedBookItem
import net.minecraft.world.item.Equipable
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentInstance
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level


class LivingHatItem(properties: Item.Properties?) :
   ItemMediaHolder(properties), Equipable {

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
}