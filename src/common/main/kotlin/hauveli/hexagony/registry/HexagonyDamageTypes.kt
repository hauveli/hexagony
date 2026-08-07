package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.damagesource.DamageType

// todo: decide if these belong in /registry
class HexagonyDamageTypes {

    companion object {
        @JvmField
        var HIDDEN: ResourceKey<DamageType?> = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Hexagony.id("hidden")
        )

        @JvmField
        var MINDGRAFT: ResourceKey<DamageType?> = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Hexagony.id("mindgraft")
        )
    }

}