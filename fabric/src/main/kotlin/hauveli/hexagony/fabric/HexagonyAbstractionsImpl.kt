@file:JvmName("HexagonyAbstractionsImpl")

package hauveli.hexagony.fabric

import hauveli.hexagony.registry.HexagonyRegistrar
import net.minecraft.core.Registry

fun <T : Any> initRegistry(registrar: HexagonyRegistrar<T>) {
    val registry = registrar.registry
    registrar.init { id, value -> Registry.register(registry, id, value) }
}
