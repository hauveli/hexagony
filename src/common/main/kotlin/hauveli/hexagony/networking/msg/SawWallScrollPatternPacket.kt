package hauveli.hexagony.networking.msg


// S2C is unused because there's no point, err I'm leaving it here just in case I change my mind and want to notify the player with a sound or osmething idk, advancements probably can do that for me...
@JvmRecord
data class SawWallScrollPatternPacketS2C(
    val resourceKey: String,
    val angles: String,
    val startDir: String
) : HexagonyMessageS2C {
    companion object : HexagonyMessageCompanion<SawWallScrollPatternPacketS2C> {
        override val type = SawWallScrollPatternPacketS2C::class.java
    }
}

@JvmRecord
data class SawWallScrollPatternPacketC2S(
    val resourceKey: String,
    val uuid: String
) : HexagonyMessageC2S {
    companion object : HexagonyMessageCompanion<SawWallScrollPatternPacketC2S> {
        override val type = SawWallScrollPatternPacketC2S::class.java
    }
}