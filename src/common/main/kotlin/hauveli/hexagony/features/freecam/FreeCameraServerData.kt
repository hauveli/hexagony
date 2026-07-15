package hauveli.hexagony.features.freecam

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.client.sound.GridSoundInstance
import at.petrak.hexcasting.common.lib.HexAttributes
import at.petrak.hexcasting.common.lib.HexSounds
import com.mojang.authlib.GameProfile
import hauveli.hexagony.Hexagony
import hauveli.hexagony.Hexagony.MINECRAFT
import hauveli.hexagony.Hexagony.id
import hauveli.hexagony.registry.HexagonyMobEffects
import hauveli.hexagony.registry.HexagonySounds
import net.minecraft.client.CameraType
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.player.Input
import net.minecraft.client.player.KeyboardInput
import net.minecraft.client.player.LocalPlayer
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.Pose
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.Scoreboard
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// Just stuff to process and get data on the server I guess
object FreeCameraServerData {

    val playerData: MutableMap<ServerPlayer, Pair<Vec3, Vec3>> = mutableMapOf()

    fun getPos(target: ServerPlayer): List<Iota>? {
        return playerData[target]?.first?.asActionResult
    }

    fun getLookDir(target: ServerPlayer): List<Iota>? {
        return playerData[target]?.second?.asActionResult
    }

    // I kind of really don't like creating a pair like this, will the compiler know from the syntactic sugar that
    // it can always just re-assign the numbers in these vectors?
    // Do I need to babysit it and break it up into xyz abc? (Doubles)?
    // I kind of wish I knew more about java compilers but I'm at a point in my life where if it works and the cost is the garbage collector
    // sometimes costing the server 0~1 ticks, I won't hate myself forever... But if you're reading this and have more knowledge please open a PR or leave a message!
    fun setData(player: ServerPlayer, absolutePositionOfEyes: Vec3, lookDirButViaHexAPI: Vec3) {
        playerData[player] = Pair(absolutePositionOfEyes, lookDirButViaHexAPI)
    }

    fun clearData(player: ServerPlayer) {
        playerData.remove(player)
    }
}
