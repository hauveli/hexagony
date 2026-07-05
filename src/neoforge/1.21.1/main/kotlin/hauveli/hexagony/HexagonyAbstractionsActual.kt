@file:JvmName("HexagonyAbstractionsActual")

package hauveli.hexagony

import hauveli.hexagony.registry.HexagonyRegistrar
import net.msrandom.multiplatform.annotations.Actual
import net.neoforged.neoforge.registries.RegisterEvent

actual fun <T : Any> initRegistry(registrar: HexagonyRegistrar<T>) {
        NeoForgeHexagony.container.eventBus!!.addListener { event: RegisterEvent ->
            event.register(registrar.registryKey) { helper ->
                registrar.init(helper::register)
            }
        }
}