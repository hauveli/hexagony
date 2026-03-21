package hauveli.hexagony.fabric.xplat


import at.petrak.hexcasting.fabric.cc.CCBrainswept
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents.BRAINSWEPT
import at.petrak.hexcasting.fabric.interop.trinkets.TrinketsApiInterop
import at.petrak.hexcasting.interop.HexInterop
import at.petrak.hexcasting.xplat.Platform
import hauveli.hexagony.xplat.IXplatAbstractions
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.entity.FakePlayer
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState

// https://github.com/FallingColors/HexMod/blob/532fe9a60138544112e096812c7aefb78b3d7364/Fabric/src/main/java/at/petrak/hexcasting/fabric/xplat/FabricXplatImpl.java
class FabricServerXplatImpl() : IXplatAbstractions {

    override fun platform(): Platform {
        return Platform.FABRIC
    }

    override fun isPhysicalClient(): Boolean {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
    }

    override fun isModPresent(id: String?): Boolean {
        return FabricLoader.getInstance().isModLoaded(id)
    }

    override fun initPlatformSpecific() {
        if (this.isModPresent(HexInterop.Fabric.TRINKETS_API_ID)) {
            TrinketsApiInterop.init()
        }
    }

    override fun setBrainsweepAddlData(livingEntity: LivingEntity?) {
        val cc: CCBrainswept = BRAINSWEPT.get(livingEntity)
        cc.setBrainswept(true)
        // CC API does the syncing for us
    }

    override fun isBrainswept(livingEntity: LivingEntity?): Boolean {
        val cc: CCBrainswept = BRAINSWEPT.get(livingEntity)
        return cc.isBrainswept()
    }

    override fun isBreakingAllowed(world: ServerLevel?,
                                   pos: BlockPos?,
                                   state: BlockState?,
                                   player: Player?): Boolean {
        var player = player
        if (player == null) player = FakePlayer.get(world, IXplatAbstractions.HEXCASTING)
        return PlayerBlockBreakEvents.BEFORE.invoker()
            .beforeBlockBreak(world, player, pos, state, world?.getBlockEntity(pos))
    }

    override fun isPlacingAllowed(world: ServerLevel?,
                                  pos: BlockPos?,
                                  blockStack: ItemStack?,
                                  player: Player?): Boolean {
        var player = player
        if (player == null) player = FakePlayer.get(world, IXplatAbstractions.HEXCASTING)
        val cached = player?.getMainHandItem()
        player?.setItemInHand(InteractionHand.MAIN_HAND, blockStack?.copy())
        val success = UseItemCallback.EVENT.invoker().interact(player, world, InteractionHand.MAIN_HAND)
        player?.setItemInHand(InteractionHand.MAIN_HAND, cached)
        return success.getResult() == InteractionResult.PASS // No other mod tried to consume this
    }
}