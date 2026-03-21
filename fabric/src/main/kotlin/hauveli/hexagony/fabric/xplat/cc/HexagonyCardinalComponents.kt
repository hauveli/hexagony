package hauveli.hexagony.fabric.xplat.cc
import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.addldata.ADMediaHolder
import at.petrak.hexcasting.api.addldata.ItemDelegatingEntityIotaHolder
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.item.*
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.common.entities.EntityWallScroll
import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexItems
// import at.petrak.hexcasting.fabric.cc.*
import at.petrak.hexcasting.fabric.cc.adimpl.*
import dev.onyxstudios.cca.api.v3.component.ComponentFactory
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer
import hauveli.hexagony.Hexagony
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

class HexagonyCardinalComponents : EntityComponentInitializer, ItemComponentInitializer {

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers<CCBrainswept?, CCBrainswept?>(
            BRAINSWEPT,
            { owner: Player? -> CCBrainswept(owner) },
            RespawnCopyStrategy.ALWAYS_COPY
        )

        /*
        registry.registerFor<CCIotaHolder?, ItemEntity?>(
            ItemEntity::class.java, IOTA_HOLDER, wrapItemEntityDelegate<ItemEntity?>(
                Function { entity: ItemEntity? -> ItemDelegatingEntityIotaHolder.ToItemEntity(entity) })
        )
        */
    }

    override fun registerItemComponentFactories(registry: ItemComponentFactoryRegistry) {

        registry.register<CCItemIotaHolder.ItemBased?>(
            Predicate { i: Item? -> i is IotaHolderItem },
            IOTA_HOLDER,
            ComponentFactory { stack: ItemStack? -> CCItemIotaHolder.ItemBased(stack) })

        registry.register<CCMediaHolder.ItemBased?>(
            Predicate { i: Item? -> i is MediaHolderItem },
            MEDIA_HOLDER,
            ComponentFactory { stack: ItemStack? -> CCMediaHolder.ItemBased(stack) })

        /*
        registry.register<CCMediaHolder.Static?>(
            HexBlocks.QUENCHED_ALLAY.asItem(),
            MEDIA_HOLDER,
            ComponentFactory { s: ItemStack? ->
                CCMediaHolder.Static(
                    Supplier { MediaConstants.QUENCHED_BLOCK_UNIT }, ADMediaHolder.QUENCHED_ALLAY_PRIORITY, s
                )
            })
        */

    }

    private fun <E : Entity?> wrapItemEntityDelegate(make: Function<E?, ItemDelegatingEntityIotaHolder?>): ComponentFactory<E?, CCEntityIotaHolder.Wrapper?> {
        return ComponentFactory { e: E? -> CCEntityIotaHolder.Wrapper(make.apply(e)) }
    }

    companion object {
        // entities
        val BRAINSWEPT: ComponentKey<CCBrainswept?> = ComponentRegistry.getOrCreate(
            ResourceLocation(Hexagony.MODID, "brainswept"),
            CCBrainswept::class.java
        )

        val IOTA_HOLDER: ComponentKey<CCIotaHolder?> = ComponentRegistry.getOrCreate(
            ResourceLocation(Hexagony.MODID, "iota_holder"),
            CCIotaHolder::class.java
        )
        val MEDIA_HOLDER: ComponentKey<CCMediaHolder?> = ComponentRegistry.getOrCreate(
            ResourceLocation(Hexagony.MODID, "media_holder"),
            CCMediaHolder::class.java
        )
    }
}