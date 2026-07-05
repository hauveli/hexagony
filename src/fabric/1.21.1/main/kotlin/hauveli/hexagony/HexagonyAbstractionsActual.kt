@file:JvmName("HexagonyAbstractionsActual")

package hauveli.hexagony

import hauveli.hexagony.registry.HexagonyRegistrar
import net.minecraft.core.Registry
import net.msrandom.multiplatform.annotations.Actual

actual fun <T : Any> initRegistry(registrar: HexagonyRegistrar<T>) {
    val registry = registrar.registry
    registrar.init { id, value -> Registry.register(registry, id, value) }
}
