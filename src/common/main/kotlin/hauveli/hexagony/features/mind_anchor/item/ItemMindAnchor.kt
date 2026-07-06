package hauveli.hexagony.features.mind_anchor.item

import at.petrak.hexcasting.annotations.SoftImplement
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus.TAG_MEDIA
import at.petrak.hexcasting.api.utils.hasCompound
import at.petrak.hexcasting.common.items.ItemLens.GRID_ZOOM
import at.petrak.hexcasting.common.items.ItemLens.SCRY_SIGHT
import at.petrak.hexcasting.common.lib.HexAttributes

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DispenserBlock

import org.jetbrains.annotations.Nullable
import java.util.UUID


class ItemMindAnchor(block: Block?, properties: Properties) : BlockItem(block as Block, properties), MindContainerItem {



    override fun getMindContainerAttrs(stack: ItemStack?): Multimap<Attribute?, AttributeModifier?> {
        val out: HashMultimap<Attribute?, AttributeModifier?> = HashMultimap.create()
        out.put(HexAttributes.GRID_ZOOM.value(), GRID_ZOOM)
        out.put(HexAttributes.SCRY_SIGHT.value(), SCRY_SIGHT)
        return out
    }


    // because I do not want to deal with bundle/shulker shenanigans...
    override fun canFitInsideContainerItems(): Boolean {
        return false

    }
    /*

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        super.inventoryTick(stack, level, entity, slotId, isSelected)
        val server = level.server
        val tag = stack.tag
        if (server != null &&
            tag != null &&
            tag.hasCompound("BlockEntityTag")) { // thank god this is a method in ItemMindAnchor so I only have to check this
            val compound = tag.getCompound("BlockEntityTag")
            MindAnchorManager.trackItemStack(
                server,
                compound.getUUID(TAG_STORED_PLAYER),
                entity,
                stack
            )
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        level: Level?,
        tooltip: MutableList<Component>,
        flag: TooltipFlag
    ) {
        val tag = stack.tag ?: return

        tooltip.add(
            Component.literal("Hello! Testing this to see how it works! ${tag}")
                .withStyle(ChatFormatting.GRAY)
        )

        if (tag.contains(TAG_STORED_PLAYER)) {
            tooltip.add(
                Component.literal("Owner: ${tag.getString(TAG_STORED_PLAYER)}")
                    .withStyle(ChatFormatting.GRAY)
            )
        }

        if (tag.contains(TAG_MEDIA)) {
            tooltip.add(
                Component.literal("Media: ${tag.getInt(TAG_MEDIA)}")
                    .withStyle(ChatFormatting.AQUA)
            )
        }
    }

    // dont need this
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
    */
}