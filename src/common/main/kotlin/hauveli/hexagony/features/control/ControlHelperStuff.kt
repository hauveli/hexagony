package hauveli.hexagony.features.control

object ControlHelperStuff {
    // how it feels to do some nonsense :ridingmybikey:
    private const val DEG_TO_INDEX = (65536.0 / 360.0).toFloat()
    private const val INDEX_TO_DEG = (360.0 / 65536.0).toFloat()

    fun pack(yRot: Float, xRot: Float): Int {
        val x = (xRot * DEG_TO_INDEX).toInt() and 0xFFFF
        val y = (yRot * DEG_TO_INDEX).toInt() and 0xFFFF

        return (x shl 16) or y
    }

    fun unpackX(packed: Int): Float {
        val xIndex = (packed ushr 16) and 0xFFFF
        return xIndex * INDEX_TO_DEG
    }

    fun unpackY(packed: Int): Float {
        val yIndex = packed and 0xFFFF
        return yIndex * INDEX_TO_DEG
    }
}