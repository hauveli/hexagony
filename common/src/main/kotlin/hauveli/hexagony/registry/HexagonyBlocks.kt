package hauveli.hexagony.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.blocks.circles.BlockEmptyMindAnchor
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockRedstoneImpetus
import com.mojang.authlib.properties.Property
import hauveli.hexagony.Hexagony
import hauveli.hexagony.common.blocks.BlockFullMindAnchor
import hauveli.hexagony.common.lib.PropertyBlock
import io.netty.util.collection.ByteCollections.emptyMap
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.material.MapColor

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

