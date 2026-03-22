package hauveli.hexagony.registry

import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.item.DyeColor

object HexagonyBlockProperties {

    val FILLED: BooleanProperty = BooleanProperty.create("filled")

    val COLOR by lazy { EnumProperty.create("color", DyeColor::class.java) }

    val POWER by lazy { IntegerProperty.create("power", 0, 3) }
}