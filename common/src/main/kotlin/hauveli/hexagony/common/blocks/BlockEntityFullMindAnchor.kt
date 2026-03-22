package hauveli.hexagony.common.blocks

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.utils.putTag
import at.petrak.hexcasting.common.lib.HexBlockEntities
import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Pair
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
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class BlockEntityFullMindAnchor(pWorldPosition: BlockPos, pBlockState: BlockState?) : BlockEntityAbstractImpetus(
    HexagonyBlockEntities.MIND_ANCHOR.value, pWorldPosition, pBlockState
) {
    private var storedPlayerProfile: GameProfile? = null
    private var storedPlayer: UUID? = null

    private var cachedDisplayProfile: GameProfile? = null
    private var cachedDisplayStack: ItemStack? = null

    protected val playerName: GameProfile?
        get() {
            if (this.level is ServerLevel) {
                val player: Player? = getStoredPlayer()
                if (player != null) {
                    return player.getGameProfile()
                }
            }

            return this.storedPlayerProfile
        }

    fun setPlayer(profile: GameProfile?, player: UUID?) {
        this.storedPlayerProfile = profile
        this.storedPlayer = player
        this.setChanged()
    }

    fun clearPlayer() {
        this.storedPlayerProfile = null
        this.storedPlayer = null
    }

    fun updatePlayerProfile() {
        val player = getStoredPlayer()
        if (player != null) {
            val newProfile = player.getGameProfile()
            if (newProfile != this.storedPlayerProfile) {
                this.storedPlayerProfile = newProfile
                this.setChanged()
            }
        }
    }

    // just feels wrong to use the protected method
    fun getStoredPlayer(): ServerPlayer? {
        if (this.storedPlayer == null) {
            return null
        }
        if (this.level !is ServerLevel) {
            HexAPI.LOGGER.error("Called getStoredPlayer on the client")
            return null
        }
        val e = (level as ServerLevel).getEntity(this.storedPlayer)
        if (e is ServerPlayer) {
            return e
        } else {
            // if owner is offline then getEntity will return null
            // if e is somehow neither null nor a player, something is very wrong
            if (e != null) {
                HexAPI.LOGGER.error("Entity {} stored in a cleric impetus wasn't a player somehow", e)
            }
            return null
        }
    }

    override fun applyScryingLensOverlay(
        lines: MutableList<Pair<ItemStack?, Component?>?>,
        state: BlockState?, pos: BlockPos?, observer: Player?,
        world: Level?,
        hitFace: Direction?
    ) {
        super.applyScryingLensOverlay(lines, state, pos, observer, world, hitFace)

        val name = this.playerName
        if (name != null) {
            if (name != cachedDisplayProfile || cachedDisplayStack == null) {
                cachedDisplayProfile = name
                val head = ItemStack(Items.PLAYER_HEAD)
                head.putTag("SkullOwner", NbtUtils.writeGameProfile(CompoundTag(), name))
                head.getItem().verifyTagAfterLoad(head.getOrCreateTag())
                cachedDisplayStack = head
            }
            lines.add(
                Pair<ItemStack?, Component?>(
                    cachedDisplayStack,
                    Component.translatable("hexcasting.tooltip.lens.impetus.redstone.bound", name.getName())
                )
            )
        } else {
            lines.add(
                Pair<ItemStack?, Component?>(
                    ItemStack(Items.BARRIER),
                    Component.translatable("hexcasting.tooltip.lens.impetus.redstone.bound.none")
                )
            )
        }
    }

    override fun saveModData(tag: CompoundTag) {
        super.saveModData(tag)
        if (this.storedPlayer != null) {
            tag.putUUID(TAG_STORED_PLAYER, this.storedPlayer)
        }
        if (this.storedPlayerProfile != null) {
            tag.put(TAG_STORED_PLAYER_PROFILE, NbtUtils.writeGameProfile(CompoundTag(), storedPlayerProfile))
        }
    }

    override fun loadModData(tag: CompoundTag) {
        super.loadModData(tag)
        if (tag.contains(TAG_STORED_PLAYER, Tag.TAG_INT_ARRAY.toInt())) {
            this.storedPlayer = tag.getUUID(TAG_STORED_PLAYER)
        } else {
            this.storedPlayer = null
        }
        if (tag.contains(TAG_STORED_PLAYER_PROFILE, Tag.TAG_COMPOUND.toInt())) {
            this.storedPlayerProfile = NbtUtils.readGameProfile(tag.getCompound(TAG_STORED_PLAYER_PROFILE))
        } else {
            this.storedPlayerProfile = null
        }
    }

    companion object {
        const val TAG_STORED_PLAYER: String = "stored_player"
        const val TAG_STORED_PLAYER_PROFILE: String = "stored_player_profile"
    }
}