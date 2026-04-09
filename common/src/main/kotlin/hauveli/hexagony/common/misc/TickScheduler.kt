package hauveli.hexagony.common.misc

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.TickEvent
import hauveli.hexagony.common.control.PlayerActionAPI.onServerTick
import net.minecraft.server.MinecraftServer
import net.minecraft.util.profiling.jfr.event.ServerTickTimeEvent

data class ScheduledTask(
    val executeAt: Long,
    val action: (MinecraftServer) -> Unit
)

object TickScheduler {
    private val tasks = mutableListOf<ScheduledTask>()

    fun schedule(delayTicks: Long, action: (MinecraftServer) -> Unit) {
        val targetTick = currentServerTick + delayTicks
        tasks += ScheduledTask(targetTick, action)
    }

    var currentServerTick = 0L
        private set

    fun tick(server: MinecraftServer) {
        currentServerTick++

        val ready = tasks.filter { it.executeAt <= currentServerTick }
        ready.forEach { it.action(server) }
        tasks.removeAll(ready)
    }

    fun init() {
        TickEvent.Server.SERVER_POST.register {
                server ->
            tick(server)
        }
    }
}
