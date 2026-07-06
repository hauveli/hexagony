package hauveli.hexagony.features.mind_anchor.blocks

import at.petrak.hexcasting.api.block.HexBlockEntity
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.contains
import at.petrak.hexcasting.api.utils.extractMedia
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.lib.HexItems
import com.mojang.authlib.GameProfile
import hauveli.hexagony.registry.HexagonyBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import org.jetbrains.annotations.Contract
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

// https://github.com/FallingColors/HexMod/blob/532fe9a60138544112e096812c7aefb78b3d7364/Common/src/main/java/at/petrak/hexcasting/common/blocks/circles/impetuses/BlockEntityRedstoneImpetus.java
/**
 * Default impl for an impetus, not tecnically necessary but I'm exposing it for ease of use
 *
 *
 * This does assume a great deal so you might have to re-implement a lot of this yourself if you
 * wanna do something wild and new
 */
class BlockEntityFullMindAnchor(
    pWorldPosition: BlockPos,
    pBlockState: BlockState?
) : HexBlockEntity(HexagonyBlockEntities.MIND_ANCHOR.value, pWorldPosition, pBlockState), WorldlyContainer {
    // We might try to load the executor in loadModData when the level doesn't exist yet,
    // so save the tag and load it lazy
    // todo: there was an idea to make this behave like an impetus, and I still kind of wanna do that, but I'm not sure now...
    var lazyExecutionState: CompoundTag? = null
    protected var executionState: CircleExecutionState? = null

    protected var storedPlayerProfile: GameProfile? = null
    protected var storedPlayer: UUID? = null
    protected var graftUUID: UUID? = null

    protected var cachedDisplayProfile: GameProfile? = null
    protected var cachedDisplayStack: ItemStack? = null

    var media: Long = 0

    // these are null together
    var displayMsg: Component? = null
        protected set
    protected var displayItem: ItemStack? = null
    protected var pigment: FrozenPigment? = null

    fun getPlayerUuid() : UUID? {
        return this.storedPlayer
    }

    fun extractMediaFromInsertedItem(stack: ItemStack, simulate: Boolean): Long {
        if (this.media < 0) {
            return 0
        }
        return extractMedia(stack, remainingMediaCapacity(), true, simulate)
    }

    fun insertMedia(stack: ItemStack) {
        if (this.media >= 0 && !stack.isEmpty() && stack.getItem() === HexItems.CREATIVE_UNLOCKER) {
            setInfiniteMedia()
            stack.shrink(1)
        } else {
            val mediamount = extractMediaFromInsertedItem(stack, false)
            if (mediamount > 0) {
                this.media = min(mediamount + media, MAX_CAPACITY)
                this.sync()
            }
        }
    }

    fun setInfiniteMedia() {
        this.media = -1
        this.sync()
    }

    fun remainingMediaCapacity(): Long {
        if (this.media < 0) {
            return 0
        }
        return max(0, MAX_CAPACITY - this.media)
    }

    //endregion
    fun setPigment(pigment: FrozenPigment?): FrozenPigment? {
        this.pigment = pigment
        return this.pigment
    }
    override fun saveModData(tag: CompoundTag, lookup: HolderLookup.Provider) {
        executionState?.let {
            tag.put(TAG_EXECUTION_STATE, it.save())
        }

        if (displayMsg != null && displayItem != null) {

            tag.putString(
                TAG_ERROR_MSG,
                ComponentSerialization.CODEC.encodeStart(
                    NbtOps.INSTANCE,
                    displayMsg
                ).result().get().asString
            )

            displayItem!!.save(lookup)
            val itemTag = CompoundTag()
            tag.put(TAG_ERROR_DISPLAY, itemTag)
        }

        helperApplyNbt(tag)
    }


    // this blows
    fun CompoundTag.readGameProfile(tagName: String): GameProfile {
        val tag = getCompound(tagName)

        val uuid = if (tag.hasUUID("Id")) {
            tag.getUUID("Id")
        } else {
            UUID(tag.getLong("IdMost"), tag.getLong("IdLeast"))
        }

        val name = if (tag.contains("Name", 8)) tag.getString("Name") else null

        return GameProfile(uuid, name)
    }


    override fun loadModData(tag: CompoundTag, lookup: HolderLookup.Provider) {
        this.executionState = null
        if (tag.contains(TAG_EXECUTION_STATE, Tag.TAG_COMPOUND.toInt())) {
            this.lazyExecutionState = tag.getCompound(TAG_EXECUTION_STATE)
        } else {
            this.lazyExecutionState = null
        }

        if (tag.contains(TAG_MEDIA, Tag.TAG_LONG.toInt())) {
            this.media = tag.getLong(TAG_MEDIA)
        }

        if (tag.contains(TAG_ERROR_MSG, Tag.TAG_STRING.toInt()) && tag.contains(
                TAG_ERROR_DISPLAY,
                Tag.TAG_COMPOUND.toInt()
            )
        ) {
            val msg = ComponentSerialization.CODEC
                .parse(lookup.createSerializationContext(NbtOps.INSTANCE), tag.get(TAG_ERROR_MSG))
                .result()
                .orElse(null)

            val display = ItemStack.parse(lookup, tag.getCompound(TAG_ERROR_DISPLAY)).get()
            this.displayMsg = msg
            this.displayItem = display
        } else {
            this.displayMsg = null
            this.displayItem = null
        }
        if (tag.contains(TAG_PIGMENT, Tag.TAG_COMPOUND.toInt())) this.pigment =
            FrozenPigment.ANCIENT.get() //  (tag.getCompound(TAG_PIGMENT))

        if (tag.contains(TAG_STORED_PLAYER, Tag.TAG_INT_ARRAY)) {
            this.storedPlayer = tag.getUUID(TAG_STORED_PLAYER);
        } else {
            this.storedPlayer = null;
        }
        if (tag.contains(TAG_STORED_PLAYER_PROFILE, Tag.TAG_COMPOUND)) {
            this.storedPlayerProfile = tag.readGameProfile(TAG_STORED_PLAYER_PROFILE)
        } else {
            this.storedPlayerProfile = null;
        }
    }

    override fun getSlotsForFace(var1: Direction): IntArray {
        return SLOTS
    }

    override fun canPlaceItemThroughFace(index: Int, stack: ItemStack, dir: Direction?): Boolean {
        return this.canPlaceItem(index, stack)
    }

    override fun canTakeItemThroughFace(var1: Int, var2: ItemStack, var3: Direction): Boolean {
        return false
    }

    override fun getContainerSize(): Int {
        return 1
    }

    override fun isEmpty(): Boolean {
        return true
    }

    override fun getItem(index: Int): ItemStack {
        return ItemStack.EMPTY.copy()
    }

    override fun removeItem(index: Int, count: Int): ItemStack {
        return ItemStack.EMPTY.copy()
    }

    override fun removeItemNoUpdate(index: Int): ItemStack {
        return ItemStack.EMPTY.copy()
    }

    override fun setItem(index: Int, stack: ItemStack) {
        insertMedia(stack)
    }

    override fun stillValid(player: Player): Boolean {
        return false
    }

    override fun clearContent() {
        // NO-OP
    }

    override fun canPlaceItem(index: Int, stack: ItemStack): Boolean {
        if (remainingMediaCapacity() == 0L) {
            return false
        }

        if (stack.`is`(HexItems.CREATIVE_UNLOCKER)) {
            return true
        }

        val mediamount = extractMediaFromInsertedItem(stack, true)
        return mediamount > 0
    }


    fun setPlayer(gameProfile: GameProfile, uuid: UUID, graftUuid: UUID) {
        this.storedPlayerProfile = gameProfile
        this.storedPlayer = uuid
        this.graftUUID = graftUuid
    }

    fun helperApplyNbt(nbt: CompoundTag) : CompoundTag {
        val playerNameNBT = CompoundTag()

        val profile = this.storedPlayerProfile
        if (profile != null && profile.name is String)
            playerNameNBT.putString("Name", profile.name)

        val uuid = this.storedPlayer
        if (uuid != null) {
            playerNameNBT.putIntArray("Id", UUIDUtil.uuidToIntArray(uuid) )
            nbt.putUUID(TAG_STORED_PLAYER, uuid)
        }
        nbt.putCompound(
            TAG_STORED_PLAYER_PROFILE,
            value = playerNameNBT
        )

        nbt.putLong(BlockEntityAbstractImpetus.TAG_MEDIA, this.media)

        //val nbt_hover = itemStack.getOrCreateCompound("Hover")
        return nbt
    }

    companion object {

        private val DUST_AMOUNT = DecimalFormat("###,###.##")
        private const val MAX_CAPACITY = 9000000000000000000L

        const val TAG_EXECUTION_STATE: String = "executor"
        const val TAG_MEDIA: String = "media"
        const val TAG_ERROR_MSG: String = "errorMsg"
        const val TAG_ERROR_DISPLAY: String = "errorDisplay"
        const val TAG_PIGMENT: String = "pigment"

        const val TAG_STORED_PLAYER = "stored_player"
        const val TAG_STORED_PLAYER_PROFILE = "stored_player_profile"

        @Contract(pure = true)
        protected fun getBounds(poses: MutableList<BlockPos>): AABB {
            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var minZ = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE
            var maxZ = Int.MIN_VALUE

            for (pos in poses) {
                if (pos.getX() < minX) {
                    minX = pos.getX()
                }
                if (pos.getY() < minY) {
                    minY = pos.getY()
                }
                if (pos.getZ() < minZ) {
                    minZ = pos.getZ()
                }
                if (pos.getX() > maxX) {
                    maxX = pos.getX()
                }
                if (pos.getY() > maxY) {
                    maxY = pos.getY()
                }
                if (pos.getZ() > maxZ) {
                    maxZ = pos.getZ()
                }
            }

            return AABB(
                minX.toDouble(),
                minY.toDouble(),
                minZ.toDouble(),
                (maxX + 1).toDouble(),
                (maxY + 1).toDouble(),
                (maxZ + 1).toDouble()
            )
        }

        // this is a good use of my time
        private val MAJOR_SCALE = intArrayOf(0, 2, 4, 5, 7, 9, 11, 12)
        private val MINOR_SCALE = intArrayOf(0, 2, 3, 5, 7, 8, 11, 12)
        private val DORIAN_SCALE = intArrayOf(0, 2, 3, 5, 7, 9, 10, 12)
        private val MIXOLYDIAN_SCALE = intArrayOf(0, 2, 4, 5, 7, 9, 10, 12)
        private val BLUES_SCALE = intArrayOf(0, 3, 5, 6, 7, 10, 12)
        private val BAD_TIME = intArrayOf(0, 0, 12, 7, 6, 5, 3, 0, 3, 5)
        private val SUSSY_BAKA = intArrayOf(5, 8, 10, 11, 10, 8, 5, 3, 7, 5)

        //endregion
        //region item handler contract stuff
        private val SLOTS = intArrayOf(0)
    }
}