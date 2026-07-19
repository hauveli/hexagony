package hauveli.hexagony.features.freecam

import hauveli.hexagony.Hexagony
import hauveli.hexagony.client.HexagonyClient.MINECRAFT
import net.minecraft.client.renderer.PostChain
import net.minecraft.resources.ResourceLocation
import java.io.IOException

// thank you miyu
// https://github.com/miyucomics/hexical/blob/8fb0ef85d8b3918d5fea6d62db90571ea35b7cb2/src/client/java/miyucomics/hexical/features/shaders/ShaderRenderer.kt#L8
object ShaderRenderer {
    private var activeShader: PostChain? = null
    private var lastShader: PostChain? = null
    private var lastWidth = 0
    private var lastHeight = 0

    @JvmStatic
    fun render(dt: Float) {
        render(dt, null, null, null, null)
    }

    @JvmStatic
    fun render(dt: Float, arg1: Float?) {
        render(dt, arg1, null, null, null)
    }

    @JvmStatic
    fun render(dt: Float, arg1: Float?, arg2: Float?) {
        render(dt, arg1, arg2, null, null)
    }

    @JvmStatic
    fun render(dt: Float, arg1: Float?, arg2: Float?, arg3: Float?) {
        render(dt, arg1, arg2, arg3, null)
    }

    @JvmStatic
    fun render(deltaTick: Float, arg1: Float?, arg2: Float?, arg3: Float?, arg4: Float?) {
        if (activeShader == null)
            return

        if (lastShader != activeShader) {
            lastShader = activeShader
            lastWidth = 0
            lastHeight = 0
        }

        updateEffectSize(activeShader!!)
        // todo: something more sensible than this...?
        if (arg1 != null) {
            activeShader!!.setUniform("FirstArgumentAmount", arg1)
        }
        if (arg2 != null) {
            activeShader!!.setUniform("SecondArgumentAmount", arg2)
        }
        if (arg3 != null) {
            activeShader!!.setUniform("ThirdArgumentAmount", arg3)
        }
        if (arg4 != null) {
            activeShader!!.setUniform("FourthArgumentAmount", arg4)
        }
        activeShader!!.process(deltaTick)
        MINECRAFT!!.mainRenderTarget.bindWrite(false)
    }

    fun setEffect(location: ResourceLocation?) {
        activeShader?.close()
        if (location == null) {
            activeShader = null
            return
        }
        try {
            val client = MINECRAFT!!
            activeShader = PostChain(client.textureManager, client.resourceManager, client.mainRenderTarget, location)
        } catch (_: IOException) {}
    }

    private fun updateEffectSize(effect: PostChain) {
        val client = MINECRAFT!!
        val width = client.window.width
        val height = client.window.height
        if ((width != lastWidth || height != lastHeight) && width != 0 && height != 0) {
            lastWidth = width
            lastHeight = height
            effect.resize(width, height)
        }
    }
}