package hauveli.hexagony.common.control

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.Level

object PlayerActionAPI {

    /*
    private val runtime = ConcurrentHashMap<UUID, MindAnchorRuntime>()

    private fun runtime(uuid: UUID) =
        runtime.computeIfAbsent(uuid) { MindAnchorRuntime() }
*/
    private val mc: Minecraft
        get() = Minecraft.getInstance()

    private val player: LocalPlayer?
        get() = mc.player

    private val level: Level?
        get() = mc.level

    fun onServerTick(server: MinecraftServer) {
        val data = PlayerControlData.get(server)

        data.players.forEach { (uuid, e) ->
            val p = server.playerList.getPlayer(uuid)
            if (p != null) {
                // If shouldMoveForwardBackward is 0 and we set p.zza it may conflict, check needed, I think...
                if (e.shouldMoveForwardBackward != 0f) {
                    p.zza = e.shouldMoveForwardBackward   // forwar hehe pizza
                }
                if (e.shouldMoveLeftRight != 0f) {
                    p.xxa = e.shouldMoveLeftRight   // side
                }
                if (e.shouldLookUpDown != 0f) {
                    p.yRot = e.shouldLookUpDown
                }
                if (e.shouldLookLeftRight != 0f) {
                    p.xRot = e.shouldLookLeftRight
                }
                if (e.shouldJump && p.onGround()) {
                    println(p.toString())
                    p.setJumping(true)
                    p.jumpFromGround()
                    // p.jumpBoostPower
                    // shouldJump = false // do I want it to be continuous if no other inputs?
                }
                if (e.shouldSprint && p.canSprint()) {
                    p.isSprinting = true
                }
                if (e.shouldSneak) {
                    p.isShiftKeyDown = true
                }

                if (e.shouldAttack) {
                    // p.swing is local
                    if (e.shouldAttackPeriod == -1) {

                        // TODO: do I really want to do it this way?
                        // Hmm...
                        p.swing(player!!.usedItemHand) // Momentary
                        e.shouldAttack = false
                    } else if (e.shouldAttackPeriod == 0) {
                        p.swing(player!!.usedItemHand) // Continuous
                    } else if ((level!!.gameTime % e.shouldAttackPeriod) == 0L) {
                        p.swing(player!!.usedItemHand) // Periodic
                    }
                }

                if (e.shouldUse) {
                    if (e.shouldUsePeriod == -1) {
                        // TODO: do I really want to do it this way?
                        // Hmm...
                        mc.options.keyUse.isDown = true // Hmm.... I don't think I can use .isDown because it would conflict...
                        e.shouldUse = false
                    } else if (e.shouldUsePeriod == 0) {
                        p.swing(player!!.usedItemHand) // Continuous
                    } else if ((level!!.gameTime % e.shouldUsePeriod) == 0L) {
                        p.swing(player!!.usedItemHand) // Periodic
                    }
                }


                if (e.shouldHotbarSlot != -1) {
                    p.inventory.selected = e.shouldHotbarSlot + 1
                    e.shouldHotbarSlot = -1 // reset, no reason to be persistent?
                }

                if (e.shouldSwapHands) {
                    // PlayerActionPacketAPI.swapHands(p)
                    val tempItemStack = p.getItemInHand(InteractionHand.MAIN_HAND)
                    p.setItemInHand(InteractionHand.MAIN_HAND, p.getItemInHand(InteractionHand.OFF_HAND))
                    p.setItemInHand(InteractionHand.OFF_HAND, tempItemStack)
                    e.shouldSwapHands = false
                    //data.setDirty()
                }

                if (e.shouldDrop) {
                    p.drop(e.shouldDropStack)
                    e.shouldDrop = false
                    //data.setDirty()
                }
            }
        }
        data.setDirty()
        // Todo: smarter way to mark dirty?
        // data.setDirty()
    }
}