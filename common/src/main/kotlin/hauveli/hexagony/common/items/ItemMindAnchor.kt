package hauveli.hexagony.common.items

import at.petrak.hexcasting.annotations.SoftImplement
import at.petrak.hexcasting.common.lib.HexAttributes

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap

import net.minecraft.core.BlockSource
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.DispenserBlock

import org.jetbrains.annotations.Nullable
import java.util.UUID


class ItemMindAnchor(properties: Properties?) : Item(properties), MindContainerItem {

    init {
        DispenserBlock.registerBehavior(this, object : OptionalDispenseItemBehavior() {
            override fun execute(world: BlockSource, stack: ItemStack): ItemStack {
                this.setSuccess(ArmorItem.dispenseArmor(world, stack))
                return stack
            }
        })
    }

    public override fun getDefaultAttributeModifiers(slot: EquipmentSlot?): Multimap <Attribute?, AttributeModifier?>? {
        val out = HashMultimap.create(super.getDefaultAttributeModifiers(slot))
        if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            out.put(HexAttributes.GRID_ZOOM, GRID_ZOOM)
            out.put(HexAttributes.SCRY_SIGHT, SCRY_SIGHT)
        }
        return out
    }

    override fun getMindContainerAttrs(stack: ItemStack?): Multimap<Attribute?, AttributeModifier?> {
        val out: HashMultimap<Attribute?, AttributeModifier?> = HashMultimap.create()
        out.put(HexAttributes.GRID_ZOOM, GRID_ZOOM)
        out.put(HexAttributes.SCRY_SIGHT, SCRY_SIGHT)
        return out
    }

    // In fabric impled with extension property?
    @Nullable
    @SoftImplement("forge")
    fun getEquipmentSlot(stack: ItemStack?): EquipmentSlot {
        return EquipmentSlot.HEAD
    } //    @Nullable
    //    @Override
    //    public SoundEvent getEquipSound() {
    //        return SoundEvents.AMETHYST_BLOCK_CHIME;
    //    }

    companion object {
        // Wearable,
        // The 0.1 is *additive*
        val GRID_ZOOM: AttributeModifier = AttributeModifier(
            UUID.fromString("7c0bb19b-dde3-4da7-bf59-5ee356f0aabc"),
            "Mind Anchor Zoom", 0.5, AttributeModifier.Operation.MULTIPLY_BASE
        )

        val SCRY_SIGHT: AttributeModifier = AttributeModifier(
            UUID.fromString("310b3216-cc7a-4bfc-8b10-74ba1595b2af"),
            "Mind Anchor Sight", 1.0, AttributeModifier.Operation.ADDITION
        )
    }
}