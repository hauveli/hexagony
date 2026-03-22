package hauveli.hexagony.common.blocks

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.utils.putTag
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus
import at.petrak.hexcasting.common.lib.HexBlockEntities
import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Pair
import hauveli.hexagony.common.blocks.BlockFullMindAnchor.Companion.POWERED
import hauveli.hexagony.registry.HexagonyBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import java.util.*

open class BlockEntityFullMindAnchor(pWorldPosition: BlockPos, pBlockState: BlockState?) :
    BlockEntityRedstoneImpetus( pWorldPosition, pBlockState
) {

    fun getStoredUUID(): UUID? {
        return storedPlayer?.uuid
    }

    override fun getStoredPlayer(): ServerPlayer? {
        return storedPlayer
    }

}