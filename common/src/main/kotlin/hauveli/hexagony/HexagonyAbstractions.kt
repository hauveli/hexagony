@file:JvmName("HexagonyAbstractions")

package hauveli.hexagony

import dev.architectury.injectables.annotations.ExpectPlatform
import hauveli.hexagony.registry.HexagonyRegistrar

fun initRegistries(vararg registries: HexagonyRegistrar<*>) {
    for (registry in registries) {
        initRegistry(registry)
    }
}

@ExpectPlatform
fun <T : Any> initRegistry(registrar: HexagonyRegistrar<T>) {
    throw AssertionError()
}
