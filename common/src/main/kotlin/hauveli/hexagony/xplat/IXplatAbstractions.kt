package hauveli.hexagony.xplat
import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.xplat.Platform
import com.mojang.authlib.GameProfile
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import java.util.stream.Collectors


/**
 * more like IHexplatAbstracts lmaooooooo
 */
interface IXplatAbstractions {
    fun platform(): Platform?

    fun isModPresent(id: String?): Boolean

    fun isPhysicalClient(): Boolean

    fun initPlatformSpecific()

    fun setBrainsweepAddlData(livingEntity: LivingEntity?, state: Boolean)

    fun isBrainswept(livingEntity: LivingEntity?): Boolean

    fun isBreakingAllowed(world: ServerLevel?, pos: BlockPos?, state: BlockState?, player: Player?): Boolean

    fun isPlacingAllowed(world: ServerLevel?, pos: BlockPos?, blockStack: ItemStack?, player: Player?): Boolean

    var INSTANCE: IXplatAbstractions?
        get() = find()
        set(value) = TODO()

    companion object {
        private fun find(): IXplatAbstractions? {
            val providers = ServiceLoader.load(IXplatAbstractions::class.java).stream().toList()
            if (providers.size != 1) {
                val names = providers.stream()
                    .map { p: ServiceLoader.Provider<IXplatAbstractions?> -> p.type().getName() }.collect(
                        Collectors.joining(",", "[", "]")
                    )
                throw IllegalStateException(
                    "There should be exactly one IXplatAbstractions implementation on the classpath. Found: " + names
                )
            } else {
                val provider = providers.get(0)
                HexAPI.LOGGER.debug("Instantiating xplat impl: " + provider.type().getName())
                return provider.get()
            }
        }

        val HEXCASTING: GameProfile =
            GameProfile(UUID.fromString("8BE7E9DA-1667-11EE-BE56-0242AC120002"), "[HexCasting]")

        /** */
        val INSTANCE: IXplatAbstractions? = find()
    }
}