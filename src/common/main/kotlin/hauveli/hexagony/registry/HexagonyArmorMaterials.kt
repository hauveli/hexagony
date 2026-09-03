package hauveli.hexagony.registry
import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.xplat.IXplatAbstractions
import at.petrak.hexcasting.xplat.IXplatRegister
import hauveli.hexagony.Hexagony
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.ArmorMaterials
import net.minecraft.world.item.crafting.Ingredient

// https://github.com/FallingColors/HexMod/blob/make-the-armor-work/Common/src/main/java/at/petrak/hexcasting/common/lib/HexArmorMaterials.java
object HexagonyArmorMaterials {
    private val REGISTER: IXplatRegister<ArmorMaterial?> =
        IXplatAbstractions.INSTANCE.createRegistar(Registries.ARMOR_MATERIAL)

    fun register() {
        REGISTER.registerAll()
    }

    val LIVING: Holder<ArmorMaterial?>? = REGISTER.registerHolder<ArmorMaterial?>("living", {
        ArmorMaterial(
            mapOf(),  // no defense here since it's specified with the other attributes in ItemRobes
            ArmorMaterials.GOLD.value().enchantmentValue(),
            SoundEvents.ARMOR_EQUIP_LEATHER,
            { Ingredient.of(HexagonyItems.LIVING_HAT.value) },
            listOf(ArmorMaterial.Layer(Hexagony.id("living"))),
            3f, 0f // 3 toughness, 0 knockback res
        )
    })
}