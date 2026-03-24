package hauveli.hexagony.registry

import hauveli.hexagony.Hexagony
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.damagesource.DamageType

// Erm... I am registering this into my registry, should I move it into registrar instead?
class HexagonyDamageTypes {

    companion object {
        @JvmField
        var HIDDEN: ResourceKey<DamageType?> = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation(Hexagony.MODID, "hidden")
        )

        @JvmField
        var BRAINSWEEP: ResourceKey<DamageType?> = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation(Hexagony.MODID, "brainsweep")
        )
    }

}