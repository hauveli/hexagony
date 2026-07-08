package hauveli.hexagony.networking.msg


@JvmRecord
data class PerWorldPatternPacketS2C(
    val resourceKey: String,
    val angles: String,
    val startDir: String
) : HexagonyMessageS2C {
    companion object : HexagonyMessageCompanion<PerWorldPatternPacketS2C> {
        override val type = PerWorldPatternPacketS2C::class.java
    }
}

@JvmRecord
data class PerWorldPatternPacketC2S(
    val resourceKey: String
) : HexagonyMessageC2S {
    companion object : HexagonyMessageCompanion<PerWorldPatternPacketC2S> {
        override val type = PerWorldPatternPacketC2S::class.java
    }
}