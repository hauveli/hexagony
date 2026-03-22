package hauveli.hexagony.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import hauveli.hexagony.Hexagony
import hauveli.hexagony.common.lib.PropertyBlock
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.material.MapColor

object HexagonyBlocks : HexagonyRegistrar<Block>(
    BuiltInRegistries.BLOCK.key() as ResourceKey<Registry<Block>>,
    { BuiltInRegistries.BLOCK }
) {
    // Declare your property somewhere early
    val FILLED: BooleanProperty = BooleanProperty.create("filled")

    val MIND_ANCHOR = make("mind_anchor/empty") {
        Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.0f, 6.0f)
                .sound(SoundType.AMETHYST)
                .requiresCorrectToolForDrops()
        )
    }

    private fun <T : Block> make(name: String, builder: () -> T): HexagonyRegistrar<Block>.Entry<T> =
        register(Hexagony.id(name), builder)
}

