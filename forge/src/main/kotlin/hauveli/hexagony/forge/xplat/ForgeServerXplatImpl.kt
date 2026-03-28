package hauveli.hexagony.forge.xplat

//import at.petrak.hexcasting.fabric.cc.CCBrainswept

import at.petrak.hexcasting.forge.interop.curios.CuriosApiInterop
import at.petrak.hexcasting.forge.network.ForgePacketHandler
import at.petrak.hexcasting.forge.network.MsgBrainsweepAck
import at.petrak.hexcasting.interop.HexInterop
import at.petrak.hexcasting.xplat.Platform
import hauveli.hexagony.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.FakePlayerFactory
import net.minecraftforge.event.level.BlockEvent.BreakEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.network.PacketDistributor
import java.util.function.BiFunction
import java.util.function.Supplier

// https://github.com/FallingColors/HexMod/blob/532fe9a60138544112e096812c7aefb78b3d7364/Forge/src/main/java/at/petrak/hexcasting/forge/xplat/ForgeXplatImpl.java

class ForgeServerXplatImpl() : IXplatAbstractions {
    override fun platform(): Platform {
        return Platform.FORGE
    }

    override fun isPhysicalClient(): Boolean {
        return FMLLoader.getDist() == Dist.CLIENT
    }

    override fun isModPresent(id: String?): Boolean {
        return ModList.get().isLoaded(id)
    }

    override fun initPlatformSpecific() {
        if (this.isModPresent(HexInterop.Forge.CURIOS_API_ID)) {
            CuriosApiInterop.init()
        }
    }

    //    @Override
    //    public double getReachDistance(Player player) {
    //        return player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
    //    }
    override fun setBrainsweepAddlData(livingEntity: LivingEntity?, state: Boolean) {
        livingEntity?.getPersistentData()?.putBoolean(TAG_BRAINSWEPT, true)

        if (livingEntity?.level() is ServerLevel) {
            ForgePacketHandler.getNetwork()
                .send(
                    PacketDistributor.TRACKING_ENTITY.with(Supplier { livingEntity }),
                    MsgBrainsweepAck.of(livingEntity)
                )
        }
    }

    override fun isBrainswept(livingEntity: LivingEntity?): Boolean {
        return livingEntity?.getPersistentData()?.getBoolean(TAG_BRAINSWEPT) ?: false
    }

    override fun isBreakingAllowed(world: ServerLevel?, pos: BlockPos?, state: BlockState?, player: Player?): Boolean {
        var player = player
        if (player == null) player =
            FakePlayerFactory.get(world, at.petrak.hexcasting.xplat.IXplatAbstractions.HEXCASTING)
        return !MinecraftForge.EVENT_BUS.post(BreakEvent(world, pos, state, player))
    }

    override fun isPlacingAllowed(
        world: ServerLevel?,
        pos: BlockPos?,
        blockStack: ItemStack?,
        player: Player?
    ): Boolean {
        var player = player
        if (player == null) player =
            FakePlayerFactory.get(world, at.petrak.hexcasting.xplat.IXplatAbstractions.HEXCASTING)
        val cached = player?.getMainHandItem()
        //if (blockStack == null) return false // huh???
        player?.setItemInHand(InteractionHand.MAIN_HAND, blockStack?.copy())
        val evt = ForgeHooks.onRightClickBlock(
            player, InteractionHand.MAIN_HAND, pos,
            BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, true)
        )
        player?.setItemInHand(InteractionHand.MAIN_HAND, cached)
        return !evt.isCanceled()
    }

    override fun <T : BlockEntity> createBlockEntityType(
        factory: BiFunction<BlockPos, BlockState, T>,
        vararg blocks: Block
    ): BlockEntityType<T> {
        return BlockEntityType.Builder
            .of(factory::apply, *blocks)
            .build(null)
    }

    companion object {
        const val TAG_BRAINSWEPT: String = "hexcasting:brainswept"
    }
}