package pyotr.popov443.glviewwallpaper

import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.content.Context


object TextureUtils {
    fun loadTexture(context: Context, resourceId: Int): Int {
        // создание объекта текстуры
        val textureIds = IntArray(1)
        GLES30.glGenTextures(1, textureIds, 0)
        if (textureIds[0] == 0) {
            return 0
        }

        // получение Bitmap
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(
            context.resources, resourceId, options
        )
        if (bitmap == null) {
            GLES30.glDeleteTextures(1, textureIds, 0)
            return 0
        }

        // настройка объекта текстуры
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        // сброс target
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        return textureIds[0]
    }
}
