@file:JvmName("HexagonyAbstractions")

package hauveli.hexagony

import hauveli.hexagony.registry.HexagonyRegistrar

fun initRegistries(vararg registries: HexagonyRegistrar<*>) {
    for (registry in registries) {
        initRegistry(registry)
    }
}

expect fun <T : Any> initRegistry(registrar: HexagonyRegistrar<T>)
