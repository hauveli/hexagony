package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import hauveli.hexagony.common.blocks.BlockEmptyMindAnchor
import hauveli.hexagony.common.blocks.BlockFullMindAnchor
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.material.MapColor
import java.util.function.ToIntFunction

object HexagonyBlocks : HexagonyRegistrar<Block>(
    BuiltInRegistries.BLOCK.key() as ResourceKey<Registry<Block>>,
    { BuiltInRegistries.BLOCK }
) {
    // Declare your property somewhere early
    val FILLED: BooleanProperty = BooleanProperty.create("filled")

    val MIND_ANCHOR_EMPTY = make("mind_anchor/empty") {
        BlockEmptyMindAnchor(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(0.2f, 100.0f)
                .sound(SoundType.AMETHYST)
                .lightLevel(ToIntFunction { i: BlockState? -> 5 })
                .emissiveRendering(BlockBehaviour.StatePredicate { state: BlockState?, level: BlockGetter?, pos: BlockPos? -> true })
        )
    }

    val MIND_ANCHOR_FULL = make("mind_anchor/full") {
        BlockFullMindAnchor(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(0.2f, 360000000.0f)
                .sound(SoundType.AMETHYST)
        )
    }

    private fun <T : Block> make(name: String, builder: () -> T): HexagonyRegistrar<Block>.Entry<T> =
        register(Hexagony.id(name), builder)
}

